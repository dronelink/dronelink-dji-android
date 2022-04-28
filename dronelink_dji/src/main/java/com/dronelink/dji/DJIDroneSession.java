//  DJIControlSession.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.dronelink.core.CameraFile;
import com.dronelink.core.Convert;
import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneControlSession;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.Dronelink;
import com.dronelink.core.Executor;
import com.dronelink.core.MissionExecutor;
import com.dronelink.core.ModeExecutor;
import com.dronelink.core.Version;
import com.dronelink.core.adapters.CameraAdapter;
import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.adapters.DroneAdapter;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.adapters.GimbalStateAdapter;
import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.command.Command;
import com.dronelink.core.command.CommandConfig;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.command.CommandQueue;
import com.dronelink.core.command.MultiChannelCommandQueue;
import com.dronelink.core.kernel.command.camera.AEBCountCameraCommand;
import com.dronelink.core.kernel.command.camera.ApertureCameraCommand;
import com.dronelink.core.kernel.command.camera.AutoExposureLockCameraCommand;
import com.dronelink.core.kernel.command.camera.AutoLockGimbalCameraCommand;
import com.dronelink.core.kernel.command.camera.CameraCommand;
import com.dronelink.core.kernel.command.camera.ColorCameraCommand;
import com.dronelink.core.kernel.command.camera.ContrastCameraCommand;
import com.dronelink.core.kernel.command.camera.DisplayModeCameraCommand;
import com.dronelink.core.kernel.command.camera.ExposureCompensationCameraCommand;
import com.dronelink.core.kernel.command.camera.ExposureCompensationStepCameraCommand;
import com.dronelink.core.kernel.command.camera.ExposureModeCameraCommand;
import com.dronelink.core.kernel.command.camera.FileIndexModeCameraCommand;
import com.dronelink.core.kernel.command.camera.FocusCameraCommand;
import com.dronelink.core.kernel.command.camera.FocusDistanceCameraCommand;
import com.dronelink.core.kernel.command.camera.FocusModeCameraCommand;
import com.dronelink.core.kernel.command.camera.FocusRingCameraCommand;
import com.dronelink.core.kernel.command.camera.ISOCameraCommand;
import com.dronelink.core.kernel.command.camera.MechanicalShutterCameraCommand;
import com.dronelink.core.kernel.command.camera.MeteringModeCameraCommand;
import com.dronelink.core.kernel.command.camera.ModeCameraCommand;
import com.dronelink.core.kernel.command.camera.PhotoAspectRatioCameraCommand;
import com.dronelink.core.kernel.command.camera.PhotoFileFormatCameraCommand;
import com.dronelink.core.kernel.command.camera.PhotoIntervalCameraCommand;
import com.dronelink.core.kernel.command.camera.PhotoModeCameraCommand;
import com.dronelink.core.kernel.command.camera.SaturationCameraCommand;
import com.dronelink.core.kernel.command.camera.SharpnessCameraCommand;
import com.dronelink.core.kernel.command.camera.ShutterSpeedCameraCommand;
import com.dronelink.core.kernel.command.camera.SpotMeteringTargetCameraCommand;
import com.dronelink.core.kernel.command.camera.StartCaptureCameraCommand;
import com.dronelink.core.kernel.command.camera.StopCaptureCameraCommand;
import com.dronelink.core.kernel.command.camera.StorageLocationCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoCaptionCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoFileCompressionStandardCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoFileFormatCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoModeCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoResolutionFrameRateCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoStandardCameraCommand;
import com.dronelink.core.kernel.command.camera.VideoStreamSourceCameraCommand;
import com.dronelink.core.kernel.command.camera.WhiteBalanceCustomCameraCommand;
import com.dronelink.core.kernel.command.camera.WhiteBalancePresetCameraCommand;
import com.dronelink.core.kernel.command.drone.AccessoryDroneCommand;
import com.dronelink.core.kernel.command.drone.BeaconDroneCommand;
import com.dronelink.core.kernel.command.drone.CollisionAvoidanceDroneCommand;
import com.dronelink.core.kernel.command.drone.ConnectionFailSafeBehaviorDroneCommand;
import com.dronelink.core.kernel.command.drone.DroneCommand;
import com.dronelink.core.kernel.command.drone.FlightAssistantDroneCommand;
import com.dronelink.core.kernel.command.drone.HomeLocationDroneCommand;
import com.dronelink.core.kernel.command.drone.LandingGearAutomaticMovementDroneCommand;
import com.dronelink.core.kernel.command.drone.LandingGearDeployDroneCommand;
import com.dronelink.core.kernel.command.drone.LandingGearDroneCommand;
import com.dronelink.core.kernel.command.drone.LandingGearRetractDroneCommand;
import com.dronelink.core.kernel.command.drone.LandingProtectionDroneCommand;
import com.dronelink.core.kernel.command.drone.LightbridgeChannelDroneCommand;
import com.dronelink.core.kernel.command.drone.LightbridgeChannelSelectionModeDroneCommand;
import com.dronelink.core.kernel.command.drone.LightbridgeDroneCommand;
import com.dronelink.core.kernel.command.drone.LightbridgeFrequencyBandDroneCommand;
import com.dronelink.core.kernel.command.drone.LowBatteryWarningThresholdDroneCommand;
import com.dronelink.core.kernel.command.drone.MaxAltitudeDroneCommand;
import com.dronelink.core.kernel.command.drone.MaxDistanceDroneCommand;
import com.dronelink.core.kernel.command.drone.MaxDistanceLimitationDroneCommand;
import com.dronelink.core.kernel.command.drone.OcuSyncChannelDroneCommand;
import com.dronelink.core.kernel.command.drone.OcuSyncChannelSelectionModeDroneCommand;
import com.dronelink.core.kernel.command.drone.OcuSyncDroneCommand;
import com.dronelink.core.kernel.command.drone.OcuSyncFrequencyBandDroneCommand;
import com.dronelink.core.kernel.command.drone.OcuSyncVideoFeedSourcesDroneCommand;
import com.dronelink.core.kernel.command.drone.PrecisionLandingDroneCommand;
import com.dronelink.core.kernel.command.drone.ReturnHomeAltitudeDroneCommand;
import com.dronelink.core.kernel.command.drone.ReturnHomeObstacleAvoidanceDroneCommand;
import com.dronelink.core.kernel.command.drone.ReturnHomeRemoteObstacleAvoidanceDroneCommand;
import com.dronelink.core.kernel.command.drone.SeriousLowBatteryWarningThresholdDroneCommand;
import com.dronelink.core.kernel.command.drone.SmartReturnHomeDroneCommand;
import com.dronelink.core.kernel.command.drone.SpotlightBrightnessDroneCommand;
import com.dronelink.core.kernel.command.drone.SpotlightDroneCommand;
import com.dronelink.core.kernel.command.drone.UpwardsAvoidanceDroneCommand;
import com.dronelink.core.kernel.command.drone.VisionAssistedPositioningDroneCommand;
import com.dronelink.core.kernel.command.gimbal.GimbalCommand;
import com.dronelink.core.kernel.command.gimbal.ModeGimbalCommand;
import com.dronelink.core.kernel.command.gimbal.OrientationGimbalCommand;
import com.dronelink.core.kernel.command.gimbal.YawSimultaneousFollowGimbalCommand;
import com.dronelink.core.kernel.command.remotecontroller.RemoteControllerCommand;
import com.dronelink.core.kernel.command.remotecontroller.TargetGimbalChannelRemoteControllerCommand;
import com.dronelink.core.kernel.core.CameraFocusCalibration;
import com.dronelink.core.kernel.core.GeoCoordinate;
import com.dronelink.core.kernel.core.Message;
import com.dronelink.core.kernel.core.Orientation3;
import com.dronelink.core.kernel.core.Orientation3Optional;
import com.dronelink.core.kernel.core.enums.CameraMode;
import com.dronelink.core.kernel.core.enums.ExecutionEngine;
import com.dronelink.core.kernel.core.enums.GimbalMode;
import com.dronelink.dji.adapters.DJICameraStateAdapter;
import com.dronelink.dji.adapters.DJIDroneAdapter;
import com.dronelink.dji.adapters.DJIDroneStateAdapter;
import com.dronelink.dji.adapters.DJIGimbalAdapter;
import com.dronelink.dji.adapters.DJIGimbalStateAdapter;
import com.dronelink.dji.adapters.DJIRemoteControllerStateAdapter;

import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dji.common.airlink.ChannelSelectionMode;
import dji.common.airlink.LightbridgeFrequencyBand;
import dji.common.airlink.OcuSyncFrequencyBand;
import dji.common.airlink.PhysicalSource;
import dji.common.battery.BatteryState;
import dji.common.camera.CameraVideoStreamSource;
import dji.common.camera.ExposureSettings;
import dji.common.camera.FocusState;
import dji.common.camera.ResolutionAndFrameRate;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.camera.SystemState;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.flightcontroller.LandingGearState;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.VisionSensorPosition;
import dji.common.gimbal.GimbalState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.product.Model;
import dji.common.remotecontroller.HardwareState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.AirLinkKey;
import dji.keysdk.CameraKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.RemoteControllerKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.airlink.AirLink;
import dji.sdk.airlink.LightbridgeLink;
import dji.sdk.airlink.OcuSyncLink;
import dji.sdk.base.BaseComponent;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.camera.Lens;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.LandingGear;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.MediaFile;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;

public class DJIDroneSession implements DroneSession, VideoFeeder.PhysicalSourceListener {
    private static final String TAG = DJIDroneSession.class.getCanonicalName();

    private final Context context;
    private final DroneSessionManager manager;
    private final DJIDroneAdapter adapter;

    private final Date opened = new Date();
    private boolean closed = false;
    public boolean isClosed() {
        return closed;
    }

    private final List<Listener> listeners = new LinkedList<>();
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private final CommandQueue droneCommands = new CommandQueue();
    private final MultiChannelCommandQueue remoteControllerCommands = new MultiChannelCommandQueue();
    private final MultiChannelCommandQueue cameraCommands = new MultiChannelCommandQueue();
    private final MultiChannelCommandQueue gimbalCommands = new MultiChannelCommandQueue();

    private final ExecutorService stateSerialQueue = Executors.newSingleThreadExecutor();
    private final DJIDroneStateAdapter state = new DJIDroneStateAdapter();

    private final ExecutorService remoteControllerSerialQueue = Executors.newSingleThreadExecutor();
    private Date remoteControllerInitialized;
    private DatedValue<HardwareState> remoteControllerState;

    private final ExecutorService cameraSerialQueue = Executors.newSingleThreadExecutor();
    private final SparseArray<DatedValue<SystemState>> cameraStates = new SparseArray<>();
    private final SparseArray<DatedValue<CameraVideoStreamSource>> cameraVideoStreamSources = new SparseArray<>();
    private final Map<String, DatedValue<FocusState>> cameraFocusStates = new HashMap<>();
    private final SparseArray<DatedValue<StorageState>> cameraStorageStates = new SparseArray<>();
    private final Map<String, DatedValue<ExposureSettings>> cameraExposureSettings = new HashMap<>();
    private final SparseArray<DatedValue<SettingsDefinitions.ExposureCompensation>> cameraExposureCompensation = new SparseArray<>();
    private final SparseArray<DatedValue<String>> cameraLensInformation = new SparseArray<>();

    private final ExecutorService gimbalSerialQueue = Executors.newSingleThreadExecutor();
    private final SparseArray<DatedValue<GimbalState>> gimbalStates = new SparseArray<>();

    private DatedValue<Double> focusRingValue;
    private DatedValue<Double> focusRingMax;

    private DatedValue<CameraFile> mostRecentCameraFile;
    public DatedValue<CameraFile> getMostRecentCameraFile() {
        return mostRecentCameraFile;
    }

    private KeyListener airlinkListener;
    private KeyListener focusRingValueListener;
    private KeyListener focusRingMaxListener;
    private KeyListener lowBatteryWarningThresholdListener;
    private KeyListener remoteControllerGimbalChannelListener;

