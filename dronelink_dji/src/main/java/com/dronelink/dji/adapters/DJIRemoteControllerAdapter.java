//  DJIGimbalAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.GimbalAdapter;
import com.dronelink.core.adapters.RemoteControllerAdapter;
import com.dronelink.core.kernel.command.gimbal.VelocityGimbalCommand;
import com.dronelink.core.kernel.core.enums.GimbalMode;

import java.util.Map;

import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.DJIParamCapability;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.remotecontroller.RemoteController;

public class DJIRemoteControllerAdapter implements RemoteControllerAdapter {
    public final RemoteController remoteController;

    public DJIRemoteControllerAdapter(final RemoteController remoteController) {
        this.remoteController = remoteController;
    }

    @Override
    public int getIndex() {
        return 0;
    }
}