//  DJICameraAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraAdapter;
import com.dronelink.core.kernel.core.enums.CameraVideoStreamSource;
import com.dronelink.dji.DronelinkDJI;

import dji.common.camera.SettingsDefinitions;
import dji.sdk.camera.Camera;
import dji.sdk.camera.Lens;

public class DJICameraAdapter implements CameraAdapter {
    public final Camera camera;

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

    @Override
    public int getLensIndex(final CameraVideoStreamSource videoStreamSource) {
        final SettingsDefinitions.LensType lensType = DronelinkDJI.getCameraVideoStreamSourceLensType(videoStreamSource);
        if (camera.getLenses() != null) {
            for (final Lens lens : camera.getLenses()) {
                if (lens.getType() == lensType) {
                    return lens.getIndex();
                }
            }
        }

        return 0;
    }
}
