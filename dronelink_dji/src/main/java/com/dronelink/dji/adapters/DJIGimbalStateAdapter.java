//  DJIGimbalStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.Convert;
import com.dronelink.core.adapters.GimbalStateAdapter;
import com.dronelink.core.kernel.core.Orientation3;
import com.dronelink.core.kernel.core.enums.GimbalMode;

import dji.common.gimbal.Attitude;
import dji.common.gimbal.GimbalState;

public class DJIGimbalStateAdapter implements GimbalStateAdapter {
    public final GimbalState state;

    public DJIGimbalStateAdapter(final GimbalState state) {
        this.state = state;
    }

    @Override
    public GimbalMode getMode() {
        switch (state == null ? dji.common.gimbal.GimbalMode.UNKNOWN : state.getMode()) {
            case FREE: return GimbalMode.FREE;
            case FPV: return GimbalMode.FPV;
            case YAW_FOLLOW: return GimbalMode.YAW_FOLLOW;
            case UNKNOWN: return GimbalMode.UNKNOWN;
        }
        return GimbalMode.UNKNOWN;
    }

    @Override
    public Orientation3 getOrientation() {
        final Orientation3 orientation = new Orientation3();
        if (state != null) {
            final Attitude attitude = state.getAttitudeInDegrees();
            if (attitude != null) {
                orientation.x = Convert.DegreesToRadians(attitude.getPitch());
                orientation.y = Convert.DegreesToRadians(attitude.getRoll());
                orientation.z = Convert.DegreesToRadians(attitude.getYaw());
            }
        }
        return orientation;
    }
}