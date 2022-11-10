//  DJIWaypointMissionSession.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 9/30/21.
//  Copyright © 2021 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.dronelink.core.Convert;
import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneControlSession;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.Dronelink;
import com.dronelink.core.Executor;
import com.dronelink.core.MissionExecutor;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.kernel.command.camera.ModeCameraCommand;
import com.dronelink.core.kernel.command.camera.PhotoModeCameraCommand;
import com.dronelink.core.kernel.command.camera.StartCaptureCameraCommand;
import com.dronelink.core.kernel.command.camera.StopCaptureCameraCommand;
import com.dronelink.core.kernel.component.DJIWaypointMissionComponent;
import com.dronelink.core.kernel.core.CameraCaptureConfiguration;
import com.dronelink.core.kernel.core.DJIExecutionState;
import com.dronelink.core.kernel.core.ExecutionState;
import com.dronelink.core.kernel.core.ExternalExecutionState;
import com.dronelink.core.kernel.core.GeoSpatial;
import com.dronelink.core.kernel.core.Message;
import com.dronelink.core.kernel.core.enums.CameraMode;
import com.dronelink.core.kernel.core.enums.CameraPhotoMode;
import com.dronelink.core.kernel.core.enums.ExecutionEngine;
import com.dronelink.core.kernel.core.enums.ExecutionStatus;

import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypoint.WaypointUploadProgress;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;

public class DJIWaypointMissionSession implements DroneControlSession {
    private static final String TAG = DJIWaypointMissionSession.class.getCanonicalName();

    @Override
    public ExecutionEngine getExecutionEngine() {
        return ExecutionEngine.DJI;
    }

    private enum State {
        READY,
        ACTIVATING,
        ACTIVATED,
        DEACTIVATED
    }

