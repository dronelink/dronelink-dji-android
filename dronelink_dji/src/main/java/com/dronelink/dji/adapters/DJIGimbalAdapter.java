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

public class DJIGimbalAdapter implements GimbalAdapter {
    public final Gimbal gimbal;
    public Rotation.Builder pendingSpeedRotationBuilder;

    public DJIGimbalAdapter(final Gimbal gimbal) {
        this.gimbal = gimbal;
    }

    @Override
    public int getIndex() {
        return gimbal.getIndex();
    }

    @Override
    public void sendVelocityCommand(final VelocityGimbalCommand command, final GimbalMode mode) {
        final Map<CapabilityKey, DJIParamCapability> gimbalCapabilities = gimbal.getCapabilities();
        final Rotation.Builder rotationBuilder = new Rotation.Builder();
        rotationBuilder.mode(RotationMode.SPEED);

        if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_PITCH) &&  gimbalCapabilities.get(CapabilityKey.ADJUST_PITCH).isSupported()) {
            rotationBuilder.pitch((float) Math.toDegrees(command.velocity.getPitch()));
        }

        if (gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_ROLL) && gimbalCapabilities.get(CapabilityKey.ADJUST_ROLL).isSupported()) {
            rotationBuilder.roll((float) Math.toDegrees(command.velocity.getRoll()));
        }

        if (mode == GimbalMode.FREE && gimbalCapabilities != null && gimbalCapabilities.containsKey(CapabilityKey.ADJUST_YAW) && gimbalCapabilities.get(CapabilityKey.ADJUST_YAW).isSupported()) {
            rotationBuilder.yaw((float)Math.toDegrees(command.velocity.getYaw()));
        }

        pendingSpeedRotationBuilder = rotationBuilder;
    }
}