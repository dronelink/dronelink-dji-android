//  DJICameraAdapter.java
//  DronelinkCore
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraAdapter;

import dji.sdk.products.Aircraft;

public class DJICameraAdapter implements CameraAdapter {
    private final Aircraft drone;
    private final int index;

    public DJICameraAdapter(final Aircraft drone, final int index) {
        this.drone = drone;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
