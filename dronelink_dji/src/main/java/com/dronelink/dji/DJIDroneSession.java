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

import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneControlSession;
import com.dronelink.core.DroneSession;
import com.dronelink.core.Dronelink;
import com.dronelink.core.Version;
import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.adapters.DroneAdapter;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.adapters.GimbalStateAdapter;
import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.command.Command;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.command.CommandQueue;
import com.dronelink.core.command.MultiChannelCommandQueue;
import com.dronelink.core.mission.command.camera.AEBCountCameraCommand;
import com.dronelink.core.mission.command.camera.ApertureCameraCommand;
import com.dronelink.core.mission.command.camera.AutoExposureLockCameraCommand;
import com.dronelink.core.mission.command.camera.AutoLockGimbalCameraCommand;
import com.dronelink.core.mission.command.camera.CameraCommand;
import com.dronelink.core.mission.command.camera.ColorCameraCommand;
import com.dronelink.core.mission.command.camera.ContrastCameraCommand;
import com.dronelink.core.mission.command.camera.ExposureCompensationCameraCommand;
import com.dronelink.core.mission.command.camera.ExposureCompensationStepCameraCommand;
import com.dronelink.core.mission.command.camera.ExposureModeCameraCommand;
import com.dronelink.core.mission.command.camera.FileIndexModeCameraCommand;
import com.dronelink.core.mission.command.camera.FocusCameraCommand;
import com.dronelink.core.mission.command.camera.FocusModeCameraCommand;
import com.dronelink.core.mission.command.camera.ISOCameraCommand;
import com.dronelink.core.mission.command.camera.MechanicalShutterCameraCommand;
import com.dronelink.core.mission.command.camera.MeteringModeCameraCommand;
import com.dronelink.core.mission.command.camera.ModeCameraCommand;
import com.dronelink.core.mission.command.camera.PhotoAspectRatioCameraCommand;
import com.dronelink.core.mission.command.camera.PhotoFileFormatCameraCommand;
import com.dronelink.core.mission.command.camera.PhotoIntervalCameraCommand;
import com.dronelink.core.mission.command.camera.PhotoModeCameraCommand;
import com.dronelink.core.mission.command.camera.SaturationCameraCommand;
import com.dronelink.core.mission.command.camera.SharpnessCameraCommand;
import com.dronelink.core.mission.command.camera.ShutterSpeedCameraCommand;
import com.dronelink.core.mission.command.camera.SpotMeteringTargetCameraCommand;
import com.dronelink.core.mission.command.camera.StartCaptureCameraCommand;
import com.dronelink.core.mission.command.camera.StopCaptureCameraCommand;
import com.dronelink.core.mission.command.camera.StorageLocationCameraCommand;
import com.dronelink.core.mission.command.camera.VideoFileCompressionStandardCameraCommand;
import com.dronelink.core.mission.command.camera.VideoFileFormatCameraCommand;
import com.dronelink.core.mission.command.camera.VideoResolutionFrameRateCameraCommand;
import com.dronelink.core.mission.command.camera.VideoStandardCameraCommand;
import com.dronelink.core.mission.command.camera.WhiteBalanceCustomCameraCommand;
import com.dronelink.core.mission.command.camera.WhiteBalancePresetCameraCommand;
import com.dronelink.core.mission.command.drone.CollisionAvoidanceDroneCommand;
import com.dronelink.core.mission.command.drone.ConnectionFailSafeBehaviorDroneCommand;
import com.dronelink.core.mission.command.drone.DroneCommand;
import com.dronelink.core.mission.command.drone.FlightAssistantDroneCommand;
import com.dronelink.core.mission.command.drone.LandingGearAutomaticMovementDroneCommand;
import com.dronelink.core.mission.command.drone.LandingGearDeployDroneCommand;
import com.dronelink.core.mission.command.drone.LandingGearDroneCommand;
import com.dronelink.core.mission.command.drone.LandingGearRetractDroneCommand;
import com.dronelink.core.mission.command.drone.LandingProtectionDroneCommand;
import com.dronelink.core.mission.command.drone.LightbridgeChannelDroneCommand;
import com.dronelink.core.mission.command.drone.LightbridgeChannelSelectionModeDroneCommand;
import com.dronelink.core.mission.command.drone.LightbridgeDroneCommand;
import com.dronelink.core.mission.command.drone.LightbridgeFrequencyBandDroneCommand;
import com.dronelink.core.mission.command.drone.LowBatteryWarningThresholdDroneCommand;
import com.dronelink.core.mission.command.drone.MaxAltitudeDroneCommand;
import com.dronelink.core.mission.command.drone.MaxDistanceDroneCommand;
import com.dronelink.core.mission.command.drone.MaxDistanceLimitationDroneCommand;
import com.dronelink.core.mission.command.drone.OcuSyncChannelDroneCommand;
import com.dronelink.core.mission.command.drone.OcuSyncChannelSelectionModeDroneCommand;
import com.dronelink.core.mission.command.drone.OcuSyncDroneCommand;
import com.dronelink.core.mission.command.drone.OcuSyncFrequencyBandDroneCommand;
import com.dronelink.core.mission.command.drone.PrecisionLandingDroneCommand;
import com.dronelink.core.mission.command.drone.ReturnHomeAltitudeDroneCommand;
import com.dronelink.core.mission.command.drone.ReturnHomeObstacleAvoidanceDroneCommand;
import com.dronelink.core.mission.command.drone.ReturnHomeRemoteObstacleAvoidanceDroneCommand;
import com.dronelink.core.mission.command.drone.SeriousLowBatteryWarningThresholdDroneCommand;
import com.dronelink.core.mission.command.drone.SmartReturnHomeDroneCommand;
import com.dronelink.core.mission.command.drone.UpwardsAvoidanceDroneCommand;
import com.dronelink.core.mission.command.drone.VisionAssistedPositioningDroneCommand;
import com.dronelink.core.mission.command.gimbal.GimbalCommand;
import com.dronelink.core.mission.command.gimbal.ModeGimbalCommand;
import com.dronelink.core.mission.command.gimbal.OrientationGimbalCommand;
import com.dronelink.core.mission.core.Message;
import com.dronelink.core.mission.core.Orientation3;
import com.dronelink.core.mission.core.Orientation3Optional;
import com.dronelink.core.mission.core.enums.CameraMode;
import com.dronelink.core.mission.core.enums.GimbalMode;
import com.dronelink.dji.adapters.DJICameraStateAdapter;
import com.dronelink.dji.adapters.DJIDroneAdapter;
import com.dronelink.dji.adapters.DJIDroneStateAdapter;
import com.dronelink.dji.adapters.DJIGimbalAdapter;
import com.dronelink.dji.adapters.DJIGimbalStateAdapter;
import com.dronelink.dji.adapters.DJIRemoteControllerStateAdapter;

