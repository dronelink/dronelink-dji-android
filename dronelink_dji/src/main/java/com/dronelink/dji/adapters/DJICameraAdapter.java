//  DJICameraAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraAdapter;

import dji.sdk.camera.Camera;

public class DJICameraAdapter implements CameraAdapter {
    private final Camera camera;

    public DJICameraAdapter(final Camera camera) {
        this.camera = camera;
    }

    @Override
    public String getModel() {
        return camera.getDisplayName();
    }

    @Override
    public int getIndex() {
        return camera.getIndex();
    }
}
