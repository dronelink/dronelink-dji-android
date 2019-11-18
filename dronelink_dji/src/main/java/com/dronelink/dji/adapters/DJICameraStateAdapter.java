//  DJICameraStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.mission.core.enums.CameraMode;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;

public class DJICameraStateAdapter implements CameraStateAdapter {
    private final SystemState state;

    public DJICameraStateAdapter(final SystemState state) {
        this.state = state;
    }

    @Override
    public boolean isCapturingPhotoInterval() {
        return state != null && state.isShootingIntervalPhoto();
    }

    @Override
    public boolean isCapturingVideo() {
        return state != null && state.isRecording();
    }

    @Override
    public boolean isCapturing() {
        return state != null && (state.isRecording()
                || state.isShootingSinglePhoto()
                || state.isShootingSinglePhotoInRAWFormat()
                || state.isShootingIntervalPhoto()
                || state.isShootingBurstPhoto()
                || state.isShootingRAWBurstPhoto()
                || state.isShootingShallowFocusPhoto()
                || state.isShootingPanoramaPhoto());
    }

    @Override
    public CameraMode getMissionMode() {
        switch (state == null ? SettingsDefinitions.CameraMode.UNKNOWN : state.getMode()) {
            case SHOOT_PHOTO: return CameraMode.PHOTO;
            case RECORD_VIDEO: return CameraMode.VIDEO;
            case PLAYBACK: return CameraMode.PLAYBACK;
            case MEDIA_DOWNLOAD: return CameraMode.DOWNLOAD;
            case BROADCAST: return CameraMode.BROADCAST;
            case UNKNOWN: return CameraMode.UNKNOWN;
        }
        return CameraMode.UNKNOWN;
    }
}