    public DJIDroneSession(final Context context, final DroneSessionManager manager, final Aircraft drone) {
        this.context = context;
        this.manager = manager;
        this.adapter = new DJIDroneAdapter(drone);
        initDrone();

        new Thread() {
            @Override
            public void run() {
                try {
                    while (!closed) {
                        if (!state.initialized && state.serialNumber != null && state.name != null && state.model != null && state.firmwarePackageVersion != null) {
                            state.initialized = true;
                            onInitialized();
                        }

                        final Location location = state.getLocation();
                        if (state.getLocation() != null) {
                            if (!state.located) {
                                state.located = true;
                                onLocated();
                            }

                            if (!state.isFlying()) {
                                state.lastKnownGroundLocation = location;
                            }
                        }

                        if (!state.initVirtualStickDisabled) {
                            final FlightController flightController = drone.getFlightController();
                            final DatedValue<FlightControllerState> flightControllerState = state.flightControllerState;
                            if (flightController != null && flightControllerState != null && flightControllerState.value != null) {
                                state.initVirtualStickDisabled = true;
                                if (flightControllerState.value.getFlightMode() != FlightMode.GPS_WAYPOINT) {
                                    flightController.getVirtualStickModeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                                        @Override
                                        public void onSuccess(final Boolean enabled) {
                                            if (enabled) {
                                                flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onResult(final DJIError djiError) {
                                                        if (djiError == null) {
                                                            Log.i(TAG, "Flight controller virtual stick deactivated");
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailure(final DJIError djiError) {}
                                    });
                                }
                            }
                        }

                        if (remoteControllerInitialized == null) {
                            final RemoteController remoteController = adapter.getDrone().getRemoteController();
                            if (remoteController != null) {
                                initRemoteController(remoteController);
                            }
                        }

                        droneCommands.process();
                        remoteControllerCommands.process();
                        cameraCommands.process();
                        gimbalCommands.process();

                        final MissionExecutor missionExecutor = Dronelink.getInstance().getMissionExecutor();
                        final ModeExecutor modeExecutor = Dronelink.getInstance().getModeExecutor();
                        final boolean missionExecutorEngaged = (missionExecutor != null && missionExecutor.isEngaged());
                        if (missionExecutorEngaged || (modeExecutor != null && modeExecutor.isEngaged())) {
                            gimbalSerialQueue.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //work-around for this issue: https://support.dronelink.com/hc/en-us/community/posts/360034749773-Seeming-to-have-a-Heading-error-
                                    for (final GimbalAdapter gimbalAdapter : adapter.getGimbals()) {
                                        //don't issue competing speed rotations, OrientationGimbalCommand always takes precedent
                                        final CommandQueue queue = gimbalCommands.get(gimbalAdapter.getIndex());
                                        if (queue != null) {
                                            final Command currentCommand = queue.getCurrentCommand();
                                            if (currentCommand != null && currentCommand.kernelCommand instanceof OrientationGimbalCommand) {
                                                return;
                                            }
                                        }

                                        if (gimbalAdapter instanceof DJIGimbalAdapter) {
                                            final DJIGimbalAdapter djiGimbalAdapter = (DJIGimbalAdapter) gimbalAdapter;
                                            Rotation.Builder rotationBuilder = djiGimbalAdapter.getPendingSpeedRotation();
                                            djiGimbalAdapter.setPendingSpeedRotationBuilder(null);
                                            final DatedValue<GimbalState> gimbalState = gimbalStates.get(djiGimbalAdapter.getIndex());
                                            if (gimbalState != null) {
                                                Double gimbalYawRelativeToAircraftHeadingCorrected = gimbalYawRelativeToAircraftHeadingCorrected(gimbalState.value);
                                                if (gimbalYawRelativeToAircraftHeadingCorrected != null) {
                                                    if (rotationBuilder == null) {
                                                        rotationBuilder = new Rotation.Builder();
                                                        rotationBuilder.mode(RotationMode.SPEED);
                                                    }

                                                    rotationBuilder.yaw((float) Math.min(Math.max(-Convert.RadiansToDegrees(gimbalYawRelativeToAircraftHeadingCorrected) * 0.25, -25.0), 25.0));
                                                }
                                            }

                                            if (missionExecutorEngaged && DronelinkDJI.isAdjustPitchSupported(djiGimbalAdapter.gimbal)) {
                                                final DatedValue<Integer> remoteControllerGimbalChannel = state.remoteControllerGimbalChannel;
                                                final int channel = remoteControllerGimbalChannel == null || remoteControllerGimbalChannel.value == null ? 0 : remoteControllerGimbalChannel.value;
                                                if (channel == gimbalAdapter.getIndex()) {
                                                    final DatedValue<RemoteControllerStateAdapter> remoteControllerState = getRemoteControllerState(channel);
                                                    if (remoteControllerState != null && remoteControllerState.value != null && remoteControllerState.value.getLeftWheel().value != 0) {
                                                        if (rotationBuilder == null) {
                                                            rotationBuilder = new Rotation.Builder();
                                                            rotationBuilder.mode(RotationMode.SPEED);
                                                        }

                                                        rotationBuilder.pitch((int) (remoteControllerState.value.getLeftWheel().value * 10));
                                                    }
                                                }
                                            }

                                            if (rotationBuilder != null) {
                                                djiGimbalAdapter.gimbal.rotate(rotationBuilder.build(), null);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        sleep(100);
                    }

                    DJISDKManager.getInstance().getKeyManager().removeListener(airlinkListener);
                    DJISDKManager.getInstance().getKeyManager().removeListener(focusRingValueListener);
                    DJISDKManager.getInstance().getKeyManager().removeListener(focusRingMaxListener);
                    DJISDKManager.getInstance().getKeyManager().removeListener(lowBatteryWarningThresholdListener);
                    DJISDKManager.getInstance().getKeyManager().removeListener(remoteControllerGimbalChannelListener);
                    Log.i(TAG, "Drone session closed");
                }
                catch (final InterruptedException e) {}
            }
        }.start();
    }

    private Double gimbalYawRelativeToAircraftHeadingCorrected(final GimbalState gimbalState) {
        final Aircraft drone = adapter.getDrone();
        if (drone != null && drone.getModel() != null) {
            switch (drone.getModel()) {
                case PHANTOM_4:
                case PHANTOM_4_PRO:
                case PHANTOM_4_PRO_V2:
                case PHANTOM_4_ADVANCED:
                case PHANTOM_4_RTK:
                    return Convert.AngleDifferenceSigned(Convert.DegreesToRadians(gimbalState.getAttitudeInDegrees().getYaw()), state.getOrientation().getYaw());

                default:
                    break;
            }
        }

        return null;
    }

    private void initDrone() {
        Log.i(TAG, "Drone session opened");

        VideoFeeder.getInstance().addPhysicalSourceListener(this);

        final Aircraft drone = adapter.getDrone();
        if (drone.getFlightController() != null) {
            initFlightController(adapter.getDrone().getFlightController());
        }

        final List<Camera> cameras = drone.getCameras();
        if (cameras != null) {
            for (final Camera camera : cameras) {
                initCamera(camera);
            }
        }

        final List<Gimbal> gimbals = drone.getGimbals();
        if (gimbals != null) {
            for (final Gimbal gimbal : gimbals) {
                initGimbal(gimbal);
            }
        }

        final RemoteController remoteController = drone.getRemoteController();
        if (remoteController != null) {
            initRemoteController(remoteController);
        }
    }

    private void initRemoteController(final RemoteController remoteController) {
        remoteControllerInitialized = new Date();
        remoteController.setHardwareStateCallback(new HardwareState.HardwareStateCallback() {
            @Override
            public void onUpdate(@NonNull final HardwareState hardwareState) {
                remoteControllerSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        remoteControllerState = new DatedValue<>(hardwareState);
                    }
                });
            }
        });
    }

    private void initFlightController(final FlightController flightController) {
        Log.i(TAG, "Flight controller connected");

        final Aircraft drone = adapter.getDrone();
        final Model model = drone.getModel();
        if (model == null) {
            state.model = "";
        }
        else {
            state.model = drone.getModel().getDisplayName();
            if (state.model == null) {
                state.model = "";
            } else {
                Log.i(TAG, "Model: " + state.model);
            }
        }

        state.firmwarePackageVersion = drone.getFirmwarePackageVersion();
        if (state.firmwarePackageVersion != null) {
            Log.i(TAG, "Firmware package version: " + state.firmwarePackageVersion);
        }

        drone.getName(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(final String s) {
                state.name = s;
                if (state.name == null) {
                    state.name = "";
                }
                else {
                    Log.i(TAG, "Name: " + s);
                }
            }

            @Override
            public void onFailure(final DJIError djiError) {

            }
        });

        initSerialNumber(flightController, 0);

        flightController.getMultipleFlightModeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(final Boolean enabled) {
                if (!enabled) {
                    flightController.setMultipleFlightModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(final DJIError djiError) {
                            if (djiError == null) {
                                Log.i(TAG, "Flight controller multiple flight mode enabled");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(final DJIError djiError) {}
        });

        flightController.getNoviceModeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(final Boolean enabled) {
                if (enabled) {
                    flightController.setNoviceModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(final DJIError djiError) {
                            if (djiError == null) {
                                Log.i(TAG, "Flight controller novice mode disabled");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(final DJIError djiError) {}
        });

        flightController.setStateCallback(new FlightControllerState.Callback() {
            private Double lastNonZeroFlyingAltitude = null;
            private boolean isFlyingPrevious = false;
            private boolean areMotorsOnPrevious = false;

            @Override
            public void onUpdate(@NonNull final FlightControllerState flightControllerStateUpdated) {
                stateSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFlyingPrevious && !flightControllerStateUpdated.isFlying()) {
                            if (Dronelink.getInstance().droneOffsets.droneAltitudeContinuity) {
                                //automatically adjust the drone altitude offset if:
                                //1) altitude continuity is enabled
                                //2) the drone is going from flying to not flying
                                //3) the altitude reference is ground level
                                //4) the current drone altitude offset is not zero
                                //5) the last flight altitude is available
                                //6) the absolute value of last non-zero flying altitude is more than 1m
                                if ((Dronelink.getInstance().droneOffsets.droneAltitudeReference == null || Dronelink.getInstance().droneOffsets.droneAltitudeReference == 0) &&
                                        lastNonZeroFlyingAltitude != null && Math.abs(lastNonZeroFlyingAltitude) > 1) {
                                    //adjust by the last non-zero flying altitude
                                    Dronelink.getInstance().droneOffsets.droneAltitude -= lastNonZeroFlyingAltitude;
                                }
                            } else {
                                Dronelink.getInstance().droneOffsets.droneAltitude = 0;
                            }
                        }

                        state.flightControllerState = new DatedValue<>(flightControllerStateUpdated);
                        if (areMotorsOnPrevious != flightControllerStateUpdated.areMotorsOn()) {
                            onMotorsChanged(flightControllerStateUpdated.areMotorsOn());
                        }

                        isFlyingPrevious = flightControllerStateUpdated.isFlying();
                        areMotorsOnPrevious = flightControllerStateUpdated.areMotorsOn();

                        if (flightControllerStateUpdated.isFlying()) {
                            if (flightControllerStateUpdated.getAircraftLocation().getAltitude() != 0) {
                                lastNonZeroFlyingAltitude = (double)flightControllerStateUpdated.getAircraftLocation().getAltitude();
                            }
                        }
                        else {
                            lastNonZeroFlyingAltitude = null;
                        }
                    }
                });
            }
        });

        final Battery battery = drone.getBattery();
        if (battery != null) {
            battery.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(final BatteryState batteryState) {
                    stateSerialQueue.execute(new Runnable() {
                        @Override
                        public void run() {
                            state.batteryState = new DatedValue<>(batteryState);
                        }
                    });
                }
            });
        }

        final FlightAssistant flightAssistant = flightController.getFlightAssistant();
        if (flightAssistant != null) {
            flightAssistant.setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback() {
                @Override
                public void onUpdate(@NonNull final VisionDetectionState visionDetectionState) {
                    if (visionDetectionState.getPosition() == VisionSensorPosition.NOSE) {
                        stateSerialQueue.execute(new Runnable() {
                            @Override
                            public void run() {
                                state.visionDetectionState = new DatedValue<>(visionDetectionState);
                            }
                        });
                    }
                }
            });
        }

