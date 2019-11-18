//  DJIGimbalAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.mission.command.gimbal.VelocityGimbalCommand;
import com.dronelink.core.mission.core.enums.GimbalMode;

import java.util.Map;

import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.DJIParamCapability;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;

public class DJIGimbalAdapter implements GimbalAdapter {
    private final Aircraft drone;
    private final int index;

    public DJIGimbalAdapter(final Aircraft drone, final int index) {
        this.drone = drone;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void sendVelocityCommand(final VelocityGimbalCommand command, final GimbalMode mode) {
        if (command.channel > drone.getGimbals().size()) {
            return;
        }

        final Gimbal gimbal = drone.getGimbals().get(command.channel);
        final Map<CapabilityKey, DJIParamCapability> gimbalCapabilities = gimbal.getCapabilities();
        final Rotation.Builder rotation = new Rotation.Builder();
        rotation.mode(RotationMode.SPEED);

        if (gimbalCapabilities != null && gimbalCapabilities.get(CapabilityKey.ADJUST_PITCH).isSupported()) {
            rotation.pitch((float) Math.toDegrees(command.velocity.getPitch()));
        }

        if (mode == GimbalMode.FREE && gimbalCapabilities != null && gimbalCapabilities.get(CapabilityKey.ADJUST_ROLL).isSupported()) {
            rotation.roll((float) Math.toDegrees(command.velocity.getRoll()));
        }

        if (mode == GimbalMode.FREE && gimbalCapabilities != null && gimbalCapabilities.get(CapabilityKey.ADJUST_YAW).isSupported()) {
            rotation.yaw((float)Math.toDegrees(command.velocity.getYaw()));
        }

        gimbal.rotate(rotation.build(), null);
    }
}