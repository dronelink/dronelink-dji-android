//  DJIDroneAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.Convert;
import com.dronelink.core.adapters.CameraAdapter;
import com.dronelink.core.adapters.DroneAdapter;
import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.command.Command;
import com.dronelink.core.mission.command.drone.VelocityDroneCommand;
import com.dronelink.core.mission.core.Vector2;
import com.dronelink.dji.DronelinkDJI;

import java.util.Collection;
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
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;

public class DJIDroneAdapter implements DroneAdapter {
    private final Aircraft drone;
    private final SortedMap<Integer, CameraAdapter> cameras = new TreeMap<>();
    private final SortedMap<Integer, GimbalAdapter> gimbals = new TreeMap<>();

    public DJIDroneAdapter(Aircraft drone) {
        this.drone = drone;
    }

    public Aircraft getDrone() {
        return this.drone;
    }

    @Override
    public Collection<CameraAdapter> getCameras() {
        for (final Camera camera : drone.getCameras()) {
            getCamera(camera.getIndex());
        }
        return cameras.values();
    }

    @Override
    public Collection<GimbalAdapter> getGimbals() {
        for (final Gimbal gimbal : drone.getGimbals()) {
            getGimbal(gimbal.getIndex());
        }
        return gimbals.values();
    }

    @Override
    public CameraAdapter getCamera(int channel) {
        final CameraAdapter cameraAdapter = cameras.get(channel);
        if (cameraAdapter == null && channel < drone.getCameras().size()) {
            final Camera camera = drone.getCameras().get(channel);
            if (camera != null) {
                return cameras.put(channel, new DJICameraAdapter(camera));
            }
        }
        return cameraAdapter;
    }

    @Override
    public GimbalAdapter getGimbal(int channel) {
        final GimbalAdapter gimbalAdapter = gimbals.get(channel);
        if (gimbalAdapter == null && channel < drone.getGimbals().size()) {
            final Gimbal gimbal = drone.getGimbals().get(channel);
            if (gimbal != null) {
                return gimbals.put(channel, new DJIGimbalAdapter(gimbal));
            }
        }
        return gimbalAdapter;
    }

    @Override
    public void sendVelocityCommand(final VelocityDroneCommand velocityCommand) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            return;
        }

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
                (float)velocityCommand.velocity.getVertical()), null);
    }

    @Override
    public void startGoHome(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute("Flight controller unavailable");
            }
            return;
        }

        flightController.startGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : djiError.getDescription());
                }
            }
        });
    }

    @Override
    public void startLanding(final Command.Finisher finisher) {
        final FlightController flightController = drone.getFlightController();
        if (flightController == null) {
            if (finisher != null) {
                finisher.execute("Flight controller unavailable");
            }
            return;
        }

        flightController.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : djiError.getDescription());
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
}
