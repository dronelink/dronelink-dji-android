//  DJIDroneAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.Convert;
import com.dronelink.core.adapters.BatteryAdapter;
import com.dronelink.core.adapters.CameraAdapter;
import com.dronelink.core.adapters.DroneAdapter;
import com.dronelink.core.adapters.EnumElement;
import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.adapters.RemoteControllerAdapter;
import com.dronelink.core.command.Command;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.kernel.command.drone.RemoteControllerSticksDroneCommand;
import com.dronelink.core.kernel.command.drone.VelocityDroneCommand;
import com.dronelink.core.kernel.core.Vector2;
import com.dronelink.dji.DronelinkDJI;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;

public class DJIDroneAdapter implements DroneAdapter {
    public final Aircraft drone;
    private final SortedMap<Integer, RemoteControllerAdapter> remoteControllers = new TreeMap<>();
    private final SortedMap<Integer, CameraAdapter> cameras = new TreeMap<>();
    private final SortedMap<Integer, GimbalAdapter> gimbals = new TreeMap<>();
    private long previousVelocityCommandMillis = 0;

    public DJIDroneAdapter(Aircraft drone) {
        this.drone = drone;
    }

    public Aircraft getDrone() {
        return this.drone;
    }

    @Override
    public Collection<RemoteControllerAdapter> getRemoteControllers() {
        getRemoteController(0);
        return remoteControllers.values();
    }

    @Override
    public Collection<CameraAdapter> getCameras() {
        final List<Camera> djiCameras = drone.getCameras();
        if (djiCameras != null) {
            for (final Camera camera : djiCameras) {
                getCamera(camera.getIndex());
            }
        }
        return cameras.values();
    }

    @Override
    public Collection<GimbalAdapter> getGimbals() {
        final List<Gimbal> djiGimbals = drone.getGimbals();
        if (djiGimbals != null) {
            for (final Gimbal gimbal : djiGimbals) {
                getGimbal(gimbal.getIndex());
            }
        }
        return gimbals.values();
    }

    @Override
    public Collection<BatteryAdapter> getBatteries() {
        //TODO
        return null;
    }

    @Override
    public RemoteControllerAdapter getRemoteController(final int channel) {
        final RemoteControllerAdapter remoteControllerAdapter = remoteControllers.get(channel);
        if (remoteControllerAdapter == null && channel < 1) {
            final RemoteController remoteController = drone.getRemoteController();
            if (remoteController != null) {
                return remoteControllers.put(channel, new DJIRemoteControllerAdapter(remoteController));
            }
        }
        return remoteControllerAdapter;
    }

    @Override
    public Integer getCameraChannel(final Integer videoFeedChannel) {
        if (!DronelinkDJI.isMultipleVideoFeedsEnabled(drone)) {
            return 0;
        }

        if (videoFeedChannel != null) {
            final VideoFeeder.VideoFeed videoFeed = DronelinkDJI.getVideoFeed(videoFeedChannel);
            if (videoFeed != null) {
                return DronelinkDJI.getCameraChannel(videoFeed.getVideoSource());
            }
        }

        for (int channel = 0; channel < 3; channel++) {
            final VideoFeeder.VideoFeed videoFeed = DronelinkDJI.getVideoFeed(channel);
            if (videoFeed != null) {
                return DronelinkDJI.getCameraChannel(videoFeed.getVideoSource());
            }
        }

        return null;
    }

    @Override
    public CameraAdapter getCamera(final int channel) {
        final CameraAdapter cameraAdapter = cameras.get(channel);
        if (cameraAdapter == null) {
            final Camera camera = DronelinkDJI.getCamera(drone, channel);
            if (camera != null) {
                return cameras.put(channel, new DJICameraAdapter(camera));
            }
        }
        return cameraAdapter;
    }

    @Override
    public GimbalAdapter getGimbal(final int channel) {
        final GimbalAdapter gimbalAdapter = gimbals.get(channel);
        if (gimbalAdapter == null) {
            final Gimbal gimbal = DronelinkDJI.getGimbal(drone, channel);
            if (gimbal != null) {
                return gimbals.put(channel, new DJIGimbalAdapter(gimbal));
            }
        }
        return gimbalAdapter;
    }