        airlinkListener = new KeyListener() {
            @Override
            public void onValueChange(final Object oldValue, final Object newValue) {
                stateSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (newValue != null && newValue instanceof Integer) {
                            state.airLinkSignalQuality = new DatedValue<>((Integer) newValue);
                        }
                        else {
                            state.airLinkSignalQuality = null;
                        }
                    }
                });
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(AirLinkKey.create(AirLinkKey.DOWNLINK_SIGNAL_QUALITY), airlinkListener);

        focusRingValueListener = new KeyListener() {
            @Override
            public void onValueChange(final Object oldValue, final Object newValue) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (newValue != null && newValue instanceof Integer) {
                            focusRingValue = new DatedValue<>(((Integer) newValue).doubleValue());
                        }
                        else {
                            focusRingValue = null;
                        }
                    }
                });
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(CameraKey.create(CameraKey.FOCUS_RING_VALUE), focusRingValueListener);
        DJISDKManager.getInstance().getKeyManager().getValue(CameraKey.create(CameraKey.FOCUS_RING_VALUE), new GetCallback() {
            @Override
            public void onSuccess(@NonNull final Object newValue) {
                if (newValue != null && newValue instanceof Integer) {
                    focusRingValue = new DatedValue<>(((Integer) newValue).doubleValue());
                }
            }

            @Override
            public void onFailure(@NonNull final DJIError djiError) {}
        });

        focusRingMaxListener = new KeyListener() {
            @Override
            public void onValueChange(final Object oldValue, final Object newValue) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (newValue != null && newValue instanceof Integer) {
                            focusRingMax = new DatedValue<>(((Integer) newValue).doubleValue());
                        }
                        else {
                            focusRingMax = null;
                        }
                    }
                });
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(CameraKey.create(CameraKey.FOCUS_RING_VALUE_UPPER_BOUND), focusRingMaxListener);
        DJISDKManager.getInstance().getKeyManager().getValue(CameraKey.create(CameraKey.FOCUS_RING_VALUE_UPPER_BOUND), new GetCallback() {
            @Override
            public void onSuccess(@NonNull final Object newValue) {
                if (newValue != null && newValue instanceof Integer) {
                    focusRingMax = new DatedValue<>(((Integer) newValue).doubleValue());
                }
            }

            @Override
            public void onFailure(@NonNull final DJIError djiError) {}
        });


        lowBatteryWarningThresholdListener = new KeyListener() {
            @Override
            public void onValueChange(final Object oldValue, final Object newValue) {
                stateSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (newValue != null && newValue instanceof Integer)
                            state.lowBatteryWarningThreshold = new DatedValue<>((Integer) newValue);
                        else if (oldValue != null && oldValue instanceof Integer)
                            state.lowBatteryWarningThreshold = new DatedValue<>((Integer) oldValue);
                        else
                            state.lowBatteryWarningThreshold = null;
                    }
                });
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(FlightControllerKey.create(FlightControllerKey.LOW_BATTERY_WARNING_THRESHOLD), lowBatteryWarningThresholdListener);
        DJISDKManager.getInstance().getKeyManager().getValue(FlightControllerKey.create(FlightControllerKey.LOW_BATTERY_WARNING_THRESHOLD), new GetCallback() {
            @Override
            public void onSuccess(@NonNull final Object newValue) {
                if (newValue != null && newValue instanceof Integer) {
                    state.lowBatteryWarningThreshold = new DatedValue<>((Integer) newValue);
                }
            }

            @Override
            public void onFailure(@NonNull final DJIError djiError) {}
        });

        remoteControllerGimbalChannelListener = new KeyListener() {
            @Override
            public void onValueChange(final Object oldValue, final Object newValue) {
                if (newValue != null && newValue instanceof Integer) {
                    state.remoteControllerGimbalChannel = new DatedValue<>((Integer) newValue);
                }
                else {
                    state.remoteControllerGimbalChannel = null;
                }
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(RemoteControllerKey.create(RemoteControllerKey.CONTROLLING_GIMBAL_INDEX), remoteControllerGimbalChannelListener);
        DJISDKManager.getInstance().getKeyManager().getValue(RemoteControllerKey.create(RemoteControllerKey.CONTROLLING_GIMBAL_INDEX), new GetCallback() {
            @Override
            public void onSuccess(@NonNull final Object newValue) {
                if (newValue != null && newValue instanceof Integer) {
                    state.remoteControllerGimbalChannel = new DatedValue<>((Integer) newValue);
                }
                else {
                    state.remoteControllerGimbalChannel = null;
                }
            }

            @Override
            public void onFailure(@NonNull final DJIError djiError) {}
        });
    }

    private void initSerialNumber(final FlightController flightController, final int attempt) {
        if (attempt < 3) {
            flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(final String s) {
                    state.serialNumber = s;
                    if (state.serialNumber != null) {
                        Log.i(TAG, "Serial number: " + s);
                    }

                    //doing this a second time because sometimes it isn't ready by the above line
                    if (state.firmwarePackageVersion == null) {
                        state.firmwarePackageVersion = adapter.getDrone().getFirmwarePackageVersion();
                        if (state.firmwarePackageVersion == null) {
                            state.firmwarePackageVersion = "";
                        } else {
                            Log.i(TAG, "Firmware package version: " + state.firmwarePackageVersion);
                        }
                    }
                }

                @Override
                public void onFailure(final DJIError djiError) {
                    Log.e(TAG, "Serial number failed: " + (djiError == null ? "??" : djiError.getDescription()));
                    initSerialNumber(flightController, attempt + 1);
                }
            });
        }
    }

    private void initCamera(final Camera camera) {
        Log.i(TAG, String.format("Camera[%d] connected: %s", camera.getIndex(), camera.getDisplayName() == null ? "unknown" : camera.getDisplayName()));
        camera.setSystemStateCallback(new SystemState.Callback() {
            @Override
            public void onUpdate(@NonNull final SystemState systemState) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraStates.put(camera.getIndex(), new DatedValue<>(systemState));
                    }
                });
            }
        });

        camera.setCameraVideoStreamSourceCallback(new CameraVideoStreamSource.Callback() {
            @Override
            public void onUpdate(final CameraVideoStreamSource cameraVideoStreamSource) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraVideoStreamSources.put(camera.getIndex(), new DatedValue<>(cameraVideoStreamSource));
                    }
                });
            }
        });

        //pumping this one time because camera.setCameraVideoStreamSourceCallback only gets called on changes
        camera.getCameraVideoStreamSource(new CommonCallbacks.CompletionCallbackWith<CameraVideoStreamSource>() {
            @Override
            public void onSuccess(final CameraVideoStreamSource cameraVideoStreamSource) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraVideoStreamSources.put(camera.getIndex(), new DatedValue<>(cameraVideoStreamSource));
                    }
                });
            }

            @Override
            public void onFailure(final DJIError djiError) {}
        });

        camera.setFocusStateCallback(new FocusState.Callback() {
            @Override
            public void onUpdate(final FocusState focusState) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraFocusStates.put(camera.getIndex() + ".0", new DatedValue<>(focusState));
                    }
                });
            }
        });

        camera.setStorageStateCallBack(new StorageState.Callback() {
            @Override
            public void onUpdate(final StorageState storageState) {
                if (storageState.getStorageLocation() == null || storageState.getStorageLocation() == SettingsDefinitions.StorageLocation.SDCARD) {
                    cameraSerialQueue.execute(new Runnable() {
                        @Override
                        public void run() {
                            cameraStorageStates.put(camera.getIndex(), new DatedValue<>(storageState));
                        }
                    });
                }
            }
        });

        camera.setExposureSettingsCallback(new ExposureSettings.Callback() {
            @Override
            public void onUpdate(@NonNull final ExposureSettings exposureSettings) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraExposureSettings.put(camera.getIndex() + ".0", new DatedValue<>(exposureSettings));

                        //KLUGE: the phantom 4 appears to be lying to us about the ev!
                        final String cameraName = camera.getDisplayName();
                        if (cameraName != null && (cameraName.toLowerCase().contains("phantom 4") || cameraName.toLowerCase().contains("p4"))) {
                            camera.getExposureCompensation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureCompensation>() {
                                @Override
                                public void onSuccess(final SettingsDefinitions.ExposureCompensation exposureCompensation) {
                                    cameraSerialQueue.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraExposureCompensation.put(camera.getIndex(), new DatedValue<>(exposureCompensation));
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(final DJIError djiError) {}
                            });
                        }
                    }
                });
            }
        });

        camera.setMediaFileCallback(new MediaFile.Callback() {
            @Override
            public void onNewFile(@NonNull final MediaFile mediaFile) {
                listenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Orientation3 orientation = state.getOrientation();
                        final DatedValue<GimbalStateAdapter> gimbalState = getGimbalState(camera.getIndex());
                        if (gimbalState != null) {
                            orientation.x = gimbalState.value.getOrientation().x;
                            orientation.y = gimbalState.value.getOrientation().y;
                            if (gimbalState.value.getMode() == GimbalMode.FREE) {
                                orientation.z = gimbalState.value.getOrientation().z;
                            }
                        } else {
                            orientation.x = 0.0;
                            orientation.y = 0.0;
                        }

                        final DatedValue<DroneStateAdapter> state = getState();
                        final DJICameraFile cameraFile = new DJICameraFile(camera.getIndex(), mediaFile, state.value.getLocation(), state.value.getAltitude(), orientation);
                        mostRecentCameraFile = new DatedValue<CameraFile>(cameraFile);
                        onCameraFileGenerated(cameraFile);
                    }
                });
            }
        });

        String kernelVersionDisplay = "";
        final Version kernelVersion = Dronelink.getInstance().getKernelVersion();
        if (kernelVersion != null) {
            kernelVersionDisplay = kernelVersion.toString();
        }
        final String xmp = "dronelink:" + kernelVersionDisplay;

        camera.setMediaFileCustomInformation(xmp, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (djiError == null) {
                    Log.i(TAG, "Set media file custom information: " + xmp);
                }
                else {
                    Log.i(TAG, "Unable to set media file custom information: " + djiError.getDescription());
                }
            }
        });

        camera.getLensInformation(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(final String info) {
                cameraSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraLensInformation.put(camera.getIndex(), new DatedValue<>(info));
                    }
                });
            }

            @Override
            public void onFailure(final DJIError djiError) {}
        });

        if (camera.getLenses() != null) {
            for (final Lens lens : camera.getLenses()) {
                lens.setFocusStateCallback(new FocusState.Callback() {
                    @Override
                    public void onUpdate(@NonNull final FocusState focusState) {
                        cameraSerialQueue.execute(new Runnable() {
                            @Override
                            public void run() {
                                cameraFocusStates.put(camera.getIndex() + "." + lens.getIndex(), new DatedValue<>(focusState));
                            }
                        });
                    }
                });

                lens.setExposureSettingsCallback(new ExposureSettings.Callback() {
                    @Override
                    public void onUpdate(@NonNull final ExposureSettings exposureSettings) {
                        cameraSerialQueue.execute(new Runnable() {
                            @Override
                            public void run() {
                                cameraExposureSettings.put(camera.getIndex() + "." + lens.getIndex(), new DatedValue<>(exposureSettings));
                            }
                        });
                    }
                });

                if (lens.isHybridZoomSupported()) {
                    initLensHybridZoom(camera, lens, 0);
                }
            }
        }
    }

    private void initLensHybridZoom(final Camera camera, final Lens lens, final int attempt) {
        if (attempt >= 3) {
            Log.e(TAG, "Unable to set lens hybrid zoom: no specification found!");
            return;
        }

        lens.getHybridZoomSpec(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.HybridZoomSpec>() {
            @Override
            public void onSuccess(final SettingsDefinitions.HybridZoomSpec hybridZoomSpec) {
                int focalLength = hybridZoomSpec.getMinHybridFocalLength();
                if (camera.getDisplayName().contains("H20")) {
                    focalLength = 470;
                }

                final int focalLengthFinal = focalLength;
                lens.setHybridZoomFocalLength(focalLengthFinal, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (djiError != null) {
                            Log.e(TAG, "Unable to set lens hybrid zoom: " + djiError.getDescription());
                            return;
                        }

                        Log.i(TAG, "Set lens hybrid zoom: " + focalLengthFinal);
                    }
                });
            }

            @Override
            public void onFailure(final DJIError djiError) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initLensHybridZoom(camera, lens, attempt + 1);
                    }
                }, attempt * 1000);
            }
        });
    }

    private void initGimbal(final Gimbal gimbal) {
        Log.i(TAG, String.format("Gimbal[%d] connected", gimbal.getIndex()));
        gimbal.setStateCallback(new GimbalState.Callback() {
            @Override
            public void onUpdate(@NonNull final GimbalState gimbalState) {
                gimbalSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        gimbalStates.put(gimbal.getIndex(), new DatedValue<>(gimbalState));
                    }
                });
            }
        });

        gimbal.getPitchRangeExtensionEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(final Boolean enabled) {
                if (!enabled) {
                    gimbal.setPitchRangeExtensionEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(final DJIError djiError) {
                            if (djiError == null) {
                                Log.i(TAG, String.format("Gimbal[%d] pitch range extension enabled", gimbal.getIndex()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(final DJIError djiError) {}
        });
    }

    public void componentConnected(final BaseComponent component) {
        if (component instanceof FlightController) {
            initFlightController((FlightController)component);
        }
        else if (component instanceof Camera) {
            initCamera((Camera)component);
        }
        else if (component instanceof Gimbal) {
            initGimbal((Gimbal)component);
        }
        else if (component instanceof RemoteController) {
            initRemoteController((RemoteController)component);
        }
    }

    public void componentDisconnected(final BaseComponent component) {
        if (component instanceof FlightController) {
            Log.i(TAG, "Flight controller disconnected");
            stateSerialQueue.execute(new Runnable() {
                @Override
                public void run() {
                    state.flightControllerState = null;
                    state.visionDetectionState = null;
                }
            });
        }
        else if (component instanceof Camera) {
            final Camera camera = (Camera)component;
            cameraSerialQueue.execute(new Runnable() {
                @Override
                public void run() {
                    cameraStates.put(camera.getIndex(), null);
                    cameraVideoStreamSources.put(camera.getIndex(), null);
                    for (final String key : cameraFocusStates.keySet().toArray(new String[]{})) {
                        if (key.startsWith(camera.getIndex() + ".")) {
                            cameraFocusStates.remove(key);
                        }
                    }
                    cameraStorageStates.put(camera.getIndex(), null);
                    for (final String key : cameraExposureSettings.keySet().toArray(new String[]{})) {
                        if (key.startsWith(camera.getIndex() + ".")) {
                            cameraExposureSettings.remove(key);
                        }
                    }
                    cameraExposureCompensation.put(camera.getIndex(), null);
                    cameraLensInformation.put(camera.getIndex(), null);
                }
            });
            Log.i(TAG, String.format("Camera[%d] disconnected", camera.getIndex()));
        }
        else if (component instanceof Gimbal) {
            final Gimbal gimbal = (Gimbal)component;
            gimbalSerialQueue.execute(new Runnable() {
                @Override
                public void run() {
                    gimbalStates.put(gimbal.getIndex(), null);
                }
            });
            Log.i(TAG, String.format("Gimbal[%d] disconnected", gimbal.getIndex()));
        }
    }

    public DJIDroneAdapter getAdapter() {
        return adapter;
    }

    @Override
    public DroneSessionManager getManager() {
        return manager;
    }

    @Override
    public DroneAdapter getDrone() {
        return adapter;
    }

    @Override
    public DatedValue<DroneStateAdapter> getState() {
        try {
            return stateSerialQueue.submit(new Callable<DatedValue<DroneStateAdapter>>() {
                @Override
                public DatedValue<DroneStateAdapter> call() {
                    return state.toDatedValue();
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public DatedValue<FlightControllerState> getFlightControllerState() {
        try {
            return stateSerialQueue.submit(new Callable<DatedValue<FlightControllerState>>() {
                @Override
                public DatedValue<FlightControllerState> call() {
                    return state.flightControllerState;
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    @Override
    public Date getOpened() {
        return opened;
    }

    @Override
    public String getId() {
        return state.id;
    }

    @Override
    public String getManufacturer() {
        return "DJI";
    }

    @Override
    public String getSerialNumber() {
        return state.serialNumber;
    }

    @Override
    public String getName() {
        return state.name;
    }

    @Override
    public String getModel() {
        return state.model;
    }

    @Override
    public String getFirmwarePackageVersion() {
        return state.firmwarePackageVersion;
    }

    @Override
    public boolean isInitialized() {
        return state.initialized;
    }

    @Override
    public boolean isLocated() {
        return state.located;
    }

    @Override
    public boolean isTelemetryDelayed() {
        return System.currentTimeMillis() - getState().date.getTime() > 2000;
    }

    @Override
    public Message getDisengageReason() {
        if (closed) {
            return new Message(context.getString(R.string.MissionDisengageReason_drone_disconnected_title));
        }

        if (adapter.getDrone().getFlightController() == null) {
            return new Message(context.getString(R.string.MissionDisengageReason_drone_control_unavailable_title));
        }

        final DatedValue<FlightControllerState> flightControllerState = state.flightControllerState;
        if (flightControllerState == null || flightControllerState.value == null) {
            return new Message(context.getString(R.string.MissionDisengageReason_telemetry_unavailable_title));
        }

        if (isTelemetryDelayed()) {
            return new Message(context.getString(R.string.MissionDisengageReason_telemetry_delayed_title));
        }

        if (flightControllerState.value.hasReachedMaxFlightHeight()) {
            return new Message(context.getString(R.string.MissionDisengageReason_drone_max_altitude_title), context.getString(R.string.MissionDisengageReason_drone_max_altitude_details));
        }

        if (flightControllerState.value.hasReachedMaxFlightRadius()) {
            return new Message(context.getString(R.string.MissionDisengageReason_drone_max_distance_title), context.getString(R.string.MissionDisengageReason_drone_max_distance_details));
        }

        return null;
    }

    @Override
    public void identify(final String id) {
        state.id = id;
    }

    @Override
    public void addListener(final Listener listener) {
        final DroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                listeners.add(listener);

                if (state.initialized) {
                    listener.onInitialized(self);
                }

                if (state.located) {
                    listener.onLocated(self);
                }
            }
        });
    }

    @Override
    public void removeListener(final Listener listener) {
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                listeners.remove(listener);
            }
        });
    }

    private void onInitialized() {
        for (final Listener listener : listeners) {
            listener.onInitialized(this);
        }
    }

    private void onLocated() {
        final DJIDroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onLocated(self);
                }
            }
        });
    }

    private void onMotorsChanged(final boolean value) {
        final DJIDroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onMotorsChanged(self, value);
                }
            }
        });
    }

    private void onCommandExecuted(final com.dronelink.core.kernel.command.Command command) {
        final DJIDroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onCommandExecuted(self, command);
                }
            }
        });
    }

    private void onCommandFinished(final com.dronelink.core.kernel.command.Command command, final CommandError error) {
        final DJIDroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onCommandFinished(self, command, error);
                }
            }
        });
    }

    private void onCameraFileGenerated(final DJICameraFile file) {
        for (final Listener listener : listeners) {
            listener.onCameraFileGenerated(this, file);
        }
    }

    @Override
    public void addCommand(final com.dronelink.core.kernel.command.Command command) throws Dronelink.UnregisteredException, CommandTypeUnhandledException {
        Command.Executor executor = null;

        if (command instanceof DroneCommand) {
            executor = new Command.Executor() {
                @Override
                public CommandError execute(final Command.Finisher finished) {
                    onCommandExecuted(command);
                    return executeDroneCommand((DroneCommand)command, finished);
                }
            };
        }
        else if (command instanceof RemoteControllerCommand) {
            executor = new Command.Executor() {
                @Override
                public CommandError execute(final Command.Finisher finished) {
                    onCommandExecuted(command);
                    return executeRemoteControllerCommand((RemoteControllerCommand) command, finished);
                }
            };
        }
        else if (command instanceof CameraCommand) {
            executor = new Command.Executor() {
                @Override
                public CommandError execute(final Command.Finisher finished) {
                    onCommandExecuted(command);
                    return executeCameraCommand((CameraCommand)command, finished);
                }
            };
        }
        else if (command instanceof GimbalCommand) {
            executor = new Command.Executor() {
                @Override
                public CommandError execute(final Command.Finisher finished) {
                    onCommandExecuted(command);
                    return executeGimbalCommand((GimbalCommand)command, finished);
                }
            };
        }

        if (executor != null) {
            final Command c = new Command(
                command,
                executor,
                new Command.Finisher() {
                    @Override
                    public void execute(final CommandError error) {
                        onCommandFinished(command, error);
                    }
                },
                command.getConfig());

            if (c.config.retriesEnabled == null) {
                //disable retries when the DJI SDK reports that the product does not support the feature
                c.config.retriesEnabled = new CommandConfig.RetriesEnabled() {
                    @Override
                    public boolean execute(final CommandError error) {
                        if (error != null && error.code == DJIError.COMMAND_NOT_SUPPORTED_BY_HARDWARE.getErrorCode()) {
                            return false;
                        }
                        return true;
                    }
                };

                if (c.config.finishDelayMillis == null) {
                    //adding a 1.5 second delay after camera and gimbal mode commands
                    if (command instanceof ModeCameraCommand || command instanceof ModeGimbalCommand) {
                        c.config.finishDelayMillis = 1500.0;
                    }
                }
            }

            if (command instanceof DroneCommand) {
                droneCommands.addCommand(c);
            }
            else if (command instanceof RemoteControllerCommand) {
                remoteControllerCommands.addCommand(((RemoteControllerCommand)command).channel, c);
            }
            else if (command instanceof CameraCommand) {
                cameraCommands.addCommand(((CameraCommand)command).channel, c);
            }
            else if (command instanceof GimbalCommand) {
                gimbalCommands.addCommand(((GimbalCommand)command).channel, c);
            }
            return;
        }

        throw new CommandTypeUnhandledException();
    }

    @Override
    public void removeCommands() {
        droneCommands.removeAll();
        remoteControllerCommands.removeAll();
        cameraCommands.removeAll();
        gimbalCommands.removeAll();
    }

    @Override
    public DroneControlSession createControlSession(final Context context, final ExecutionEngine executionEngine, final Executor executor) throws UnsupportedExecutionEngineException, UnsupportedDroneDJIExecutionEngineException {
        switch (executionEngine) {
            case DRONELINK_KERNEL:
                return new DJIVirtualStickSession(context, this);

            case DJI:
                switch (adapter.drone.getModel()) {
                    case MAVIC_MINI:
                    case DJI_MINI_2:
                    case DJI_MINI_SE:
                    case MAVIC_AIR_2:
                    case DJI_AIR_2S:
                    case MATRICE_300_RTK:
                        throw new UnsupportedDroneDJIExecutionEngineException();

                    default:
                        break;
                }

                if (executor instanceof MissionExecutor) {
                    try {
                        return new DJIWaypointMissionSession(context, this, (MissionExecutor)executor);
                    } catch (final JSONException e) {
                        throw new UnsupportedExecutionEngineException(executionEngine);
                    }
                }
                break;

            default:
                break;
        }

        throw new UnsupportedExecutionEngineException(executionEngine);
    }

    @Override
    public DatedValue<RemoteControllerStateAdapter> getRemoteControllerState(final int channel) {
        try {
            return remoteControllerSerialQueue.submit(new Callable<DatedValue<RemoteControllerStateAdapter>>() {
                @Override
                public DatedValue<RemoteControllerStateAdapter> call() {
                    if (remoteControllerState == null) {
                        return null;
                    }

                    final RemoteControllerStateAdapter remoteControllerStateAdapter = new DJIRemoteControllerStateAdapter(remoteControllerState.value);
                    return new DatedValue<>(remoteControllerStateAdapter, remoteControllerState.date);
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    @Override
    public DatedValue<CameraStateAdapter> getCameraState(final int channel) {
        return getCameraState(channel, null);
    }

    @Override
    public DatedValue<CameraStateAdapter> getCameraState(final int channel, final Integer lensIndex) {
        try {
            return cameraSerialQueue.submit(new Callable<DatedValue<CameraStateAdapter>>() {
                @Override
                public DatedValue<CameraStateAdapter> call() {
                    final DatedValue<SystemState> systemState = cameraStates.get(channel);
                    final CameraAdapter camera = getDrone().getCamera(channel);
                    if (systemState == null || camera == null) {
                        return null;
                    }

                    int lensIndexResolved = 0;
                    if (lensIndex != null) {
                        lensIndexResolved = lensIndex;
                    }
                    else {
                        final DatedValue<CameraVideoStreamSource> videoStreamSource = cameraVideoStreamSources.get(channel);
                        if (videoStreamSource != null && videoStreamSource.value != null) {
                            lensIndexResolved = camera.getLensIndex(DronelinkDJI.getCameraVideoStreamSource(videoStreamSource.value));
                        }
                    }

                    final DatedValue<CameraVideoStreamSource> videoStreamSource = cameraVideoStreamSources.get(channel);
                    final DatedValue<FocusState> focusState = cameraFocusStates.get(channel + "." + lensIndexResolved);
                    final DatedValue<StorageState> storageState = cameraStorageStates.get(channel);
                    final DatedValue<ExposureSettings> exposureSettings = cameraExposureSettings.get(channel + "." + lensIndexResolved);
                    final DatedValue<SettingsDefinitions.ExposureCompensation> exposureCompensation = cameraExposureCompensation.get(channel);
                    final DatedValue<String> lensInformation = cameraLensInformation.get(channel);
                    final CameraStateAdapter cameraStateAdapter = new DJICameraStateAdapter(
                            systemState.value,
                            videoStreamSource == null ? null : videoStreamSource.value,
                            focusState == null ? null : focusState.value,
                            storageState == null ? null : storageState.value,
                            exposureSettings == null ? null : exposureSettings.value,
                            exposureCompensation == null ? null : exposureCompensation.value,
                            lensIndexResolved,
                            lensInformation == null ? null : lensInformation.value,
                            focusRingValue == null ? null : focusRingValue.value,
                            focusRingMax == null ? null : focusRingMax.value);
                    return new DatedValue<>(cameraStateAdapter, systemState.date);
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    @Override
    public DatedValue<GimbalStateAdapter> getGimbalState(final int channel) {
        try {
            return gimbalSerialQueue.submit(new Callable<DatedValue<GimbalStateAdapter>>() {
                @Override
                public DatedValue<GimbalStateAdapter> call() {
                    final DatedValue<GimbalState> gimbalState = gimbalStates.get(channel);
                    if (gimbalState == null) {
                        return null;
                    }

                    final GimbalStateAdapter gimbalStateAdapter = new DJIGimbalStateAdapter(gimbalState.value);
                    return new DatedValue<>(gimbalStateAdapter, gimbalState.date);
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    @Override
    public void resetPayloads() {
        sendResetGimbalCommands();
        sendResetCameraCommands();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    protected void sendResetVelocityCommand(final CommonCallbacks.CompletionCallback completion) {
        adapter.sendResetVelocityCommand(completion);
    }

    protected void sendResetGimbalCommands() {
        final List<Gimbal> gimbals = adapter.getDrone().getGimbals();
        if (gimbals == null) {
            return;
        }

        for (final Gimbal gimbal : gimbals) {
            final Rotation.Builder rotation = new Rotation.Builder();
            rotation.mode(RotationMode.ABSOLUTE_ANGLE);
            rotation.time(DronelinkDJI.GimbalRotationMinTime);
            if (DronelinkDJI.isAdjustPitchSupported(gimbal)) {
                rotation.pitch(-12);
            }
            if (DronelinkDJI.isAdjustRollSupported(gimbal)) {
                rotation.roll(0);
            }


            final DatedValue<GimbalStateAdapter> state = getGimbalState(gimbal.getIndex());
            if (DronelinkDJI.isAdjustYawSupported(gimbal) && state != null && state.value.getMode() != GimbalMode.YAW_FOLLOW) {
                gimbal.setMode(dji.common.gimbal.GimbalMode.YAW_FOLLOW, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError setModeError) {
                        gimbal.reset(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError resetError) {
                                gimbal.rotate(rotation.build(), null);
                            }
                        });
                    }
                });
            }
            else {
                gimbal.rotate(rotation.build(), null);
            }
        }
    }

    protected void sendResetCameraCommands() {
        final List<Camera> cameras = adapter.getDrone().getCameras();
        if (cameras == null) {
            return;
        }

        for (final Camera camera : cameras) {
            final DatedValue<CameraStateAdapter> state = getCameraState(camera.getIndex());
            if (state != null) {
                if (state.value.isCapturingVideo()) {
                    camera.stopRecordVideo(null);
                }
                else if (state.value.isCapturing()) {
                    camera.stopShootPhoto(null);
                }
            }
        }
    }

    private CommonCallbacks.CompletionCallback createCompletionCallback(final Command.Finisher finished) {
        return new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                finished.execute(DronelinkDJI.createCommandError(djiError));
            }
        };
    }

    private <V> CommonCallbacks.CompletionCallbackWith<V> createCompletionCallbackWith(final Command.FinisherWith<V> success, final Command.Finisher error) {
        return new CommonCallbacks.CompletionCallbackWith<V>() {
            @Override
            public void onSuccess(final V value) {
                success.execute(value);
            }

            @Override
            public void onFailure(final DJIError djiError) {
                error.execute((DronelinkDJI.createCommandError(djiError)));
            }
        };
    }

    private CommandError executeDroneCommand(final DroneCommand command, final Command.Finisher finished) {
        if (command instanceof FlightAssistantDroneCommand) {
            return executeFlightAssistantDroneCommand((FlightAssistantDroneCommand) command, finished);
        }

        if (command instanceof LandingGearDroneCommand) {
            return executeLandingGearDroneCommand((LandingGearDroneCommand) command, finished);
        }

        if (command instanceof LightbridgeDroneCommand) {
            return executeLightbridgeDroneCommand((LightbridgeDroneCommand) command, finished);
        }

        if (command instanceof OcuSyncDroneCommand) {
            return executeOcuSyncDroneCommand((OcuSyncDroneCommand) command, finished);
        }

        if (command instanceof AccessoryDroneCommand) {
            return executeAccessoryDroneCommand((AccessoryDroneCommand) command, finished);
        }

        final FlightController flightController = adapter.getDrone().getFlightController();
        if (flightController == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_control_unavailable_title));
        }

        if (command instanceof ConnectionFailSafeBehaviorDroneCommand) {
            flightController.getConnectionFailSafeBehavior(createCompletionCallbackWith(new Command.FinisherWith<ConnectionFailSafeBehavior>() {
                @Override
                public void execute(final ConnectionFailSafeBehavior current) {
                    final ConnectionFailSafeBehavior target = DronelinkDJI.getDroneConnectionFailSafeBehavior(((ConnectionFailSafeBehaviorDroneCommand) command).connectionFailSafeBehavior);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setConnectionFailSafeBehavior(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof HomeLocationDroneCommand) {
            final GeoCoordinate coordinate = ((HomeLocationDroneCommand) command).coordinate;
            flightController.setHomeLocation(DronelinkDJI.getCoordinate(coordinate), createCompletionCallback(finished));
            return null;
        }

        if (command instanceof LowBatteryWarningThresholdDroneCommand) {
            flightController.getLowBatteryWarningThreshold(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = (int)(((LowBatteryWarningThresholdDroneCommand) command).lowBatteryWarningThreshold * 100);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setLowBatteryWarningThreshold(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof MaxAltitudeDroneCommand) {
            flightController.getMaxFlightHeight(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = (int)(((MaxAltitudeDroneCommand) command).maxAltitude);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setMaxFlightHeight(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof MaxDistanceDroneCommand) {
            flightController.getMaxFlightRadius(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = (int)(((MaxDistanceDroneCommand) command).maxDistance);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setMaxFlightRadius(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof MaxDistanceLimitationDroneCommand) {
            flightController.getMaxFlightRadiusLimitationEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((MaxDistanceLimitationDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setMaxFlightRadiusLimitationEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ReturnHomeAltitudeDroneCommand) {
            flightController.getGoHomeHeightInMeters(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = (int)(((ReturnHomeAltitudeDroneCommand) command).returnHomeAltitude);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setGoHomeHeightInMeters(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof SeriousLowBatteryWarningThresholdDroneCommand) {
            flightController.getSeriousLowBatteryWarningThreshold(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = (int)(((SeriousLowBatteryWarningThresholdDroneCommand) command).seriousLowBatteryWarningThreshold * 100);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setSeriousLowBatteryWarningThreshold(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof SmartReturnHomeDroneCommand) {
            flightController.getSmartReturnToHomeEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((SmartReturnHomeDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightController.setSmartReturnToHomeEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeFlightAssistantDroneCommand(final FlightAssistantDroneCommand command, final Command.Finisher finished) {
        final FlightController flightController = adapter.getDrone().getFlightController();
        if (flightController == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_flight_assistant_unavailable_title));
        }

        final FlightAssistant flightAssistant = flightController.getFlightAssistant();
        if (flightAssistant == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_flight_assistant_unavailable_title));
        }

        if (command instanceof CollisionAvoidanceDroneCommand) {
//            flightAssistant.getCollisionAvoidanceEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
//                @Override
//                public void execute(final Boolean current) {
//                    final Boolean target = ((CollisionAvoidanceDroneCommand) command).enabled;
//                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
//                        @Override
//                        public void execute() {
//                            flightAssistant.setCollisionAvoidanceEnabled(target, createCompletionCallback(finished));
//                        }
//                    });
//                }
//            }, finished));
            //skipping conditional execution for now because it seems like the DJI SDK always returns true for getCollisionAvoidanceEnabled
            flightAssistant.setCollisionAvoidanceEnabled(((CollisionAvoidanceDroneCommand) command).enabled, createCompletionCallback(finished));
            return null;
        }

        if (command instanceof LandingProtectionDroneCommand) {
            flightAssistant.getLandingProtectionEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((LandingProtectionDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setLandingProtectionEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof PrecisionLandingDroneCommand) {
            flightAssistant.getPrecisionLandingEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((PrecisionLandingDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setPrecisionLandingEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ReturnHomeObstacleAvoidanceDroneCommand) {
            flightAssistant.getRTHObstacleAvoidanceEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((ReturnHomeObstacleAvoidanceDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setRTHObstacleAvoidanceEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ReturnHomeRemoteObstacleAvoidanceDroneCommand) {
            flightAssistant.getRTHRemoteObstacleAvoidanceEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((ReturnHomeRemoteObstacleAvoidanceDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setRTHRemoteObstacleAvoidanceEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof UpwardsAvoidanceDroneCommand) {
            flightAssistant.getUpwardVisionObstacleAvoidanceEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((UpwardsAvoidanceDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setUpwardVisionObstacleAvoidanceEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VisionAssistedPositioningDroneCommand) {
            flightAssistant.getVisionAssistedPositioningEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((VisionAssistedPositioningDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setVisionAssistedPositioningEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeLandingGearDroneCommand(final LandingGearDroneCommand command, final Command.Finisher finished) {
        final LandingGear landingGear = adapter.getDrone().getFlightController().getLandingGear();
        if (landingGear == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_landing_gear_unavailable_title));
        }

        if (command instanceof LandingGearAutomaticMovementDroneCommand) {
            landingGear.getAutomaticMovementEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((LandingGearAutomaticMovementDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            landingGear.setAutomaticMovementEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof LandingGearDeployDroneCommand) {
            Command.conditionallyExecute(!(landingGear.getState() == LandingGearState.DEPLOYED || landingGear.getState() == LandingGearState.DEPLOYING), finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    landingGear.deploy(createCompletionCallback(finished));
                }
            });
            return null;
        }

        if (command instanceof LandingGearRetractDroneCommand) {
            Command.conditionallyExecute(!(landingGear.getState() == LandingGearState.RETRACTED || landingGear.getState() == LandingGearState.RETRACTING), finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    landingGear.retract(createCompletionCallback(finished));
                }
            });
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeLightbridgeDroneCommand(final LightbridgeDroneCommand command, final Command.Finisher finished) {
        final AirLink airLink = adapter.getDrone().getAirLink();
        if (airLink == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_lightbridge_unavailable_title));
        }

        final LightbridgeLink link = airLink.getLightbridgeLink();
        if (link == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_lightbridge_unavailable_title));
        }

        if (command instanceof LightbridgeChannelDroneCommand) {
            link.getChannelNumber(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = ((LightbridgeChannelDroneCommand) command).lightbridgeChannel;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setChannelNumber(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof LightbridgeChannelSelectionModeDroneCommand) {
            link.getChannelSelectionMode(createCompletionCallbackWith(new Command.FinisherWith<ChannelSelectionMode>() {
                @Override
                public void execute(final ChannelSelectionMode current) {
                    final ChannelSelectionMode target = DronelinkDJI.getLightbridgeChannelSelectionMode(((LightbridgeChannelSelectionModeDroneCommand) command).lightbridgeChannelSelectionMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setChannelSelectionMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof LightbridgeFrequencyBandDroneCommand) {
            link.getFrequencyBand(createCompletionCallbackWith(new Command.FinisherWith<LightbridgeFrequencyBand>() {
                @Override
                public void execute(final LightbridgeFrequencyBand current) {
                    final LightbridgeFrequencyBand target = DronelinkDJI.getLightbridgeFrequencyBand(((LightbridgeFrequencyBandDroneCommand) command).lightbridgeFrequencyBand);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setFrequencyBand(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeOcuSyncDroneCommand(final OcuSyncDroneCommand command, final Command.Finisher finished) {
        final AirLink airLink = adapter.getDrone().getAirLink();
        if (airLink == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_ocusync_unavailable_title));
        }

        final OcuSyncLink link = airLink.getOcuSyncLink();
        if (link == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_ocusync_unavailable_title));
        }

        if (command instanceof OcuSyncChannelDroneCommand) {
            link.getChannelNumber(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = ((OcuSyncChannelDroneCommand) command).ocuSyncChannel;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setChannelNumber(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof OcuSyncChannelSelectionModeDroneCommand) {
            link.getChannelSelectionMode(createCompletionCallbackWith(new Command.FinisherWith<ChannelSelectionMode>() {
                @Override
                public void execute(final ChannelSelectionMode current) {
                    final ChannelSelectionMode target = DronelinkDJI.getOcuSyncChannelSelectionMode(((OcuSyncChannelSelectionModeDroneCommand) command).ocuSyncChannelSelectionMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setChannelSelectionMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof OcuSyncFrequencyBandDroneCommand) {
            link.getFrequencyBand(createCompletionCallbackWith(new Command.FinisherWith<OcuSyncFrequencyBand>() {
                @Override
                public void execute(final OcuSyncFrequencyBand current) {
                    final OcuSyncFrequencyBand target = DronelinkDJI.getOcuSyncFrequencyBand(((OcuSyncFrequencyBandDroneCommand) command).ocuSyncFrequencyBand);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            link.setFrequencyBand(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof OcuSyncVideoFeedSourcesDroneCommand) {
            link.assignSourceToPrimaryChannel(
                    DronelinkDJI.getOcuSyncFeedSource((OcuSyncVideoFeedSourcesDroneCommand) command, 0),
                    DronelinkDJI.getOcuSyncFeedSource((OcuSyncVideoFeedSourcesDroneCommand) command, 1),
                    createCompletionCallback(finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeAccessoryDroneCommand(final AccessoryDroneCommand command, final Command.Finisher finished) {
        final AccessoryAggregation accessoryAggregation = adapter.getDrone().getAccessoryAggregation();
        if (accessoryAggregation == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_accessory_aggregation_unavailable_title));
        }

        if (command instanceof BeaconDroneCommand) {
            final Beacon beacon = accessoryAggregation.getBeacon();
            if (beacon == null) {
                return new CommandError(context.getString(R.string.MissionDisengageReason_drone_beacon_unavailable_title));
            }

            beacon.getEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((BeaconDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            beacon.setEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof SpotlightDroneCommand || command instanceof SpotlightBrightnessDroneCommand) {
            final Spotlight spotlight = accessoryAggregation.getSpotlight();
            if (spotlight == null) {
                return new CommandError(context.getString(R.string.MissionDisengageReason_drone_spotlight_unavailable_title));
            }

            if (command instanceof SpotlightDroneCommand) {
                spotlight.getEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                    @Override
                    public void execute(final Boolean current) {
                        final Boolean target = ((SpotlightDroneCommand) command).enabled;
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                spotlight.setEnabled(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
                return null;
            }

            spotlight.setBrightness((int)(((SpotlightBrightnessDroneCommand) command).spotlightBrightness * 100), createCompletionCallback(finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeRemoteControllerCommand(final RemoteControllerCommand command, final Command.Finisher finished) {
        final RemoteController remoteController = DronelinkDJI.getRemoteController(adapter.getDrone(), command.channel);
        if (remoteController == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_remote_controller_unavailable_title));
        }

        if (command instanceof TargetGimbalChannelRemoteControllerCommand) {
            remoteController.getControllingGimbalIndex(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final int target = ((TargetGimbalChannelRemoteControllerCommand) command).targetGimbalChannel;
                    Command.conditionallyExecute(target != current, finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            remoteController.setControllingGimbalIndex(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeCameraCommand(final CameraCommand command, final Command.Finisher finished) {
        final Camera camera = DronelinkDJI.getCamera(adapter.getDrone(), command.channel);
        final DatedValue<CameraStateAdapter> state = getCameraState(command.channel);
        if (camera == null || state == null || !(state.value instanceof DJICameraStateAdapter)) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_unavailable_title));
        }
        final DJICameraStateAdapter djiState = (DJICameraStateAdapter)state.value;

        if (command instanceof AEBCountCameraCommand) {
            camera.getPhotoAEBCount(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.PhotoAEBCount>() {
                @Override
                public void execute(final SettingsDefinitions.PhotoAEBCount current) {
                    final SettingsDefinitions.PhotoAEBCount target = DronelinkDJI.getCameraAEBCount(((AEBCountCameraCommand) command).aebCount);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setPhotoAEBCount(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ApertureCameraCommand) {
            final SettingsDefinitions.Aperture target = DronelinkDJI.getCameraAperture(((ApertureCameraCommand) command).aperture);
            Command.conditionallyExecute(djiState.exposureSettings.getAperture() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setAperture(target, createCompletionCallback(finished));
                }
            });
            return null;
        }

        if (command instanceof AutoLockGimbalCameraCommand) {
            camera.getAutoLockGimbalEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((AutoLockGimbalCameraCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setAutoLockGimbalEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof AutoExposureLockCameraCommand) {
            camera.getAELock(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((AutoExposureLockCameraCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setAELock(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof DisplayModeCameraCommand) {
            if (adapter.drone.getModel() == Model.MAVIC_2_ENTERPRISE_DUAL || camera.getDisplayName() == Camera.DisplayNameXT2_IR) {
                camera.getDisplayMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.DisplayMode>() {
                    @Override
                    public void execute(final SettingsDefinitions.DisplayMode current) {
                        final SettingsDefinitions.DisplayMode target = DronelinkDJI.getCameraDisplayMode(((DisplayModeCameraCommand) command).displayMode);
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                camera.setDisplayMode(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
                return null;
            }

            final Lens lens = DronelinkDJI.getLens(camera, ((DisplayModeCameraCommand) command).lensIndex);
            if (lens == null) {
                return new CommandError(context.getString(R.string.MissionDisengageReason_drone_lens_unavailable_title));
            }

            lens.getDisplayMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.DisplayMode>() {
                @Override
                public void execute(final SettingsDefinitions.DisplayMode current) {
                    final SettingsDefinitions.DisplayMode target = DronelinkDJI.getCameraDisplayMode(((DisplayModeCameraCommand) command).displayMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            lens.setDisplayMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ColorCameraCommand) {
            camera.getColor(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.CameraColor>() {
                @Override
                public void execute(final SettingsDefinitions.CameraColor current) {
                    final SettingsDefinitions.CameraColor target = DronelinkDJI.getCameraColor(((ColorCameraCommand) command).color);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setColor(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ContrastCameraCommand) {
            camera.getContrast(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = ((ContrastCameraCommand) command).contrast;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setContrast(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ExposureCompensationCameraCommand) {
            final SettingsDefinitions.ExposureCompensation target = DronelinkDJI.getCameraExposureCompensation(((ExposureCompensationCameraCommand) command).exposureCompensation);
            Command.conditionallyExecute(DronelinkDJI.getCameraExposureCompensation(djiState.getExposureCompensation()) != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    final Lens lens = DronelinkDJI.getLens(camera, djiState.getLensIndex());
                    if (lens == null) {
                        camera.setExposureCompensation(target, createCompletionCallback(finished));
                    }
                    else {
                        lens.setExposureCompensation(target, createCompletionCallback(finished));
                    }
                }
            });
            return null;
        }

        if (command instanceof ExposureCompensationStepCameraCommand) {
            final SettingsDefinitions.ExposureCompensation target = DronelinkDJI.getCameraExposureCompensation(djiState.getExposureCompensation().offset(((ExposureCompensationStepCameraCommand) command).exposureCompensationSteps));
            Command.conditionallyExecute(DronelinkDJI.getCameraExposureCompensation(djiState.getExposureCompensation()) != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    final Lens lens = DronelinkDJI.getLens(camera, djiState.getLensIndex());
                    if (lens == null) {
                        camera.setExposureCompensation(target, createCompletionCallback(finished));
                    }
                    else {
                        lens.setExposureCompensation(target, createCompletionCallback(finished));
                    }
                }
            });
            return null;
        }

        if (command instanceof ExposureModeCameraCommand) {
            camera.getExposureMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.ExposureMode>() {
                @Override
                public void execute(final SettingsDefinitions.ExposureMode current) {
                    final SettingsDefinitions.ExposureMode target = DronelinkDJI.getCameraExposureMode(((ExposureModeCameraCommand) command).exposureMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setExposureMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof FileIndexModeCameraCommand) {
            camera.getFileIndexMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.FileIndexMode>() {
                @Override
                public void execute(final SettingsDefinitions.FileIndexMode current) {
                    final SettingsDefinitions.FileIndexMode target = DronelinkDJI.getCameraFileIndexMode(((FileIndexModeCameraCommand) command).fileIndexMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setFileIndexMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof FocusCameraCommand) {
            final FocusCameraCommand focusCameraCommand = (FocusCameraCommand)command;
            camera.setFocusTarget(new PointF((float)focusCameraCommand.focusTarget.x, (float)focusCameraCommand.focusTarget.y), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(final DJIError djiError) {
                    if (djiError != null) {
                        finished.execute(DronelinkDJI.createCommandError(djiError));
                        return;
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cameraCommandFinishFocusTargetVerifyRing(focusCameraCommand, finished);
                        }
                    }, 500);
                }
            });

            return null;
        }

        if (command instanceof FocusDistanceCameraCommand) {
            final FocusDistanceCameraCommand focusDistanceCameraCommand = (FocusDistanceCameraCommand)command;
            final CameraFocusCalibration cameraFocusCalibration = Dronelink.getInstance().getCameraFocusCalibration(focusDistanceCameraCommand.focusCalibration.withDroneSerialNumber(getSerialNumber()));
            if (cameraFocusCalibration == null) {
                return new CommandError(context.getString(R.string.DJIDroneSession_cameraCommand_focus_distance_error));
            }
            camera.setFocusRingValue(cameraFocusCalibration.ringValue.intValue(), createCompletionCallback(finished));
            return null;
        }

        if (command instanceof FocusModeCameraCommand) {
            camera.getFocusMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.FocusMode>() {
                @Override
                public void execute(final SettingsDefinitions.FocusMode current) {
                    final SettingsDefinitions.FocusMode target = DronelinkDJI.getCameraFocusMode(((FocusModeCameraCommand) command).focusMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setFocusMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof FocusRingCameraCommand) {
            final Double focusRingMax = djiState.getFocusRingMax();
            camera.setFocusRingValue((int)(((FocusRingCameraCommand)command).focusRingPercent * (focusRingMax == null ? 0 : focusRingMax)), createCompletionCallback(finished));
            return null;
        }

        if (command instanceof ISOCameraCommand) {
            final SettingsDefinitions.ISO target = DronelinkDJI.getCameraISO(((ISOCameraCommand) command).iso);
            Command.conditionallyExecute(djiState.exposureSettings.getISO() != target.value(), finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setISO(target, createCompletionCallback(finished));
                }
            });
            return null;
        }

        if (command instanceof MechanicalShutterCameraCommand) {
            camera.getMechanicalShutterEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((MechanicalShutterCameraCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setMechanicalShutterEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof MeteringModeCameraCommand) {
            camera.getMeteringMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.MeteringMode>() {
                @Override
                public void execute(final SettingsDefinitions.MeteringMode current) {
                    final SettingsDefinitions.MeteringMode target = DronelinkDJI.getCameraMeteringMode(((MeteringModeCameraCommand) command).meteringMode);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setMeteringMode(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ModeCameraCommand) {
            if (camera.isFlatCameraModeSupported()) {
                camera.getFlatMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.FlatCameraMode>() {
                    @Override
                    public void execute(final SettingsDefinitions.FlatCameraMode current) {
                        final SettingsDefinitions.FlatCameraMode target = DronelinkDJI.getCameraModeFlat(((ModeCameraCommand) command).mode);
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                camera.setFlatMode(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
            }
            else {
                final CameraMode target = ((ModeCameraCommand) command).mode;
                Command.conditionallyExecute(djiState.getMode() != target, finished, new Command.ConditionalExecutor() {
                    @Override
                    public void execute() {
                        camera.setMode(DronelinkDJI.getCameraMode(target), createCompletionCallback(finished));
                    }
                });
            }
            return null;
        }

        if (command instanceof PhotoAspectRatioCameraCommand) {
            camera.getPhotoAspectRatio(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.PhotoAspectRatio>() {
                @Override
                public void execute(final SettingsDefinitions.PhotoAspectRatio current) {
                    final SettingsDefinitions.PhotoAspectRatio target = DronelinkDJI.getCameraPhotoAspectRatio(((PhotoAspectRatioCameraCommand) command).photoAspectRatio);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setPhotoAspectRatio(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof PhotoFileFormatCameraCommand) {
            camera.getPhotoFileFormat(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.PhotoFileFormat>() {
                @Override
                public void execute(final SettingsDefinitions.PhotoFileFormat current) {
                    final SettingsDefinitions.PhotoFileFormat target = DronelinkDJI.getCameraPhotoFileFormat(((PhotoFileFormatCameraCommand) command).photoFileFormat);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setPhotoFileFormat(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof PhotoIntervalCameraCommand) {
            camera.getPhotoTimeIntervalSettings(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.PhotoTimeIntervalSettings>() {
                @Override
                public void execute(final SettingsDefinitions.PhotoTimeIntervalSettings current) {
                    final SettingsDefinitions.PhotoTimeIntervalSettings target = new SettingsDefinitions.PhotoTimeIntervalSettings(255, ((PhotoIntervalCameraCommand) command).photoInterval);
                    Command.conditionallyExecute(current.getCaptureCount() != target.getCaptureCount() || current.getTimeIntervalInSeconds() != target.getTimeIntervalInSeconds(), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setPhotoTimeIntervalSettings(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof PhotoModeCameraCommand) {
            if (camera.isFlatCameraModeSupported()) {
                camera.getFlatMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.FlatCameraMode>() {
                    @Override
                    public void execute(final SettingsDefinitions.FlatCameraMode current) {
                        final SettingsDefinitions.FlatCameraMode target = DronelinkDJI.getCameraModeFlat(((PhotoModeCameraCommand) command).photoMode);
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                camera.setFlatMode(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
            }
            else {
                camera.getShootPhotoMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.ShootPhotoMode>() {
                    @Override
                    public void execute(final SettingsDefinitions.ShootPhotoMode current) {
                        final SettingsDefinitions.ShootPhotoMode target = DronelinkDJI.getCameraPhotoMode(((PhotoModeCameraCommand) command).photoMode);
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                camera.setShootPhotoMode(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
            }
            return null;
        }

        if (command instanceof SaturationCameraCommand) {
            camera.getSaturation(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = ((SaturationCameraCommand) command).saturation;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setSaturation(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof SharpnessCameraCommand) {
            camera.getSharpness(createCompletionCallbackWith(new Command.FinisherWith<Integer>() {
                @Override
                public void execute(final Integer current) {
                    final Integer target = ((SharpnessCameraCommand) command).sharpness;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setSharpness(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof ShutterSpeedCameraCommand) {
            final SettingsDefinitions.ShutterSpeed target = DronelinkDJI.getCameraShutterSpeed(((ShutterSpeedCameraCommand) command).shutterSpeed);
            Command.conditionallyExecute(djiState.exposureSettings.getShutterSpeed() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setShutterSpeed(target, createCompletionCallback(finished));
                }
            });
            return null;
        }

        if (command instanceof SpotMeteringTargetCameraCommand) {
            final SpotMeteringTargetCameraCommand spotMeteringTargetCameraCommand = (SpotMeteringTargetCameraCommand)command;
            camera.setSpotMeteringTarget(new Point((int)Math.round(spotMeteringTargetCameraCommand.spotMeteringTarget.x * 11), (int)Math.round(spotMeteringTargetCameraCommand.spotMeteringTarget.y * 7)), createCompletionCallback(finished));
            return null;
        }

        if (command instanceof StartCaptureCameraCommand) {
            switch (djiState.getMode()) {
                case PHOTO:
                    if (djiState.isCapturingPhotoInterval()) {
                        Log.d(TAG, "Camera start capture skipped, already shooting interval photos");
                        finished.execute(null);
                    }
                    else {
                        Log.d(TAG, "Camera start capture photo");
                        final Date started = new Date();
                        camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                            boolean resultReceived = false;

                            @Override
                            public void onResult(final DJIError djiError) {
                                //seeing two calls to onResult when taking interval photos!
                                if (resultReceived) {
                                    Log.d(TAG, "Camera start capture received multiple results!");
                                    return;
                                }
                                resultReceived = true;

                                if (djiError != null) {
                                    finished.execute(DronelinkDJI.createCommandError(djiError));
                                    return;
                                }

                                //waiting since isBusy will still be false for a bit
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        final StartCaptureCameraCommand startCaptureCameraCommand = (StartCaptureCameraCommand)command;
                                        if (startCaptureCameraCommand.verifyFileCreated) {
                                            cameraCommandFinishStartShootPhotoVerifyFile(startCaptureCameraCommand, started, finished);
                                        }
                                        else {
                                            cameraCommandFinishNotBusy(startCaptureCameraCommand, finished);
                                        }
                                    }
                                }, 500);
                            }
                        });
                    }
                    break;

                case VIDEO:
                    if (djiState.isCapturingVideo()) {
                        Log.d(TAG, "Camera start capture skipped, already recording video");
                        finished.execute(null);
                    }
                    else {
                        Log.d(TAG, "Camera start capture video");
                        camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                if (djiError != null) {
                                    finished.execute(DronelinkDJI.createCommandError(djiError));
                                    return;
                                }

                                //waiting since isBusy will still be false for a bit
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cameraCommandFinishNotBusy(command, finished);
                                    }
                                }, 500);
                            }
                        });
                    }
                    break;

                case PLAYBACK:
                case DOWNLOAD:
                case BROADCAST:
                case UNKNOWN:
                    Log.i(TAG, "Camera start capture invalid mode: " + djiState.getMode().toString());
                    return new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_mode_invalid_title));
            }
            return null;
        }

        if (command instanceof StopCaptureCameraCommand) {
            switch (djiState.getMode()) {
                case PHOTO:
                    if (djiState.isCapturingPhotoInterval()) {
                        Log.d(TAG, "Camera stop capture interval photo");
                        camera.stopShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                if (djiError != null) {
                                    finished.execute(DronelinkDJI.createCommandError(djiError));
                                    return;
                                }

                                cameraCommandFinishStopCapture(command, finished);
                            }
                        });
                    }
                    else {
                        Log.d(TAG, "Camera stop capture skipped, not shooting interval photos");
                        finished.execute(null);
                    }
                    break;

                case VIDEO:
                    if (djiState.isCapturingVideo()) {
                        Log.d(TAG, "Camera stop capture video");
                        camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                if (djiError != null) {
                                    finished.execute(DronelinkDJI.createCommandError(djiError));
                                    return;
                                }

                                cameraCommandFinishStopCapture(command, finished);
                            }
                        });
                    }
                    else {
                        Log.d(TAG, "Camera stop capture skipped, not recording video");
                        finished.execute(null);
                    }
                    break;

                case PLAYBACK:
                case DOWNLOAD:
                case BROADCAST:
                case UNKNOWN:
                    Log.i(TAG, "Camera start capture invalid mode: " + djiState.getMode().toString());
                    return new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_mode_invalid_title));
            }
            return null;
        }

        if (command instanceof StorageLocationCameraCommand) {
            camera.getStorageLocation(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.StorageLocation>() {
                @Override
                public void execute(final SettingsDefinitions.StorageLocation current) {
                    final SettingsDefinitions.StorageLocation target = DronelinkDJI.getCameraStorageLocation(((StorageLocationCameraCommand) command).storageLocation);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setStorageLocation(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoCaptionCameraCommand) {
            camera.getVideoCaptionEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((VideoCaptionCameraCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setVideoCaptionEnabled(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoFileCompressionStandardCameraCommand) {
            camera.getVideoFileCompressionStandard(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.VideoFileCompressionStandard>() {
                @Override
                public void execute(final SettingsDefinitions.VideoFileCompressionStandard current) {
                    final SettingsDefinitions.VideoFileCompressionStandard target = DronelinkDJI.getCameraVideoFileCompressionStandard(((VideoFileCompressionStandardCameraCommand) command).videoFileCompressionStandard);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setVideoFileCompressionStandard(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoFileFormatCameraCommand) {
            camera.getVideoFileFormat(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.VideoFileFormat>() {
                @Override
                public void execute(final SettingsDefinitions.VideoFileFormat current) {
                    final SettingsDefinitions.VideoFileFormat target = DronelinkDJI.getCameraVideoFileFormat(((VideoFileFormatCameraCommand) command).videoFileFormat);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setVideoFileFormat(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoModeCameraCommand) {
            if (camera.isFlatCameraModeSupported()) {
                camera.getFlatMode(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.FlatCameraMode>() {
                    @Override
                    public void execute(final SettingsDefinitions.FlatCameraMode current) {
                        final SettingsDefinitions.FlatCameraMode target = DronelinkDJI.getCameraModeFlat(((VideoModeCameraCommand) command).videoMode);
                        Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                            @Override
                            public void execute() {
                                camera.setFlatMode(target, createCompletionCallback(finished));
                            }
                        });
                    }
                }, finished));
            }
            else {
                Command.conditionallyExecute(djiState.getMode() != CameraMode.VIDEO, finished, new Command.ConditionalExecutor() {
                    @Override
                    public void execute() {
                        camera.setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, createCompletionCallback(finished));
                    }
                });
            }
            return null;
        }

        if (command instanceof VideoResolutionFrameRateCameraCommand) {
            camera.getVideoResolutionAndFrameRate(createCompletionCallbackWith(new Command.FinisherWith<ResolutionAndFrameRate>() {
                @Override
                public void execute(final ResolutionAndFrameRate current) {
                    final ResolutionAndFrameRate target = new ResolutionAndFrameRate(
                            DronelinkDJI.getCameraVideoVideoResolution(((VideoResolutionFrameRateCameraCommand) command).videoResolution),
                            DronelinkDJI.getCameraVideoVideoFrameRate(((VideoResolutionFrameRateCameraCommand) command).videoFrameRate),
                            DronelinkDJI.getCameraVideoVideoFieldOfView(((VideoResolutionFrameRateCameraCommand) command).videoFieldOfView)
                    );
                    Command.conditionallyExecute(current.getResolution() != target.getResolution() || current.getFrameRate() != target.getFrameRate() || current.getFov() != target.getFov(), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setVideoResolutionAndFrameRate(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoStandardCameraCommand) {
            camera.getVideoStandard(createCompletionCallbackWith(new Command.FinisherWith<SettingsDefinitions.VideoStandard>() {
                @Override
                public void execute(final SettingsDefinitions.VideoStandard current) {
                    final SettingsDefinitions.VideoStandard target = DronelinkDJI.getCameraVideoStandard(((VideoStandardCameraCommand) command).videoStandard);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setVideoStandard(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof VideoStreamSourceCameraCommand) {
            camera.getCameraVideoStreamSource(createCompletionCallbackWith(new Command.FinisherWith<CameraVideoStreamSource>() {
                @Override
                public void execute(final CameraVideoStreamSource current) {
                    final CameraVideoStreamSource target = DronelinkDJI.getCameraVideoStreamSource(((VideoStreamSourceCameraCommand) command).videoStreamSource);
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setCameraVideoStreamSource(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof WhiteBalanceCustomCameraCommand) {
            camera.getWhiteBalance(createCompletionCallbackWith(new Command.FinisherWith<WhiteBalance>() {
                @Override
                public void execute(final WhiteBalance current) {
                    final WhiteBalance target = new WhiteBalance(SettingsDefinitions.WhiteBalancePreset.CUSTOM, ((WhiteBalanceCustomCameraCommand) command).whiteBalanceCustom);
                    Command.conditionallyExecute(current.getWhiteBalancePreset() != target.getWhiteBalancePreset() || current.getColorTemperature() != target.getColorTemperature(), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setWhiteBalance(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        if (command instanceof WhiteBalancePresetCameraCommand) {
            camera.getWhiteBalance(createCompletionCallbackWith(new Command.FinisherWith<WhiteBalance>() {
                @Override
                public void execute(final WhiteBalance current) {
                    final WhiteBalance target = new WhiteBalance(DronelinkDJI.getCameraWhiteBalancePreset(((WhiteBalancePresetCameraCommand) command).whiteBalancePreset));
                    Command.conditionallyExecute(current.getWhiteBalancePreset() != target.getWhiteBalancePreset(), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setWhiteBalance(target, createCompletionCallback(finished));
                        }
                    });
                }
            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private void cameraCommandFinishStopCapture(final CameraCommand cameraCommand, final Command.Finisher finished) {
        cameraCommandFinishStopCapture(cameraCommand, 0, 20, finished);
    }

    private void cameraCommandFinishStopCapture(final CameraCommand cameraCommand, final int attempt, final int maxAttempts, final Command.Finisher finished) {
        if (attempt >= maxAttempts) {
            finished.execute(new CommandError(context.getString(R.string.DJIDroneSession_cameraCommand_stop_capture_error)));
            return;
        }

        final DatedValue<CameraStateAdapter> state = getCameraState(cameraCommand.channel);
        if (state == null || !(state.value instanceof DJICameraStateAdapter)) {
            finished.execute(new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_unavailable_title)));
            return;
        }

        if (!state.value.isCapturing()) {
            finished.execute(null);
            return;
        }

        final long wait = 250;
        Log.d(TAG, "Camera command finished and waiting for camera to stop capturing (" + ((attempt + 1) * wait) + "ms)... (" + cameraCommand.id + ")");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraCommandFinishNotBusy(cameraCommand, attempt + 1, maxAttempts, finished);
            }
        }, wait);
    }

    private void cameraCommandFinishStartShootPhotoVerifyFile(final StartCaptureCameraCommand cameraCommand, final Date started, final Command.Finisher finished) {
        cameraCommandFinishStartShootPhoto(cameraCommand, started, 0, 20, finished);
    }

    private void cameraCommandFinishStartShootPhoto(final StartCaptureCameraCommand cameraCommand, final Date started, final int attempt, final int maxAttempts, final Command.Finisher finished) {
        if (attempt >= maxAttempts) {
            finished.execute(new CommandError(context.getString(R.string.DJIDroneSession_cameraCommand_start_shoot_photo_no_file)));
            return;
        }

        final DatedValue<CameraFile> mostRecentCameraFile = this.mostRecentCameraFile;
        if (mostRecentCameraFile != null) {
            final long timeSinceMostRecentCameraFile = mostRecentCameraFile.date.getTime() - started.getTime();
            if (timeSinceMostRecentCameraFile > 0) {
                Log.d(TAG, "Camera start shoot photo found camera file (" + mostRecentCameraFile.value.getName() + ") after " + timeSinceMostRecentCameraFile + "ms (" + cameraCommand.id + ")");
                cameraCommandFinishNotBusy(cameraCommand, finished);
                return;
            }
        }

        final long wait = 250;
        Log.d(TAG, "Camera start shoot photo finished and waiting for camera file (" + ((attempt + 1) * wait) + "ms)... (" + cameraCommand.id + ")");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraCommandFinishStartShootPhoto(cameraCommand, started, attempt + 1, maxAttempts, finished);
            }
        }, wait);
    }

    private void cameraCommandFinishNotBusy(final CameraCommand cameraCommand, final Command.Finisher finished) {
        cameraCommandFinishNotBusy(cameraCommand, 0, 10, finished);
    }

    private void cameraCommandFinishNotBusy(final CameraCommand cameraCommand, final int attempt, final int maxAttempts, final Command.Finisher finished) {
        final DatedValue<CameraStateAdapter> state = getCameraState(cameraCommand.channel);
        if (state == null || !(state.value instanceof DJICameraStateAdapter)) {
            finished.execute(new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_unavailable_title)));
            return;
        }

        if (attempt >= maxAttempts || !state.value.isBusy()) {
            finished.execute(null);
            return;
        }

        Log.d(TAG, "Camera command finished and waiting for camera to not be busy (" + (attempt + 1) + ")... (" + cameraCommand.id + ")");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraCommandFinishNotBusy(cameraCommand, attempt + 1, maxAttempts, finished);
            }
        }, 100);
    }

    private void cameraCommandFinishFocusTargetVerifyRing(final FocusCameraCommand cameraCommand, final Command.Finisher finished) {
        cameraCommandFinishFocusTargetVerifyRing(cameraCommand, 0, 10, finished);
    }

    private void cameraCommandFinishFocusTargetVerifyRing(final FocusCameraCommand cameraCommand, final int attempt, final int maxAttempts, final Command.Finisher finished) {
        if (cameraCommand.focusRingPercentLimits == null) {
            finished.execute(null);
            return;
        }

        final DatedValue<CameraStateAdapter> state = getCameraState(cameraCommand.channel);
        if (state == null || !(state.value instanceof DJICameraStateAdapter)) {
            finished.execute(new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_unavailable_title)));
            return;
        }

        if (attempt >= maxAttempts) {
            finished.execute(new CommandError(context.getString(R.string.DJIDroneSession_cameraCommand_focus_target_error)));
            return;
        }

        if (!state.value.isBusy()) {
            final Double focusRingValue = state.value.getFocusRingValue();
            final Double focusRingMax = state.value.getFocusRingMax();
            if (focusRingValue != null && focusRingMax != null && focusRingMax > 0) {
                final double focusRingPercent = focusRingValue / focusRingMax;
                if (focusRingPercent < cameraCommand.focusRingPercentLimits.min || focusRingPercent > cameraCommand.focusRingPercentLimits.max) {
                    finished.execute(new CommandError(
                            context.getString(R.string.DJIDroneSession_cameraCommand_focus_target_ring_invalid) + " " +
                                    Dronelink.getInstance().format("percent", cameraCommand.focusRingPercentLimits.min, "") + " < " +
                                    Dronelink.getInstance().format("percent", focusRingPercent, "") + " < " +
                                    Dronelink.getInstance().format("percent", cameraCommand.focusRingPercentLimits.max, "")
                            ));
                    return;
                }
            }

            finished.execute(null);
            return;
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraCommandFinishFocusTargetVerifyRing(cameraCommand, attempt + 1, maxAttempts, finished);
            }
        }, 100);
    }

    private CommandError executeGimbalCommand(final GimbalCommand command, final Command.Finisher finished) {
        final Gimbal gimbal = DronelinkDJI.getGimbal(adapter.getDrone(), command.channel);
        final DatedValue<GimbalStateAdapter> state = getGimbalState(command.channel);
        if (gimbal == null || state == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_gimbal_unavailable_title));
        }

        if (command instanceof ModeGimbalCommand) {
            final GimbalMode target = ((ModeGimbalCommand) command).mode;
            Command.conditionallyExecute(state.value.getMode() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    gimbal.setMode(DronelinkDJI.getGimbalMode(target), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(final DJIError djiError) {
                            if (djiError != null) {
                                finished.execute(DronelinkDJI.createCommandError(djiError));
                                return;
                            }

                            if (DronelinkDJI.getGimbalMode(target) == dji.common.gimbal.GimbalMode.YAW_FOLLOW) {
                                gimbal.reset(createCompletionCallback(finished));
                            }
                            else {
                                finished.execute(null);
                            }
                        }
                    });
                }
            });
            return null;
        }

        if (command instanceof OrientationGimbalCommand) {
            final Orientation3Optional orientation = ((OrientationGimbalCommand) command).orientation;
            if (orientation.getPitch() == null && orientation.getRoll() == null && orientation.getYaw() == null) {
                finished.execute(null);
                return null;
            }

            final Rotation.Builder rotation = new Rotation.Builder();
            rotation.time(DronelinkDJI.GimbalRotationMinTime);

            Double pitch = orientation.getPitch() == null ? null : Convert.RadiansToDegrees(orientation.getPitch());
            if (pitch != null && Math.abs(pitch + 90) < 0.1) {
                pitch = -89.9;
            }

            Double roll = orientation.getRoll() == null ? null : Convert.RadiansToDegrees(orientation.getRoll());

            Double yaw = orientation.getYaw();
            if (yaw != null && (state.value.getMode() == GimbalMode.FREE || DronelinkDJI.isAdjustYaw360Supported(gimbal))) {
                //use relative angle because absolute angle for yaw is not predictable
                if (DronelinkDJI.isAdjustPitchSupported(gimbal) && pitch != null) {
                    rotation.pitch((float)Convert.RadiansToDegrees(Convert.AngleDifferenceSigned(Convert.DegreesToRadians(pitch), state.value.getOrientation().getPitch())));
                }

                if (DronelinkDJI.isAdjustRollSupported(gimbal) && roll != null) {
                    rotation.roll((float)Convert.RadiansToDegrees(Convert.AngleDifferenceSigned(Convert.DegreesToRadians(roll), state.value.getOrientation().getRoll())));
                }

                rotation.yaw((float)Convert.RadiansToDegrees(Convert.AngleDifferenceSigned(yaw, state.value.getOrientation().getYaw())));

                rotation.mode(RotationMode.RELATIVE_ANGLE);
                gimbal.rotate(rotation.build(), createCompletionCallback(finished));

                gimbal.rotate(rotation.build(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (djiError != null) {
                            finished.execute(DronelinkDJI.createCommandError(djiError));
                            return;
                        }

                        gimbalCommandFinishOrientationVerify((OrientationGimbalCommand)command, finished);
                    }
                });
                return null;
            }

            if (pitch == null && roll == null) {
                finished.execute(null);
                return null;
            }

            if (pitch != null && DronelinkDJI.isAdjustPitchSupported(gimbal)) {
                rotation.pitch(pitch.floatValue());
            }

            if (roll != null && DronelinkDJI.isAdjustRollSupported(gimbal)) {
                rotation.roll(roll.floatValue());
            }

            rotation.mode(RotationMode.ABSOLUTE_ANGLE);
            gimbal.rotate(rotation.build(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(final DJIError djiError) {
                    if (djiError != null) {
                        finished.execute(DronelinkDJI.createCommandError(djiError));
                        return;
                    }

                    gimbalCommandFinishOrientationVerify((OrientationGimbalCommand)command, finished);
                }
            });
            return null;
        }

        if (command instanceof YawSimultaneousFollowGimbalCommand) {
// TODO getYawSimultaneousFollowEnabled always returns false right now, DJI bug?
//            gimbal.getYawSimultaneousFollowEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
//                @Override
//                public void execute(final Boolean current) {
                    final Boolean target = ((YawSimultaneousFollowGimbalCommand) command).enabled;
//                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
//                        @Override
//                        public void execute() {
                            gimbal.setYawSimultaneousFollowEnabled(target, createCompletionCallback(finished));
//                        }
//                    });
//                }
//            }, finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private void gimbalCommandFinishOrientationVerify(final OrientationGimbalCommand gimbalCommand, final Command.Finisher finished) {
        gimbalCommandFinishOrientationVerify(gimbalCommand, 0, 20, Convert.DegreesToRadians(2.0), finished);
    }

    private void gimbalCommandFinishOrientationVerify(final OrientationGimbalCommand gimbalCommand, final int attempt, final int maxAttempts, final double threshold, final Command.Finisher finished) {
        final Gimbal gimbal = DronelinkDJI.getGimbal(adapter.getDrone(), gimbalCommand.channel);
        final DatedValue<GimbalStateAdapter> state = getGimbalState(gimbalCommand.channel);
        if (gimbal == null || state == null) {
            finished.execute(new CommandError(context.getString(R.string.MissionDisengageReason_drone_gimbal_unavailable_title)));
            return ;
        }

        if (attempt >= maxAttempts) {
            finished.execute(new CommandError(context.getString(R.string.DJIDroneSession_gimbalCommand_orientation_not_achieved)));
            return;
        }

        boolean verified = true;

        if (gimbalCommand.orientation.getPitch() != null && DronelinkDJI.isAdjustPitchSupported(gimbal)) {
            verified = verified && Math.abs(Convert.AngleDifferenceSigned(gimbalCommand.orientation.getPitch(), state.value.getOrientation().getPitch())) <= threshold;
        }

        if (gimbalCommand.orientation.getRoll() != null && DronelinkDJI.isAdjustRollSupported(gimbal)) {
            verified = verified && Math.abs(Convert.AngleDifferenceSigned(gimbalCommand.orientation.getRoll(), state.value.getOrientation().getRoll())) <= threshold;
        }

        if (gimbalCommand.orientation.getYaw() != null && (state.value.getMode() == GimbalMode.FREE || DronelinkDJI.isAdjustYaw360Supported(gimbal))) {
            verified = verified && Math.abs(Convert.AngleDifferenceSigned(gimbalCommand.orientation.getYaw(), state.value.getOrientation().getYaw())) <= threshold;
        }

        if (verified) {
            finished.execute(null);
            return;
        }

        final long wait = 100;
        Log.d(TAG, "Gimbal command finished and waiting for orientation (" + ((attempt + 1) * wait) + "ms)... (" + gimbalCommand.id + ")");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gimbalCommandFinishOrientationVerify(gimbalCommand, attempt + 1, maxAttempts, threshold, finished);
            }
        }, wait);
    }

    @Override
    public void onChange(final VideoFeeder.VideoFeed videoFeed, final PhysicalSource physicalSource) {
        final DJIDroneSession self = this;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onVideoFeedSourceUpdated(self, DronelinkDJI.getChannel(videoFeed));
                }
            }
        });
    }
}