import java.util.Date;
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
import dji.common.camera.ExposureSettings;
import dji.common.camera.ResolutionAndFrameRate;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.camera.SystemState;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LandingGearState;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.VisionSensorPosition;
import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.GimbalState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.product.Model;
import dji.common.remotecontroller.HardwareState;
import dji.common.util.CommonCallbacks;
import dji.common.util.DJIParamCapability;
import dji.sdk.airlink.AirLink;
import dji.sdk.airlink.LightbridgeLink;
import dji.sdk.airlink.OcuSyncLink;
import dji.sdk.base.BaseComponent;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.LandingGear;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.MediaFile;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;

public class DJIDroneSession implements DroneSession {
    private static final String TAG = DJIDroneSession.class.getCanonicalName();

    private final Context context;
    private final DJIDroneAdapter adapter;

    private final Date opened = new Date();
    private boolean closed = false;

    private final List<Listener> listeners = new LinkedList<>();
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private final CommandQueue droneCommands = new CommandQueue();
    private final MultiChannelCommandQueue cameraCommands = new MultiChannelCommandQueue();
    private final MultiChannelCommandQueue gimbalCommands = new MultiChannelCommandQueue();

    private ExecutorService stateSerialQueue = Executors.newSingleThreadExecutor();
    private final DJIDroneStateAdapter state = new DJIDroneStateAdapter();

    private ExecutorService remoteControllerSerialQueue = Executors.newSingleThreadExecutor();
    private DatedValue<HardwareState> remoteControllerState;

    private ExecutorService cameraSerialQueue = Executors.newSingleThreadExecutor();
    private SparseArray<DatedValue<SystemState>> cameraStates = new SparseArray<>();
    private SparseArray<DatedValue<StorageState>> cameraStorageStates = new SparseArray<>();
    private SparseArray<DatedValue<ExposureSettings>> cameraExposureSettings = new SparseArray<>();

    private ExecutorService gimbalSerialQueue = Executors.newSingleThreadExecutor();
    private SparseArray<DatedValue<GimbalState>> gimbalStates = new SparseArray<>();