    @Override
    public BatteryAdapter getBattery(final int index) {
        //TODO
        return null;
    }

    @Override
    public void sendVelocityCommand(final VelocityDroneCommand velocityCommand) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            return;
        }

        if (ignoreVelocityCommand()) {
            return;
        }

        previousVelocityCommandMillis = System.currentTimeMillis();
        flightController.setVirtualStickAdvancedModeEnabled(true);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.GROUND);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        if (velocityCommand == null) {
            flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, 0, 0), null);
            return;
        }

        flightController.setYawControlMode(velocityCommand.heading == null ? YawControlMode.ANGULAR_VELOCITY : YawControlMode.ANGLE);
        final Vector2 horizontal = velocityCommand.velocity.getHorizontal();
        horizontal.magnitude = Math.min(DronelinkDJI.DroneMaxVelocity, horizontal.magnitude);
        flightController.sendVirtualStickFlightControlData(new FlightControlData((float)horizontal.getY(),
                (float)horizontal.getX(),
                (float)Math.toDegrees(velocityCommand.heading == null ? velocityCommand.velocity.getRotational() : Convert.AngleDifferenceSigned(velocityCommand.heading, 0)),
                Math.min(4.0f, Math.max(-4.0f, (float)velocityCommand.velocity.getVertical()))), null);
    }

    //kluge: if we send commands to any P4 model faster than 150ms, it results in sudden stopping and resuming every few seconds!
    private boolean ignoreVelocityCommand() {
        if (drone.getModel() != null) {
            switch (drone.getModel()) {
                case PHANTOM_4:
                case PHANTOM_4_PRO:
                case PHANTOM_4_PRO_V2:
                case PHANTOM_4_ADVANCED:
                case PHANTOM_4_RTK:
                    return System.currentTimeMillis() - previousVelocityCommandMillis < 150;
                default:
                    break;
            }
        }

        return false;
    }

    @Override
    public void sendRemoteControllerSticksCommand(final RemoteControllerSticksDroneCommand remoteControllerSticks) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            return;
        }

        flightController.setRollPitchControlMode(RollPitchControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        if (remoteControllerSticks == null) {
            flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, 0, 0), null);
            return;
        }

        flightController.setYawControlMode(remoteControllerSticks.heading == null ? YawControlMode.ANGULAR_VELOCITY : YawControlMode.ANGLE);
        flightController.sendVirtualStickFlightControlData(new FlightControlData((float)(-remoteControllerSticks.rightStick.y * 30),
                (float)(remoteControllerSticks.rightStick.x * 30),
                remoteControllerSticks.heading == null ? (float)(remoteControllerSticks.leftStick.x * 100) : (float)Math.toDegrees(Convert.AngleDifferenceSigned(remoteControllerSticks.heading, 0)),
                (float)(remoteControllerSticks.leftStick.y * 4.0)), null);
    }

    @Override
    public void startTakeoff(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.startPrecisionTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (djiError == null) {
                    if (finisher != null) {
                        finisher.execute(null);
                    }
                    return;
                }

                flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (finisher != null) {
                            finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void startLand(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void stopLand(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.cancelLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void startReturnHome(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void stopReturnHome(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.cancelGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void startCompassCalibration(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.getCompass().startCalibration(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void stopCompassCalibration(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError("Flight controller unavailable"));
            }
            return;
        }

        flightController.getCompass().stopCalibration(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    public void sendResetVelocityCommand(final CommonCallbacks.CompletionCallback completion) {
        final FlightController flightController = drone.getFlightController();
        if (flightController != null) {
            flightController.setVirtualStickAdvancedModeEnabled(true);
            flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.GROUND);
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, 0, 0), completion);
        }
    }

    @Override
    public List<EnumElement> getEnumElements(final String parameter) {
        return null;
    }
}