    private final Context context;
    private final WaypointMissionOperator djiWaypointMissionOperator;
    private final DJIDroneSession droneSession;
    private final MissionExecutor missionExecutor;
    private final DJIWaypointMissionComponent[] kernelComponents;
    private final WaypointMission[] djiWaypointMissions;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private State state = State.READY;
    private Message disengageReason;
    @Override
    public Message getDisengageReason() {
        if (disengageReason != null) {
            return disengageReason;
        }

        if (state == State.ACTIVATED) {
            final DatedValue<FlightControllerState> state =  droneSession.getFlightControllerState();
            if (state != null) {
                switch (state.value.getFlightMode()) {
                    case MOTORS_JUST_STARTED:
                    case AUTO_TAKEOFF:
                    case ASSISTED_TAKEOFF:
                    case JOYSTICK:
                    case GPS_ATTI:
                    case GPS_WAYPOINT:
                        break;

                    case GO_HOME:
                    case AUTO_LANDING:
                        if (!terminalFlightModeAllowed) {
                            return new Message(context.getString(R.string.MissionDisengageReason_drone_control_override_title), context.getString(R.string.MissionDisengageReason_drone_control_override_details));
                        }
                        break;

                    default:
                        return new Message(context.getString(R.string.MissionDisengageReason_drone_control_override_title), context.getString(R.string.MissionDisengageReason_drone_control_override_details));
                }
            }

            if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {
                    WaypointMissionState.UNKNOWN,
                    WaypointMissionState.RECOVERING,
                    WaypointMissionState.NOT_SUPPORTED,
                    WaypointMissionState.EXECUTION_PAUSED
            })) {
                return new Message(context.getString(R.string.MissionDisengageReason_drone_control_override_title), context.getString(R.string.MissionDisengageReason_drone_control_override_details));
            }
            else if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {WaypointMissionState.DISCONNECTED})) {
                return new Message(context.getString(R.string.MissionDisengageReason_drone_disconnected_title));
            }
        }

        return null;
    }

    private DJIExecutionState getExecutionState() {
        final ExternalExecutionState state = missionExecutor.getExternalExecutionState(getExecutionEngine());
        return state instanceof DJIExecutionState ? (DJIExecutionState)state : null;
    }
    private int resumeWaypointIndex = 0;
    private double resumeWaypointProgress = 0.0;
    private boolean terminalFlightModeAllowed = false;
    private WaypointExecutionProgress latestProgress = null;

    public DJIWaypointMissionSession(final Context context, final DJIDroneSession droneSession, final MissionExecutor missionExecutor) throws JSONException {
        this.context = context;
        this.kernelComponents = missionExecutor.getJSONForExecutionEngine(DJIWaypointMissionComponent[].class, ExecutionEngine.DJI);
        if (kernelComponents == null || kernelComponents.length == 0) {
            throw new JSONException("kernelComponents invalid");
        }
        this.djiWaypointMissionOperator = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        this.droneSession = droneSession;
        this.missionExecutor = missionExecutor;
        this.djiWaypointMissions = new WaypointMission[kernelComponents.length];
        for (int i = 0; i < kernelComponents.length; i++) {
            djiWaypointMissions[i] = DronelinkDJI.getWaypointMission(kernelComponents[i]);
            final DJIError error = djiWaypointMissions[i].checkParameters();
            if (error != null) {
                throw new JSONException(error.toString());
            }
        }

        final ExecutionState state = getExecutionState();
        if (state != null && state.status.completed) {
            throw new JSONException("mission completed already");
        }
    }

    @Override
    public boolean isReengaging() {
        if (state == State.ACTIVATED && (resumeWaypointIndex > 0 || resumeWaypointProgress > 0) && latestProgress != null) {
            return latestProgress.targetWaypointIndex == 0;
        }

        return false;
    }

    @Override
    public Boolean activate() {
        switch (state) {
            case READY:
                state = State.ACTIVATING;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        activating();
                    }
                });
                return null;

            case ACTIVATING:
                return disengageReason == null ? null : false;

            case ACTIVATED:
                return true;

            case DEACTIVATED:
            default:
                return false;
        }
    }

    private void activating() {
        if (state != State.ACTIVATING || djiWaypointMissionOperator == null) {
            return;
        }

        final DJIExecutionState executionState = getExecutionState();
        if (executionState == null || executionState.componentIndex >= djiWaypointMissions.length) {
            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
            return;
        }

        final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];
        final WaypointMission loadedMission = djiWaypointMissionOperator.getLoadedMission();
        if (loadedMission != null && loadedMission.getMissionID() == currentDJIWaypointMission.getMissionID()) {
            if (WaypointMissionState.EXECUTING.equals(djiWaypointMissionOperator.getCurrentState())) {
                Log.i(TAG, "Mission already executing");
                updateExternalExecutionState(new HashMap<String, String>() {{
                    put("revertDisengagment", "true");
                }});

                if (loadedMission.getWaypointCount() < currentDJIWaypointMission.getWaypointCount()) {
                    resumeWaypointIndex = currentDJIWaypointMission.getWaypointCount() - loadedMission.getWaypointCount();
                    resumeWaypointProgress = executionState.waypointProgress;
                }

                state = State.ACTIVATED;
                startProgressListeners();
                return;
            }
        }

        if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {
                WaypointMissionState.EXECUTING,
                WaypointMissionState.EXECUTION_PAUSED
        })) {
            Log.i(TAG, "Stopping previous mission");
            djiWaypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(final DJIError djiError) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            startCurrentMission();
                        }
                    });
                }
            });
            return;
        }

        startCurrentMission();
    }

    private void startCurrentMission() {
        if (state == State.DEACTIVATED || djiWaypointMissionOperator == null) {
            return;
        }

        final DJIExecutionState executionState = getExecutionState();
        if (executionState == null || executionState.componentIndex >= djiWaypointMissions.length) {
            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
            return;
        }

        final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];

        terminalFlightModeAllowed = false;
        final WaypointMission.Builder mission = new WaypointMission.Builder(currentDJIWaypointMission);
        if (executionState.waypointProgress > 0) {
            while (mission.getWaypointCount() > 0) {
                mission.removeWaypoint(0);
            }
            resumeWaypointIndex = executionState.waypointIndex;
            resumeWaypointProgress = executionState.waypointProgress;
            for (int index = 0; index < currentDJIWaypointMission.getWaypointCount(); index++) {
                if (index >= resumeWaypointIndex) {
                    final Waypoint waypoint = currentDJIWaypointMission.getWaypointList().get(index);
                    if (mission.getWaypointCount() == 0 && resumeWaypointProgress > 0 && index + 1 < currentDJIWaypointMission.getWaypointCount() - 1) {
                        final Waypoint nextWaypoint = currentDJIWaypointMission.getWaypointList().get(index + 1);
                        final GeoSpatial reengagementSpatial = missionExecutor.getReengagementSpatial();
                        if (reengagementSpatial != null) {
                            waypoint.removeAllAction();
                            waypoint.coordinate = DronelinkDJI.getCoordinate(reengagementSpatial.coordinate);
                            waypoint.altitude = (float)reengagementSpatial.altitude.value;
                            if (mission.getHeadingMode() == WaypointMissionHeadingMode.USING_WAYPOINT_HEADING) {
                                waypoint.heading += ((int)((double)(nextWaypoint.heading - waypoint.heading) * resumeWaypointProgress));
                            }

                            if (mission.isGimbalPitchRotationEnabled()) {
                                waypoint.gimbalPitch += ((nextWaypoint.gimbalPitch - waypoint.gimbalPitch) * (float)(resumeWaypointProgress));
                            }
                        }

                        if (DronelinkDJI.getDistance(waypoint, nextWaypoint) < 0.5) {
                            final Location waypointLocation = DronelinkDJI.getLocation(waypoint.coordinate);
                            final Location nextWaypointLocation = DronelinkDJI.getLocation(nextWaypoint.coordinate);
                            waypoint.coordinate = DronelinkDJI.getCoordinate(Convert.locationWithBearing(waypointLocation, waypointLocation.bearingTo(nextWaypointLocation), 0.5));
                            break;
                        }

                        final float distance = DronelinkDJI.getLocation(waypoint.coordinate).distanceTo(DronelinkDJI.getLocation(nextWaypoint.coordinate));
                        nextWaypoint.cornerRadiusInMeters = Math.max(Math.min(((distance - 1) / 2), nextWaypoint.cornerRadiusInMeters), 0.2f);
                    }

                    if (mission.getWaypointCount() == 0) {
                        waypoint.cornerRadiusInMeters = 0.2f;
                    }

                    mission.addWaypoint(waypoint);
                }
            }

            Log.i(TAG, String.format("Updating mission to resume at %s between waypoint %d and %d", Dronelink.getInstance().format("percent", resumeWaypointProgress, ""), resumeWaypointIndex + 1, resumeWaypointIndex + 2));
        }

        Log.i(TAG,"Attempting load mission");
        final DJIError error = djiWaypointMissionOperator.loadMission(mission.build());
        if (error != null) {
            Log.e(TAG,"Load mission failed: " + error.getDescription());
            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_load_mission_failed_title), error.getDescription());
            return;
        }

        Log.i(TAG,"Load mission succeeded");
        djiWaypointMissionOperator.addListener(uploadListener);

        Log.i(TAG,"Attempting to start uploading mission");
        djiWaypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (state == State.DEACTIVATED) {
                    return;
                }

                if (djiError != null) {
                    uploadFailed(djiError);
                    return;
                }

                Log.i(TAG,"Start uploading mission succeeded");
            }
        });
    }

    private final WaypointMissionOperatorListener uploadListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(final WaypointMissionDownloadEvent event) {}

        @Override
        public void onUploadUpdate(final WaypointMissionUploadEvent event) {
            if (state == State.DEACTIVATED || djiWaypointMissionOperator == null) {
                return;
            }

            final DJIExecutionState executionState = getExecutionState();
            if (executionState == null || executionState.componentIndex >= djiWaypointMissions.length) {
                disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
                return;
            }

            if (WaypointMissionState.UPLOADING.equals(event.getCurrentState())) {
                final WaypointUploadProgress progress = event.getProgress();
                if (progress != null) {
                    Log.i(TAG, "Uploading mission progress: " + event.getProgress().uploadedWaypointIndex);

                    if (state == State.ACTIVATED) {
                        final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];
                        updateExternalExecutionState(new Message[]{
                                new Message(context.getString(R.string.DJIWaypointMissionSession_statusMessage_uploading_title,
                                        progress.uploadedWaypointIndex + 1,
                                        currentDJIWaypointMission.getWaypointCount()))
                        }, null);
                    }
                }
                return;
            }

            if (WaypointMissionState.READY_TO_EXECUTE.equals(event.getCurrentState())) {
                djiWaypointMissionOperator.removeListener(uploadListener);

                final DJIWaypointMissionComponent currentKernelComponent = kernelComponents[executionState.componentIndex];
                startCameraCaptureConfigurations(currentKernelComponent.cameraCaptureConfigurations);

                Log.i(TAG, "Attempting start mission");
                djiWaypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (state == State.DEACTIVATED) {
                            return;
                        }

                        if (djiError != null) {
                            Log.e(TAG, "Start mission failed: " + djiError.getDescription());
                            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_start_mission_failed_title), djiError.getDescription());
                            return;
                        }

                        Log.i(TAG, "Start mission succeeded");
                        state = State.ACTIVATED;
                        startProgressListeners();
                    }
                });
                return;
            }

            if (DronelinkDJI.isWaypointMissionState(event.getCurrentState(), new WaypointMissionState[] {
                    WaypointMissionState.UNKNOWN,
                    WaypointMissionState.READY_TO_UPLOAD,
                    WaypointMissionState.DISCONNECTED,
                    WaypointMissionState.RECOVERING,
                    WaypointMissionState.NOT_SUPPORTED,
                    WaypointMissionState.EXECUTING,
                    WaypointMissionState.EXECUTION_PAUSED
            })) {
                uploadFailed(event.getError());
            }
        }

        @Override
        public void onExecutionUpdate(final WaypointMissionExecutionEvent event) {}

        @Override
        public void onExecutionStart() {}

        @Override
        public void onExecutionFinish(final DJIError djiError) {}
    };

    private void updateExternalExecutionState(final ExecutionStatus status) {
        missionExecutor.updateExternalExecutionState(getExecutionEngine(), status, 0, null, null, null);
    }

    private void updateExternalExecutionState(final Map<String, String> values) {
        updateExternalExecutionState(null, values);
    }

    private void updateExternalExecutionState(final Message[] messages, final Map<String, String> values) {
        missionExecutor.updateExternalExecutionState(getExecutionEngine(), ExecutionStatus.EXECUTING, 0, null, messages, values);
    }

    private void uploadFailed(final DJIError error) {
        final String details = error == null ? "Unknown Error" : error.getDescription();
        Log.e(TAG, "Upload mission failed: " + details);
        disengageReason = new Message(context.getString(R.string.MissionDisengageReason_upload_mission_failed_title), details);
    }

    private void startProgressListeners() {
        if (state != State.ACTIVATED || djiWaypointMissionOperator == null) {
            return;
        }

        djiWaypointMissionOperator.addListener(progressListener);
    }

    final WaypointMissionOperatorListener progressListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(final WaypointMissionDownloadEvent event) {}

        @Override
        public void onUploadUpdate(final WaypointMissionUploadEvent event) {}

        @Override
        public void onExecutionUpdate(final WaypointMissionExecutionEvent event) {
            if (state != State.ACTIVATED) {
                return;
            }

            final DJIExecutionState executionState = getExecutionState();
            if (executionState == null || executionState.componentIndex >= djiWaypointMissions.length) {
                disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
                return;
            }

            final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];
            final DJIWaypointMissionComponent currentKernelComponent = kernelComponents[executionState.componentIndex];

            if (WaypointMissionState.EXECUTING.equals(event.getCurrentState())) {
                final WaypointExecutionProgress progress = event.getProgress();
                if (progress != null) {
                    latestProgress = progress;

                    final int waypointIndex = resumeWaypointIndex + Math.max(0, progress.targetWaypointIndex - (progress.isWaypointReached ? 0 : 1));
                    final List<Message> messages = new LinkedList<>();
                    if (kernelComponents.length > 1) {
                        messages.add(new Message(context.getString(R.string.DJIWaypointMissionSession_statusMessage_component_title,
                                executionState.componentIndex + 1, kernelComponents.length),
                                currentKernelComponent.descriptors.name));
                    }

                    double waypointProgress = 0.0;
                    double waypointDistance = 0.0;
                    double droneDistance = 0.0;

                    if (progress.targetWaypointIndex > 0) {
                        if (waypointIndex < currentDJIWaypointMission.getWaypointCount() - 2) {
                            final Waypoint a = currentDJIWaypointMission.getWaypointList().get(waypointIndex);
                            final Waypoint b = currentDJIWaypointMission.getWaypointList().get(waypointIndex + 1);
                            waypointDistance = DronelinkDJI.getDistance(a, b);
                        }

                        final Waypoint waypoint = currentDJIWaypointMission.getWaypointList().get(waypointIndex);
                        final DatedValue<DroneStateAdapter> state = droneSession.getState();
                        if (state != null) {
                            final Location droneLocation = state.value.getLocation();
                            if (droneLocation != null) {
                                final double x = DronelinkDJI.getLocation(waypoint.coordinate).distanceTo(droneLocation);
                                final double y = Math.abs((double) (waypoint.altitude) - state.value.getAltitude());
                                droneDistance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
                            }

                            waypointProgress = progress.isWaypointReached ? 1 : waypointDistance == 0 ? 0 : Math.min(1, droneDistance / waypointDistance);
                        }
                    } else {
                        waypointProgress = resumeWaypointProgress;
                    }

                    if ((resumeWaypointIndex > 0 || resumeWaypointProgress > 0) && progress.targetWaypointIndex == 0) {
                        messages.add(new Message(context.getString(R.string.DJIWaypointMissionSession_statusMessage_reengaging_title,
                                Dronelink.getInstance().format("percent", resumeWaypointIndex, ""),
                                waypointIndex + 2)));
                    } else if (progress.targetWaypointIndex == 0) {
                        messages.add(new Message(context.getString(R.string.DJIWaypointMissionSession_statusMessage_waypoint_0_title,
                                waypointIndex + 1,
                                currentDJIWaypointMission.getWaypointCount())));
                    } else {
                        messages.add(new Message(context.getString(R.string.DJIWaypointMissionSession_statusMessage_waypoint_n_title,
                                Math.min(waypointIndex + 1, (currentDJIWaypointMission.getWaypointCount()) - 1) + 1,
                                currentDJIWaypointMission.getWaypointCount(),
                                Dronelink.getInstance().format("percent", waypointProgress, ""))));
                    }

                    terminalFlightModeAllowed = waypointIndex == currentDJIWaypointMission.getWaypointCount() - 1 && progress.isWaypointReached;

                    final double waypointProgressFinal = waypointProgress;
                    updateExternalExecutionState(messages.toArray(new Message[] {}), new HashMap<String, String>() {{
                        put("waypointIndex", String.valueOf(waypointIndex));
                        put("waypointProgress", String.valueOf(waypointProgressFinal));
                    }});
                }
            }
        }

        @Override
        public void onExecutionStart() {}

        @Override
        public void onExecutionFinish(final DJIError djiError) {
            checkMissionFinishedCurrent(0);
        }
    };

    private void checkMissionFinishedCurrent(final int attempt) {
        if (attempt > 20 || state == State.DEACTIVATED) {
            return;
        }

        final DatedValue<FlightControllerState> flightControllerState = droneSession.getFlightControllerState();
        if (flightControllerState == null) {
            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
            return;
        }

        switch (flightControllerState.value.getFlightMode()) {
            case GPS_WAYPOINT:
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(250);
                        } catch (final InterruptedException ignored) {}
                        checkMissionFinishedCurrent(attempt + 1);
                    }
                });
                break;

            case JOYSTICK:
            case GPS_ATTI:
                finishCurrentMission();
                break;

            case GO_HOME:
            case AUTO_LANDING:
                if (terminalFlightModeAllowed) {
                    finishCurrentMission();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void deactivate() {
        if (djiWaypointMissionOperator == null) {
            return;
        }

        djiWaypointMissionOperator.removeListener(uploadListener);
        djiWaypointMissionOperator.removeListener(progressListener);
        //for some reason we see the flight mode go to joystick after some missions sometimes, so force it out
        if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {
                WaypointMissionState.EXECUTING,
                WaypointMissionState.EXECUTION_PAUSED
        })) {
            //TODO would be nice to be able to use pause mission
            djiWaypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(final DJIError djiError) {
                    final FlightController flightController = droneSession.getAdapter().drone.getFlightController();
                    if (flightController != null) {
                        flightController.setVirtualStickModeEnabled(false, null);
                    }
                }
            });
        }
        else {
            final FlightController flightController = droneSession.getAdapter().drone.getFlightController();
            if (flightController != null) {
                flightController.setVirtualStickModeEnabled(false, null);
            }
        }

        state = State.DEACTIVATED;
        if (WaypointMissionState.DISCONNECTED.equals(djiWaypointMissionOperator.getCurrentState())) {
            final DJIExecutionState executionState = getExecutionState();
            if (executionState != null && !executionState.status.completed) {
                startCheckMissionFinishedOffline(0);
            }
        }
    }

    private void finishCurrentMission() {
        if (state != State.ACTIVATED || djiWaypointMissionOperator == null) {
            return;
        }

        final DJIExecutionState executionState = getExecutionState();
        if (executionState == null || executionState.componentIndex >= djiWaypointMissions.length) {
            disengageReason = new Message(context.getString(R.string.MissionDisengageReason_execution_state_invalid_title));
            return;
        }

        final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];
        final WaypointMission loadedMission = djiWaypointMissionOperator.getLoadedMission();
        if (loadedMission == null || loadedMission.getMissionID() != currentDJIWaypointMission.getMissionID()) {
            return;
        }

        final DJIWaypointMissionComponent currentKernelComponent = kernelComponents[executionState.componentIndex];
        stopCameraCaptureConfigurations(currentKernelComponent.cameraCaptureConfigurations);

        final int componentIndex = executionState.componentIndex + 1;
        resumeWaypointIndex = 0;
        resumeWaypointProgress = 0;
        if (componentIndex == kernelComponents.length) {
            updateExternalExecutionState(ExecutionStatus.SUCCEEDED);
        }
        else {
            djiWaypointMissionOperator.removeListener(progressListener);
            updateExternalExecutionState(new HashMap<String, String>() {{
                put("componentIndex", String.valueOf(componentIndex));
                put("waypointIndex", "0");
                put("waypointProgress", "0");
            }});
            startCurrentMission();
        }
    }

    private void startCameraCaptureConfigurations(final CameraCaptureConfiguration[] cameraCaptureConfigurations) {
        if (cameraCaptureConfigurations != null) {
            for (final CameraCaptureConfiguration cameraCaptureConfiguration : cameraCaptureConfigurations) {
                try {
                    final StopCaptureCameraCommand stopCaptureCameraCommand = new StopCaptureCameraCommand();
                    stopCaptureCameraCommand.channel = cameraCaptureConfiguration.channel;
                    droneSession.addCommand(stopCaptureCameraCommand);
                    final ModeCameraCommand modeCameraCommand = new ModeCameraCommand();
                    modeCameraCommand.channel = cameraCaptureConfiguration.channel;
                    modeCameraCommand.mode = cameraCaptureConfiguration.captureType.getCameraMode();
                    droneSession.addCommand(modeCameraCommand);
                    if (modeCameraCommand.mode == CameraMode.PHOTO) {
                        final PhotoModeCameraCommand photoModeCameraCommand = new PhotoModeCameraCommand();
                        photoModeCameraCommand.channel = cameraCaptureConfiguration.channel;
                        photoModeCameraCommand.photoMode = CameraPhotoMode.INTERVAL;
                        droneSession.addCommand(photoModeCameraCommand);
                    }
                    final StartCaptureCameraCommand startCaptureCameraCommand = new StartCaptureCameraCommand();
                    startCaptureCameraCommand.channel = cameraCaptureConfiguration.channel;
                    droneSession.addCommand(startCaptureCameraCommand);
                } catch (final Dronelink.UnregisteredException | DroneSession.CommandTypeUnhandledException ignored) {
                }
            }
        }
    }

    private void stopCameraCaptureConfigurations(final CameraCaptureConfiguration[] cameraCaptureConfigurations) {
        if (cameraCaptureConfigurations != null) {
            for (final CameraCaptureConfiguration cameraCaptureConfiguration : cameraCaptureConfigurations) {
                final StopCaptureCameraCommand command = new StopCaptureCameraCommand();
                command.channel = cameraCaptureConfiguration.channel;
                try {
                    droneSession.addCommand(command);
                } catch (final Dronelink.UnregisteredException | DroneSession.CommandTypeUnhandledException ignored) {
                }
            }
        }
    }

    private void startCheckMissionFinishedOffline(final int attempt) {
        if (attempt > 20) {
            return;
        }

        //cannot immediately setup a listener because sometimes WaypointMissionState.DISCONNECTED.equals(djiWaypointMissionOperator.getCurrentState()) before the session is closed
        if (!droneSession.isClosed()) {
            Log.i(TAG, "Waiting to start checking if mission finished offline (" + attempt + ")");
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException ignored) {}
                    startCheckMissionFinishedOffline(attempt + 1);
                }
            });
            return;
        }

        Log.i(TAG, "Starting to check if mission finished offline (" + attempt + ")");
        droneSession.getManager().addListener(droneSessionManagerListener);
    }

    private void checkMissionFinishedOffline(final DroneSession session, final int attempt) {
        if (attempt > 20 || djiWaypointMissionOperator == null) {
            return;
        }

        Log.i(TAG, "Checking if mission finished offline (" + attempt + ")");

        final DJIExecutionState executionState = getExecutionState();
        if (executionState == null || executionState.status.completed || executionState.componentIndex >= djiWaypointMissions.length) {
            return;
        }

        final WaypointMission currentDJIWaypointMission = djiWaypointMissions[executionState.componentIndex];
        final WaypointMission loadedMission = djiWaypointMissionOperator.getLoadedMission();
        if (loadedMission == null || loadedMission.getMissionID() != currentDJIWaypointMission.getMissionID()) {
            return;
        }

        if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {
                WaypointMissionState.UNKNOWN,
                WaypointMissionState.DISCONNECTED,
                WaypointMissionState.RECOVERING
        })) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException ignored) {}
                    checkMissionFinishedOffline(session, attempt + 1);
                }
            });
            return;
        }

        if (WaypointMissionState.EXECUTING.equals(djiWaypointMissionOperator.getCurrentState())) {
            Log.i(TAG, "Attempting to engage mission in progress");
            try {
                missionExecutor.engage(session, new Executor.EngageDisallowed() {
                    @Override
                    public void disallowed(final Message reason) {
                        Log.e(TAG,"Mission engage failed: " + reason.toString());
                    }
                });
            } catch (final Dronelink.DroneSerialNumberUnavailableException ignored) {}
            return;
        }

        if (DronelinkDJI.isWaypointOperatorCurrentState(new WaypointMissionState[] {
                WaypointMissionState.READY_TO_UPLOAD,
                WaypointMissionState.READY_TO_EXECUTE
        })) {
            final int componentIndex = executionState.componentIndex + 1;
            if (componentIndex == kernelComponents.length) {
                updateExternalExecutionState(ExecutionStatus.SUCCEEDED);
            }
            else {
                updateExternalExecutionState(new HashMap<String, String>() {{
                    put("componentIndex", String.valueOf(componentIndex));
                    put("waypointIndex", "0");
                    put("waypointProgress", "0");
                }});
            }
        }
    }

    private final DroneSessionManager.Listener droneSessionManagerListener = new DroneSessionManager.Listener() {
        @Override
        public void onOpened(final DroneSession session) {
            //if the current mission is still loaded
            final MissionExecutor loadedMissionExecutor = Dronelink.getInstance().getMissionExecutor();
            if (loadedMissionExecutor != null && loadedMissionExecutor.id == missionExecutor.id) {
                checkMissionFinishedOffline(session, 0);
            }

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    session.getManager().removeListener(droneSessionManagerListener);
                }
            });
        }

        @Override
        public void onClosed(final DroneSession session) {}
    };
}
