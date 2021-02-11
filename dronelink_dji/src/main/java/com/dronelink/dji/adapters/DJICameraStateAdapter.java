//  DJICameraStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.kernel.core.enums.CameraAEBCount;
import com.dronelink.core.kernel.core.enums.CameraAperture;
import com.dronelink.core.kernel.core.enums.CameraExposureCompensation;
import com.dronelink.core.kernel.core.enums.CameraISO;
import com.dronelink.core.kernel.core.enums.CameraMode;
import com.dronelink.core.kernel.core.enums.CameraPhotoMode;
import com.dronelink.core.kernel.core.enums.CameraShutterSpeed;
import com.dronelink.core.kernel.core.enums.CameraStorageLocation;
import com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset;
import com.dronelink.dji.DronelinkDJI;

import dji.common.camera.ExposureSettings;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.camera.SystemState;
import dji.common.camera.WhiteBalance;

public class DJICameraStateAdapter implements CameraStateAdapter {
    public final SystemState state;
    public final StorageState storageState;
    public final ExposureSettings exposureSettings;
    public final WhiteBalance whiteBalance;
    public final String lensInformation;

    public DJICameraStateAdapter(final SystemState state, final StorageState storageState, final ExposureSettings exposureSettings, final String lensInformation, final WhiteBalance whiteBalance) {
        this.state = state;
        this.storageState = storageState;
        this.exposureSettings = exposureSettings;
        this.lensInformation = lensInformation;
        this.whiteBalance = whiteBalance;
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
    public boolean isCapturingContinuous() {
        return state != null && (isCapturingPhotoInterval() || isCapturingVideo());
    }

    //todo: add isShootingHyperanalytic in new version
    @Override
    public boolean isBusy() {
        return state != null && (state.isStoringPhoto()
                || state.isShootingSinglePhoto()
                || state.isShootingSinglePhotoInRAWFormat()
                || state.isShootingIntervalPhoto()
                || state.isShootingBurstPhoto()
                || state.isShootingRAWBurstPhoto()
                || state.isShootingShallowFocusPhoto()
                || state.isShootingPanoramaPhoto());
                //|| state.isShootingHyperanalytic();
    }

    @Override
    public boolean isSDCardInserted() {
        if (storageState != null) {
            return storageState.isInserted();
        }
        return true;
    }

    @Override
    public CameraStorageLocation getStorageLocation() {
        if (storageState == null) {
            return CameraStorageLocation.UNKNOWN;
        }
        return DronelinkDJI.getStorageLocation(storageState.getStorageLocation());
    }

    @Override
    public CameraMode getMode() {
        if (state == null) {
            return CameraMode.UNKNOWN;
        }

        return DronelinkDJI.getCameraMode(state.getMode());
    }

    @Override
    public CameraPhotoMode getPhotoMode() {
        //FIXME
        return null;
    }

    @Override
    public int getPhotoInterval() {
        //FIXME
        return 0;
    }

    @Override
    public CameraExposureCompensation getExposureCompensation() {
        if (exposureSettings == null) {
            return CameraExposureCompensation.UNKNOWN;
        }

        return DronelinkDJI.getCameraExposureCompensation(exposureSettings.getExposureCompensation());
    }

    @Override
    public CameraISO getISO() {
        if (exposureSettings == null) {
            return CameraISO.UNKNOWN;
        }

        return DronelinkDJI.getCameraISO(exposureSettings.getISO());
    }

    @Override
    public CameraShutterSpeed getShutterSpeed() {
        if (exposureSettings == null) {
            return CameraShutterSpeed.UNKNOWN;
        }

        return DronelinkDJI.getCameraShutterSpeed(exposureSettings.getShutterSpeed());
    }

    @Override
    public CameraAperture getAperture() {
        if (exposureSettings == null) {
            return CameraAperture.UNKNOWN;
        }

        return DronelinkDJI.getCameraAperture(exposureSettings.getAperture());
    }

    @Override
    public CameraWhiteBalancePreset getWhiteBalancePreset() {
        return DronelinkDJI.getCameraWhiteBalancePreset(whiteBalance.getWhiteBalancePreset());
    }

    @Override
    public String getLensDetails() {
        return lensInformation;
    }
}