    public DJIDroneSession(final Context context, final Aircraft drone) {
        this.context = context;
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

                        droneCommands.process();
                        cameraCommands.process();
                        gimbalCommands.process();

                        gimbalSerialQueue.execute(new Runnable() {
                            @Override
                            public void run() {
                                //work-around for this issue: https://support.dronelink.com/hc/en-us/community/posts/360034749773-Seeming-to-have-a-Heading-error-
                                for (final GimbalAdapter gimbalAdapter : adapter.getGimbals()) {
                                    if (gimbalAdapter instanceof DJIGimbalAdapter) {
                                        final DJIGimbalAdapter djiGimbalAdapter = (DJIGimbalAdapter)gimbalAdapter;
                                        Rotation.Builder rotationBuilder = djiGimbalAdapter.getPendingSpeedRotation();
                                        djiGimbalAdapter.setPendingSpeedRotationBuilder(null);
                                        final Map<CapabilityKey, DJIParamCapability> gimbalCapabilities = djiGimbalAdapter.gimbal.getCapabilities();
                                        if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_YAW) && gimbalCapabilities.get(CapabilityKey.ADJUST_YAW).isSupported()) {
                                            final DatedValue<GimbalState> gimbalState = gimbalStates.get(djiGimbalAdapter.getIndex());
                                            if (gimbalState != null && gimbalState.value.getMode() == dji.common.gimbal.GimbalMode.YAW_FOLLOW) {
                                                if (rotationBuilder == null) {
                                                    rotationBuilder = new Rotation.Builder();
                                                    rotationBuilder.mode(RotationMode.SPEED);
                                                }
                                                rotationBuilder.yaw((float)Math.min(Math.max(-gimbalState.value.getYawRelativeToAircraftHeading() * 0.1, -5.0), 5.0));
                                            }
                                        }

                                        if (rotationBuilder != null) {
                                            djiGimbalAdapter.gimbal.rotate(rotationBuilder.build(), null);
                                        }
                                    }
                                }
                            }
                        });
                        sleep(100);
                    }
                }
                catch (final InterruptedException e) {

                }
            }
        }.start();
    }

    private void initDrone() {
        Log.i(TAG, "Drone session opened");

        final Aircraft drone = adapter.getDrone();
        if (drone.getFlightController() != null) {
            initFlightController(adapter.getDrone().getFlightController());
        }

        final RemoteController remoteController = drone.getRemoteController();
        if (remoteController != null) {
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

        flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(final String s) {
                state.serialNumber = s;
                if (state.serialNumber != null) {
                    Log.i(TAG, "Serial number: " + s);
                }

                //doing this a second time because sometimes it isn't ready by the above line
                if (state.firmwarePackageVersion == null) {
                    state.firmwarePackageVersion = drone.getFirmwarePackageVersion();
                    if (state.firmwarePackageVersion == null) {
                        state.firmwarePackageVersion = "";
                    } else {
                        Log.i(TAG, "Firmware package version: " + state.firmwarePackageVersion);
                    }
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        flightController.setMultipleFlightModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    Log.i(TAG, "Flight controller multiple flight mode enabled");
                }
            }
        });

        flightController.setNoviceModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    Log.i(TAG, "Flight controller novice mode disabled");
                }
            }
        });

        flightController.setStateCallback(new FlightControllerState.Callback() {
            private boolean areMotorsOnPrevious = false;

            @Override
            public void onUpdate(@NonNull final FlightControllerState flightControllerStateUpdated) {
                stateSerialQueue.execute(new Runnable() {
                    @Override
                    public void run() {
                        state.flightControllerState = new DatedValue<>(flightControllerStateUpdated);
                        if (areMotorsOnPrevious != flightControllerStateUpdated.areMotorsOn()) {
                            onMotorsChanged(flightControllerStateUpdated.areMotorsOn());
                        }
                        areMotorsOnPrevious = flightControllerStateUpdated.areMotorsOn();
                    }
                });
            }
        });

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
    }

    private void initCamera(final Camera camera) {
        Log.i(TAG, String.format("Camera[%d] connected", camera.getIndex()));
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
                        cameraExposureSettings.put(camera.getIndex(), new DatedValue<>(exposureSettings));
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
                        Orientation3 orientation = state.getMissionOrientation();
                        final DatedValue<GimbalStateAdapter> gimbalState = getGimbalState(camera.getIndex());
                        if (gimbalState != null) {
                            orientation.x = gimbalState.value.getMissionOrientation().x;
                            orientation.y = gimbalState.value.getMissionOrientation().y;
                            if (gimbalState.value.getMissionMode() == GimbalMode.FREE) {
                                orientation.z = gimbalState.value.getMissionOrientation().z;
                            }
                        } else {
                            orientation.x = 0.0;
                            orientation.y = 0.0;
                        }

                        final DatedValue<DroneStateAdapter> state = getState();
                        onCameraFileGenerated(new DJICameraFile(camera.getIndex(), mediaFile, state.value.getLocation(), state.value.getAltitude(), orientation));
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
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    Log.i(TAG, "Set media file custom information: " + xmp);
                }
                else {
                    Log.i(TAG, "Unable to set media file custom information: " + djiError.getDescription());
                }
            }
        });

        Log.i(TAG, "camera.getDisplayName(): " + camera.getDisplayName());
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
        gimbal.setPitchRangeExtensionEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    Log.i(TAG, String.format("Gimbal[%d] pitch range extension enabled", gimbal.getIndex()));
                }
            }
        });
    }

    protected void componentConnected(final BaseComponent component) {
        if (component instanceof FlightController) {
            initFlightController((FlightController)component);
        }
        else if (component instanceof Camera) {
            initCamera((Camera)component);
        }
        else if (component instanceof Gimbal) {
            initGimbal((Gimbal)component);
        }
    }

    protected void componentDisconnected(final BaseComponent component) {
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
                    cameraStorageStates.put(camera.getIndex(), null);
                    cameraExposureSettings.put(camera.getIndex(), null);
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
        return System.currentTimeMillis() - getState().date.getTime() > 1000;
    }

    @Override
    public Message getDisengageReason() {
        if (adapter.getDrone().getFlightController() == null) {
            return new Message(context.getString(R.string.MissionDisengageReason_drone_control_unavailable_title));
        }


        if (state.flightControllerState == null) {
            return new Message(context.getString(R.string.MissionDisengageReason_telemetry_unavailable_title));
        }

        if (isTelemetryDelayed()) {
            return new Message(context.getString(R.string.MissionDisengageReason_telemetry_delayed_title));
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

    private void onCommandExecuted(final com.dronelink.core.mission.command.Command command) {
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

    private void onCommandFinished(final com.dronelink.core.mission.command.Command command, final CommandError error) {
        final DJIDroneSession self = this;
        CommandError errorResolved = error;
        if (error != null && error.code == DJIError.COMMAND_NOT_SUPPORTED_BY_HARDWARE.getErrorCode()) {
            Log.i(TAG, String.format("Ignoring command failure: product not supported (%s)", command.id));
            errorResolved = null;
        }

        final CommandError errorResolvedFinal = errorResolved;
        listenerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (final Listener listener : listeners) {
                    listener.onCommandFinished(self, command, errorResolvedFinal);
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
    public void addCommand(final com.dronelink.core.mission.command.Command command) throws CommandTypeUnhandledException {
        if (command instanceof DroneCommand) {
            droneCommands.addCommand(
                    new Command(
                            command.id,
                            command.type,
                            new Command.Executor() {
                                @Override
                                public CommandError execute(final Command.Finisher finished) {
                                    onCommandExecuted(command);
                                    return executeDroneCommand((DroneCommand)command, finished);
                                }
                            },
                            new Command.Finisher() {
                                @Override
                                public void execute(final CommandError error) {
                                    onCommandFinished(command, error);
                                }
                            },
                            command.getConfig()));
            return;
        }

        if (command instanceof CameraCommand) {
            droneCommands.addCommand(
                    new Command(
                            command.id,
                            command.type,
                            new Command.Executor() {
                                @Override
                                public CommandError execute(final Command.Finisher finished) {
                                    onCommandExecuted(command);
                                    return executeCameraCommand((CameraCommand)command, finished);
                                }
                            },
                            new Command.Finisher() {
                                @Override
                                public void execute(final CommandError error) {
                                    onCommandFinished(command, error);
                                }
                            },
                            command.getConfig()));
            return;
        }

        if (command instanceof GimbalCommand) {
            droneCommands.addCommand(
                    new Command(
                            command.id,
                            command.type,
                            new Command.Executor() {
                                @Override
                                public CommandError execute(final Command.Finisher finished) {
                                    onCommandExecuted(command);
                                    return executeGimbalCommand((GimbalCommand)command, finished);
                                }
                            },
                            new Command.Finisher() {
                                @Override
                                public void execute(final CommandError error) {
                                    onCommandFinished(command, error);
                                }
                            },
                            command.getConfig()));
            return;
        }

        throw new CommandTypeUnhandledException();
    }

    @Override
    public void removeCommands() {
        droneCommands.removeAll();
        cameraCommands.removeAll();
        gimbalCommands.removeAll();
    }

    @Override
    public DroneControlSession createControlSession(final Context context) {
        return new DJIControlSession(context, this);
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
        try {
            return cameraSerialQueue.submit(new Callable<DatedValue<CameraStateAdapter>>() {
                @Override
                public DatedValue<CameraStateAdapter> call() {
                    final DatedValue<SystemState> systemState = cameraStates.get(channel);
                    if (systemState == null) {
                        return null;
                    }

                    final DatedValue<StorageState> storageState = cameraStorageStates.get(channel);
                    final DatedValue<ExposureSettings> exposureSettings = cameraExposureSettings.get(channel);
                    final CameraStateAdapter cameraStateAdapter = new DJICameraStateAdapter(systemState.value, storageState == null ? null : storageState.value, exposureSettings == null ? null : exposureSettings.value);
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
    public void close() {
        this.closed = true;
    }

    protected void sendResetVelocityCommand(final CommonCallbacks.CompletionCallback completion) {
        adapter.sendResetVelocityCommand(completion);
    }

    protected void sendResetGimbalCommands() {
        for (final Gimbal gimbal : adapter.getDrone().getGimbals()) {
            final Map<CapabilityKey, DJIParamCapability> gimbalCapabilities = gimbal.getCapabilities();
            final Rotation.Builder rotation = new Rotation.Builder();
            rotation.mode(RotationMode.ABSOLUTE_ANGLE);
            rotation.time(DronelinkDJI.GimbalRotationMinTime);
            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_PITCH) && gimbalCapabilities.get(CapabilityKey.ADJUST_PITCH).isSupported()) {
                rotation.pitch(-12);
            }
            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_ROLL) &&  gimbalCapabilities.get(CapabilityKey.ADJUST_ROLL).isSupported()) {
                rotation.roll(0);
            }
            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_YAW) &&  gimbalCapabilities.get(CapabilityKey.ADJUST_YAW).isSupported()) {
                rotation.yaw(0);
            }
            gimbal.rotate(rotation.build(), null);
        }
    }

    protected void sendResetCameraCommands() {
        for (final Camera camera : adapter.getDrone().getCameras()) {
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
                finished.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
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
                error.execute(new CommandError(djiError.getDescription(), djiError.getErrorCode()));
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
            flightAssistant.getUpwardAvoidanceEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((UpwardsAvoidanceDroneCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            flightAssistant.setUpwardAvoidanceEnabled(target, createCompletionCallback(finished));
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

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }

    private CommandError executeCameraCommand(final CameraCommand command, final Command.Finisher finished) {
        final List<Camera> cameras = adapter.getDrone().getCameras();
        final DatedValue<CameraStateAdapter> state = getCameraState(command.channel);
        if (command.channel > cameras.size() || state == null || !(state.value instanceof DJICameraStateAdapter)) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_unavailable_title));
        }
        final Camera camera = cameras.get(command.channel);
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
            camera.getAutoAEUnlockEnabled(createCompletionCallbackWith(new Command.FinisherWith<Boolean>() {
                @Override
                public void execute(final Boolean current) {
                    final Boolean target = ((AutoExposureLockCameraCommand) command).enabled;
                    Command.conditionallyExecute(!target.equals(current), finished, new Command.ConditionalExecutor() {
                        @Override
                        public void execute() {
                            camera.setAutoAEUnlockEnabled(target, createCompletionCallback(finished));
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
            Command.conditionallyExecute(djiState.exposureSettings.getExposureCompensation() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setExposureCompensation(target, createCompletionCallback(finished));
                }
            });
            return null;
        }

        if (command instanceof ExposureCompensationStepCameraCommand) {
            final SettingsDefinitions.ExposureCompensation target = DronelinkDJI.getCameraExposureCompensation(state.value.getMissionExposureCompensation().offset(((ExposureCompensationStepCameraCommand) command).exposureCompensationSteps));
            Command.conditionallyExecute(djiState.exposureSettings.getExposureCompensation() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setExposureCompensation(target, createCompletionCallback(finished));
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
            camera.setFocusTarget(new PointF((float)focusCameraCommand.focusTarget.x, (float)focusCameraCommand.focusTarget.y), createCompletionCallback(finished));
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
            final CameraMode target = ((ModeCameraCommand) command).mode;
            Command.conditionallyExecute(state.value.getMissionMode() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    camera.setMode(DronelinkDJI.getCameraMode(target), createCompletionCallback(finished));
                }
            });
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
            switch (state.value.getMissionMode()) {
                case PHOTO:
                    if (state.value.isCapturingPhotoInterval()) {
                        Log.d(TAG, "Camera start capture skipped, already shooting interval photos");
                        finished.execute(null);
                    }
                    else {
                        Log.d(TAG, "Camera start capture photo");
                        camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finished.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                                    }
                                }, 1000);
                            }
                        });
                    }
                    break;

                case VIDEO:
                    if (state.value.isCapturingVideo()) {
                        Log.d(TAG, "Camera start capture skipped, already recording video");
                        finished.execute(null);
                    }
                    else {
                        Log.d(TAG, "Camera start capture video");
                        camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finished.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                                    }
                                }, 1000);
                            }
                        });
                    }
                    break;

                case PLAYBACK:
                case DOWNLOAD:
                case BROADCAST:
                case UNKNOWN:
                    Log.i(TAG, "Camera start capture invalid mode: " + state.value.getMissionMode().toString());
                    return new CommandError(context.getString(R.string.MissionDisengageReason_drone_camera_mode_invalid_title));
            }
            return null;
        }

        if (command instanceof StopCaptureCameraCommand) {
            switch (state.value.getMissionMode()) {
                case PHOTO:
                    if (state.value.isCapturingPhotoInterval()) {
                        Log.d(TAG, "Camera stop capture interval photo");
                        camera.stopShootPhoto(createCompletionCallback(finished));
                    }
                    else {
                        Log.d(TAG, "Camera stop capture skipped, not shooting interval photos");
                        finished.execute(null);
                    }
                    break;

                case VIDEO:
                    if (state.value.isCapturingVideo()) {
                        Log.d(TAG, "Camera stop capture video");
                        camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finished.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                                    }
                                }, 2000);
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
                    Log.i(TAG, "Camera start capture invalid mode: " + state.value.getMissionMode().toString());
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

    private CommandError executeGimbalCommand(final GimbalCommand command, final Command.Finisher finished) {
        final List<Gimbal> gimbals = adapter.getDrone().getGimbals();
        final DatedValue<GimbalStateAdapter> state = getGimbalState(command.channel);
        if (command.channel > gimbals.size() || state == null) {
            return new CommandError(context.getString(R.string.MissionDisengageReason_drone_gimbal_unavailable_title));
        }
        final Gimbal gimbal = gimbals.get(command.channel);
        final Map<CapabilityKey, DJIParamCapability> gimbalCapabilities = gimbal.getCapabilities();

        if (command instanceof ModeGimbalCommand) {
            final GimbalMode target = ((ModeGimbalCommand) command).mode;
            Command.conditionallyExecute(state.value.getMissionMode() != target, finished, new Command.ConditionalExecutor() {
                @Override
                public void execute() {
                    gimbal.setMode(DronelinkDJI.getGimbalMode(target), createCompletionCallback(finished));
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
            rotation.mode(RotationMode.ABSOLUTE_ANGLE);
            rotation.time(DronelinkDJI.GimbalRotationMinTime);

            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_PITCH) && gimbalCapabilities.get(CapabilityKey.ADJUST_PITCH).isSupported() && orientation.getPitch() != null) {
                double pitch = Math.toDegrees(orientation.getPitch());
                if (Math.abs(pitch + 90) < 0.1) {
                    pitch = -89.9;
                }
                rotation.pitch((float)pitch);
            }

            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_ROLL) && gimbalCapabilities.get(CapabilityKey.ADJUST_ROLL).isSupported() && orientation.getRoll() != null) {
                rotation.roll((float)Math.toDegrees(orientation.getRoll()));
            }

            if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_YAW) && gimbalCapabilities.get(CapabilityKey.ADJUST_YAW).isSupported()) {
                if (state.value.getMissionMode() == com.dronelink.core.mission.core.enums.GimbalMode.FREE) {
                    rotation.yaw((float) Math.toDegrees(orientation.getYaw()));
                }
                else {
                    rotation.yaw(0);
                }
            }

            gimbal.rotate(rotation.build(), createCompletionCallback(finished));
            return null;
        }

        return new CommandError(context.getString(R.string.MissionDisengageReason_command_type_unhandled));
    }
}
