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
import com.dronelink.core.kernel.core.enums.CameraBurstCount;
import com.dronelink.core.kernel.core.enums.CameraExposureCompensation;
import com.dronelink.core.kernel.core.enums.CameraExposureMode;
import com.dronelink.core.kernel.core.enums.CameraISO;
import com.dronelink.core.kernel.core.enums.CameraMode;
import com.dronelink.core.kernel.core.enums.CameraPhotoAspectRatio;
import com.dronelink.core.kernel.core.enums.CameraPhotoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraPhotoMode;
import com.dronelink.core.kernel.core.enums.CameraShutterSpeed;
import com.dronelink.core.kernel.core.enums.CameraStorageLocation;
import com.dronelink.core.kernel.core.enums.CameraVideoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraVideoFrameRate;
import com.dronelink.core.kernel.core.enums.CameraVideoResolution;
import com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset;
import com.dronelink.dji.DronelinkDJI;

import dji.common.camera.CameraVideoStreamSource;
import dji.common.camera.ExposureSettings;
import dji.common.camera.FocusState;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.camera.SystemState;
import dji.common.camera.WhiteBalance;

public
class DJICameraStateAdapter implements CameraStateAdapter {
    public final SystemState state;
    public final CameraVideoStreamSource videoStreamSource;
    public final FocusState focusState;
    public final StorageState storageState;
    public final SettingsDefinitions.ExposureMode exposureMode;
    public final ExposureSettings exposureSettings;
    public final int lensIndex;
    public final String lensInformation;
    public final SettingsDefinitions.StorageLocation storageLocation;
    public final SettingsDefinitions.ShootPhotoMode photoMode;
    public final SettingsDefinitions.PhotoTimeIntervalSettings photoTimeIntervalSettings;
    public final SettingsDefinitions.PhotoFileFormat photoFileFormat;
    public final SettingsDefinitions.PhotoAspectRatio photoAspectRatio;
    public final SettingsDefinitions.PhotoBurstCount burstCount;
    public final SettingsDefinitions.PhotoAEBCount aebCount;
    public final SettingsDefinitions.VideoFileFormat videoFileFormat;
    public final SettingsDefinitions.VideoFrameRate videoFrameRate;
    public final SettingsDefinitions.VideoResolution videoResolution;
    public final WhiteBalance whiteBalance;
    public final SettingsDefinitions.ISO iso;
    public final Double focusRingValue;
    public final Double focusRingMax;

    public DJICameraStateAdapter(
            final SystemState state,
            final CameraVideoStreamSource videoStreamSource,
            final FocusState focusState,
            final StorageState storageState,
            final SettingsDefinitions.ExposureMode exposureMode,
            final ExposureSettings exposureSettings,
            final int lensIndex,
            final String lensInformation,
            final SettingsDefinitions.StorageLocation storageLocation,
            final SettingsDefinitions.ShootPhotoMode photoMode,
            final SettingsDefinitions.PhotoTimeIntervalSettings photoTimeIntervalSettings,
            final SettingsDefinitions.PhotoFileFormat photoFileFormat,
            final SettingsDefinitions.PhotoAspectRatio photoAspectRatio,
            final SettingsDefinitions.PhotoBurstCount burstCount,
            final SettingsDefinitions.PhotoAEBCount aebCount,
            final SettingsDefinitions.VideoFileFormat videoFileFormat,
            final SettingsDefinitions.VideoFrameRate videoFrameRate,
            final SettingsDefinitions.VideoResolution videoResolution,
            final WhiteBalance whiteBalance,
            final SettingsDefinitions.ISO iso,
            final Double focusRingValue,
            final Double focusRingMax) {
        this.state = state;
        this.videoStreamSource = videoStreamSource;
        this.focusState = focusState;
        this.storageState = storageState;
        this.exposureMode = exposureMode;
        this.exposureSettings = exposureSettings;
        this.lensIndex = lensIndex;
        this.lensInformation = lensInformation;
        this.storageLocation = storageLocation;
        this.photoMode = photoMode;
        this.photoTimeIntervalSettings = photoTimeIntervalSettings;
        this.photoFileFormat = photoFileFormat;
        this.photoAspectRatio = photoAspectRatio;
        this.burstCount = burstCount;
        this.aebCount = aebCount;
        this.videoFileFormat = videoFileFormat;
        this.videoFrameRate = videoFrameRate;
        this.videoResolution = videoResolution;
        this.whiteBalance = whiteBalance;
        this.iso = iso;
        this.focusRingValue = focusRingValue;
        this.focusRingMax = focusRingMax;
    }

