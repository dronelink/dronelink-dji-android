//  DJIGimbalAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.Convert;
import com.dronelink.core.Dronelink;
import com.dronelink.core.Kernel;
import com.dronelink.core.adapters.EnumElement;
import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.kernel.command.gimbal.VelocityGimbalCommand;
import com.dronelink.core.kernel.core.enums.GimbalMode;
import com.dronelink.dji.DronelinkDJI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.sdk.gimbal.Gimbal;

public class DJIGimbalAdapter implements GimbalAdapter {
    private ExecutorService serialQueue = Executors.newSingleThreadExecutor();

    public final Gimbal gimbal;
    private Rotation.Builder pendingSpeedRotationBuilder;

    public DJIGimbalAdapter(final Gimbal gimbal) {
        this.gimbal = gimbal;
    }

    @Override
    public int getIndex() {
        return gimbal.getIndex();
    }

    @Override
    public void sendVelocityCommand(final VelocityGimbalCommand command, final GimbalMode mode) {
        final Rotation.Builder rotationBuilder = new Rotation.Builder();
        rotationBuilder.mode(RotationMode.SPEED);

        if (DronelinkDJI.isAdjustPitchSupported(gimbal)) {
            rotationBuilder.pitch((float) Math.max(-90, Math.min(90, Math.toDegrees(command.velocity.getPitch()))));
        }

        if (DronelinkDJI.isAdjustRollSupported(gimbal)) {
            rotationBuilder.roll((float) Math.max(-90, Math.min(90, Math.toDegrees(command.velocity.getRoll()))));
        }

        if (mode == GimbalMode.FREE || DronelinkDJI.isAdjustYaw360Supported(gimbal)) {
            rotationBuilder.yaw((float)Math.toDegrees(command.velocity.getYaw()));
        }

        setPendingSpeedRotationBuilder(rotationBuilder);
    }

    @Override
    public void reset() {
        gimbal.reset(null);
    }

    @Override
    public void fineTuneRoll(double roll) {
        gimbal.fineTuneRollInDegrees((float)Convert.RadiansToDegrees(roll), null);
    }

    public Rotation.Builder getPendingSpeedRotation() {
        try {
            return serialQueue.submit(new Callable<Rotation.Builder>() {
                @Override
                public Rotation.Builder call() {
                    return pendingSpeedRotationBuilder;
                }
            }).get();
        }
        catch (final ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public void setPendingSpeedRotationBuilder(final Rotation.Builder newPendingSpeedRotationBuilder) {
        serialQueue.execute(new Runnable() {
            @Override
            public void run() {
                pendingSpeedRotationBuilder = newPendingSpeedRotationBuilder;
            }
        });
    }

    @Override
    public List<EnumElement> getEnumElements(final String parameter) {
        final Map<String, String> enumDefinition = Dronelink.getInstance().getEnumDefinition(parameter);
        if (enumDefinition == null) {
            return null;
        }

        final List<String> range = new ArrayList<>();
        switch (parameter) {
            case "GimbalMode":
                range.add(Kernel.enumRawValue(GimbalMode.YAW_FOLLOW));
                if (DronelinkDJI.isAdjustYaw360Supported(gimbal)) {
                    range.add(Kernel.enumRawValue(GimbalMode.FREE));
                }
                range.add(Kernel.enumRawValue(GimbalMode.FPV));
                break;
        }

        final List<EnumElement> enumElements = new ArrayList<>();
        for (final String rangeValue : range) {
            if (rangeValue != null && !rangeValue.equals("unknown")) {
                final String enumDisplay = enumDefinition.get(rangeValue);
                if (enumDisplay != null) {
                    enumElements.add(new EnumElement(enumDisplay, rangeValue));
                }
            }
        }

        return enumElements.isEmpty() ? null : enumElements;
    }
}