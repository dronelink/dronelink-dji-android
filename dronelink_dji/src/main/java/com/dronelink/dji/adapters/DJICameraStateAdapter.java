//  DJICameraStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.mission.core.enums.CameraExposureCompensation;
import com.dronelink.core.mission.core.enums.CameraMode;

import dji.common.camera.ExposureSettings;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.camera.SystemState;

public class DJICameraStateAdapter implements CameraStateAdapter {
    public final SystemState state;
    public final StorageState storageState;
    public final ExposureSettings exposureSettings;

    public DJICameraStateAdapter(final SystemState state, final StorageState storageState, final ExposureSettings exposureSettings) {
        this.state = state;
        this.storageState = storageState;
        this.exposureSettings = exposureSettings;
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
    public boolean isSDCardInserted() {
        if (storageState != null) {
            return storageState.isInserted();
        }
        return true;
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

    @Override
    public CameraExposureCompensation getMissionExposureCompensation() {
        switch (exposureSettings == null ? SettingsDefinitions.ExposureCompensation.UNKNOWN : exposureSettings.getExposureCompensation()) {
            case N_5_0: return CameraExposureCompensation.N_5_0;
            case N_4_7: return CameraExposureCompensation.N_4_7;
            case N_4_3: return CameraExposureCompensation.N_4_3;
            case N_4_0: return CameraExposureCompensation.N_4_0;
            case N_3_7: return CameraExposureCompensation.N_3_7;
            case N_3_3: return CameraExposureCompensation.N_3_3;
            case N_3_0: return CameraExposureCompensation.N_3_0;
            case N_2_7: return CameraExposureCompensation.N_2_7;
            case N_2_3: return CameraExposureCompensation.N_2_3;
            case N_2_0: return CameraExposureCompensation.N_2_0;
            case N_1_7: return CameraExposureCompensation.N_1_7;
            case N_1_3: return CameraExposureCompensation.N_1_3;
            case N_1_0: return CameraExposureCompensation.N_1_0;
            case N_0_7: return CameraExposureCompensation.N_0_7;
            case N_0_3: return CameraExposureCompensation.N_0_3;
            case N_0_0: return CameraExposureCompensation.N_0_0;
            case P_0_3: return CameraExposureCompensation.P_0_3;
            case P_0_7: return CameraExposureCompensation.P_0_7;
            case P_1_0: return CameraExposureCompensation.P_1_0;
            case P_1_3: return CameraExposureCompensation.P_1_3;
            case P_1_7: return CameraExposureCompensation.P_1_7;
            case P_2_0: return CameraExposureCompensation.P_2_0;
            case P_2_3: return CameraExposureCompensation.P_2_3;
            case P_2_7: return CameraExposureCompensation.P_2_7;
            case P_3_0: return CameraExposureCompensation.P_3_0;
            case P_3_3: return CameraExposureCompensation.P_3_3;
            case P_3_7: return CameraExposureCompensation.P_3_7;
            case P_4_0: return CameraExposureCompensation.P_4_0;
            case P_4_3: return CameraExposureCompensation.P_4_3;
            case P_4_7: return CameraExposureCompensation.P_4_7;
            case P_5_0: return CameraExposureCompensation.P_5_0;
            case FIXED:
            case UNKNOWN: return CameraExposureCompensation.UNKNOWN;
        }
        return CameraExposureCompensation.UNKNOWN;
    }
}