    @Override
    public boolean isBusy() {
        if (DronelinkDJI.isBusy(state)) {
            return true;
        }

        if (focusState != null) {
            return focusState.getFocusStatus() == SettingsDefinitions.FocusStatus.FOCUSING;
        }

        if (storageState != null) {
            return storageState.isFormatting() || storageState.isInitializing();
        }

        return false;
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
    public boolean isCapturingPhotoInterval() {
        return state != null && state.isShootingIntervalPhoto();
    }

    @Override
    public boolean isCapturingVideo() {
        return state != null && state.isRecording();
    }

    @Override
    public boolean isCapturingContinuous() {
        return isCapturingVideo() || isCapturingPhotoInterval();
    }

    @Override
    public boolean isSDCardInserted() {
        if (storageState != null) {
            return storageState.isInserted();
        }
        return true;
    }

    @Override
    public com.dronelink.core.kernel.core.enums.CameraVideoStreamSource getVideoStreamSource() {
        return DronelinkDJI.getCameraVideoStreamSource(videoStreamSource == null ? CameraVideoStreamSource.UNKNOWN : videoStreamSource);
    }

    @Override
    public CameraStorageLocation getStorageLocation() {
        return DronelinkDJI.getCameraStorageLocation(storageLocation == null ? SettingsDefinitions.StorageLocation.UNKNOWN : storageLocation);
    }

    @Override
    public CameraMode getMode() {
        return DronelinkDJI.getCameraMode(state == null ? SettingsDefinitions.CameraMode.UNKNOWN : state.getMode());
    }

    @Override
    public CameraPhotoMode getPhotoMode() {
        return DronelinkDJI.getCameraPhotoMode(photoMode == null ? SettingsDefinitions.ShootPhotoMode.UNKNOWN : photoMode);
    }

    @Override
    public CameraPhotoFileFormat getPhotoFileFormat() {
        return DronelinkDJI.getCameraPhotoFileFormat(photoFileFormat == null ? SettingsDefinitions.PhotoFileFormat.UNKNOWN : photoFileFormat);
    }

    @Override
    public Integer getPhotoInterval() {
        return photoTimeIntervalSettings == null ? null : photoTimeIntervalSettings.getTimeIntervalInSeconds();
    }

    @Override
    public CameraBurstCount getBurstCount() {
        return DronelinkDJI.getCameraBurstCount(burstCount == null ? SettingsDefinitions.PhotoBurstCount.UNKNOWN : burstCount);
    }

    @Override
    public CameraAEBCount getAEBCount() {
        return DronelinkDJI.getCameraAEBCount(aebCount == null ? SettingsDefinitions.PhotoAEBCount.UNKNOWN : aebCount);
    }

    @Override
    public CameraVideoFileFormat getVideoFileFormat() {
        return DronelinkDJI.getCameraVideoFileFormat(videoFileFormat == null ? SettingsDefinitions.VideoFileFormat.UNKNOWN : videoFileFormat);
    }

    @Override
    public CameraVideoFrameRate getVideoFrameRate() {
        return DronelinkDJI.getCameraVideoFrameRate(videoFrameRate == null ? SettingsDefinitions.VideoFrameRate.UNKNOWN : videoFrameRate);
    }

    @Override
    public CameraVideoResolution getVideoResolution() {
        return DronelinkDJI.getCameraVideoResolution(videoResolution == null ? SettingsDefinitions.VideoResolution.UNKNOWN : videoResolution);
    }

    @Override
    public Double getCurrentVideoTime() {
        return state == null ? null : (double)state.getCurrentVideoRecordingTimeInSeconds();
    }

    @Override
    public CameraExposureMode getExposureMode() {
        return DronelinkDJI.getCameraExposureMode(exposureMode == null ? SettingsDefinitions.ExposureMode.UNKNOWN : exposureMode);
    }

    @Override
    public CameraExposureCompensation getExposureCompensation() {
        return DronelinkDJI.getCameraExposureCompensation(exposureSettings == null ? SettingsDefinitions.ExposureCompensation.UNKNOWN : exposureSettings.getExposureCompensation());
    }

    @Override
    public CameraISO getISO() {
        return DronelinkDJI.getCameraISO(iso == null ? SettingsDefinitions.ISO.UNKNOWN : iso);
    }

    @Override
    public Integer getISOSensitivity() {
        return exposureSettings == null ? null : exposureSettings.getISO();
    }

    @Override
    public CameraShutterSpeed getShutterSpeed() {
        return DronelinkDJI.getCameraShutterSpeed(exposureSettings == null ? SettingsDefinitions.ShutterSpeed.UNKNOWN : exposureSettings.getShutterSpeed());
    }

    @Override
    public CameraAperture getAperture() {
        return DronelinkDJI.getCameraAperture(exposureSettings == null ? SettingsDefinitions.Aperture.UNKNOWN : exposureSettings.getAperture());
    }

    @Override
    public CameraWhiteBalancePreset getWhiteBalancePreset() {
        return DronelinkDJI.getCameraWhiteBalancePreset(whiteBalance == null ? SettingsDefinitions.WhiteBalancePreset.UNKNOWN : whiteBalance.getWhiteBalancePreset());
    }

    @Override
    public Integer getWhiteBalanceColorTemperature() {
        return whiteBalance == null ? null : whiteBalance.getColorTemperature();
    }

    @Override
    public int getLensIndex() {
        return lensIndex;
    }

    @Override
    public String getLensDetails() {
        return lensInformation;
    }

    @Override
    public Double getFocusRingValue() {
        return focusRingValue;
    }

    @Override
    public Double getFocusRingMax() {
        return focusRingMax;
    }

    @Override
    public CameraPhotoAspectRatio getAspectRatio() {
        return getMode() == CameraMode.PHOTO ? DronelinkDJI.getCameraPhotoAspectRatio(photoAspectRatio == null ? SettingsDefinitions.PhotoAspectRatio.UNKNOWN : photoAspectRatio) : CameraPhotoAspectRatio._16_9;
    }
}