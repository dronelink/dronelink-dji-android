//  DJIRemoteControllerAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.RemoteControllerAdapter;

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