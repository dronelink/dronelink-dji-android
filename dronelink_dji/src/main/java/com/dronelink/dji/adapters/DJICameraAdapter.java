//  DJICameraAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.Dronelink;
import com.dronelink.core.Kernel;
import com.dronelink.core.adapters.CameraAdapter;
import com.dronelink.core.adapters.EnumElement;
import com.dronelink.core.command.Command;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.kernel.core.enums.CameraPhotoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraPhotoMode;
import com.dronelink.core.kernel.core.enums.CameraStorageLocation;
import com.dronelink.core.kernel.core.enums.CameraVideoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset;
import com.dronelink.dji.DronelinkDJI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;

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
    public void format(final CameraStorageLocation storageLocation, final Command.Finisher finisher) {
        camera.formatStorage(DronelinkDJI.getCameraStorageLocation(storageLocation), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void setHistogramEnabled(final boolean enabled, final Command.Finisher finisher) {
        camera.setHistogramEnabled(enabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public List<EnumElement> getEnumElements(final String parameter) {
        switch (parameter) {
            case "CameraPhotoInterval":
                //TODO missing? camera.getCapabilities().photoIntervalRange()
                final List<EnumElement> enumElements = new ArrayList<>();
                for (final int interval : new int[]{ 2, 3, 4, 5, 6, 7, 8, 9, 10 }) {
                    enumElements.add(new EnumElement(interval + " s", interval));
                }
                return enumElements;
            default:
                break;
        }

        final Map<String, String> enumDefinition = Dronelink.getInstance().getEnumDefinition(parameter);
        if (enumDefinition == null) {
            return null;
        }

        final List<String> range = new ArrayList<>();
        switch (parameter) {
            case "CameraAperture":
                for (final SettingsDefinitions.Aperture value : camera.getCapabilities().apertureRange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraAperture(value)));
                }
                break;
            case "CameraExposureCompensation":
                for (final SettingsDefinitions.ExposureCompensation value : camera.getCapabilities().exposureCompensationRange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraExposureCompensation(value)));
                }
                break;
            case "CameraExposureMode":
                for (final SettingsDefinitions.ExposureMode value : camera.getCapabilities().exposureModeRange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraExposureMode(value)));
                }
                break;
            case "CameraISO":
                for (final SettingsDefinitions.ISO value : camera.getCapabilities().ISORange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraISO(value)));
                }
                break;
            case "CameraPhotoMode":
                final SettingsDefinitions.FlatCameraMode[] modes = camera.getCapabilities().FlatCameraModeRange();
                if (modes != null) {
                    for (final SettingsDefinitions.FlatCameraMode value : modes) {
                        range.add(Kernel.enumRawValue(DronelinkDJI.getCameraPhotoMode(value)));
                    }
                }
                else {
                    range.add(Kernel.enumRawValue(CameraPhotoMode.SINGLE));
                    range.add(Kernel.enumRawValue(CameraPhotoMode.INTERVAL));
                    range.add(Kernel.enumRawValue(CameraPhotoMode.BURST));
                    range.add(Kernel.enumRawValue(CameraPhotoMode.AEB));
                    range.add(Kernel.enumRawValue(CameraPhotoMode.HYPER_LIGHT));
                }
                break;
            case "CameraPhotoFileFormat":
                //TODO doesn't exist? camera.getCapabilities().photoFileFormatRange()
                range.add(Kernel.enumRawValue(CameraPhotoFileFormat.JPEG));
                range.add(Kernel.enumRawValue(CameraPhotoFileFormat.RAW_JPEG));
                break;
            case "CameraMode":
                for (final SettingsDefinitions.CameraMode value : camera.getCapabilities().modeRange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraMode(value)));
                }
                break;
            case "CameraShutterSpeed":
                for (final SettingsDefinitions.ShutterSpeed value : camera.getCapabilities().shutterSpeedRange()) {
                    range.add(Kernel.enumRawValue(DronelinkDJI.getCameraShutterSpeed(value)));
                }
                break;
            case "CameraStorageLocation":
                range.add(Kernel.enumRawValue(CameraStorageLocation.SD_CARD));
                if (camera.isInternalStorageSupported()) {
                    range.add(Kernel.enumRawValue(CameraStorageLocation.INTERNAL));
                }
                break;
            case "CameraVideoFileFormat":
                //TODO doesn't exist? camera.getCapabilities().videoFileFormatRange()
                range.add(Kernel.enumRawValue(CameraVideoFileFormat.MP4));
                range.add(Kernel.enumRawValue(CameraVideoFileFormat.MOV));
                break;
            case "CameraWhiteBalancePreset":
                //TODO doesn't exist? camera.getCapabilities().whiteBalancePresetRange()
                range.add(Kernel.enumRawValue(CameraWhiteBalancePreset.AUTO));
                range.add(Kernel.enumRawValue(CameraWhiteBalancePreset.SUNNY));
                range.add(Kernel.enumRawValue(CameraWhiteBalancePreset.CLOUDY));
                range.add(Kernel.enumRawValue(CameraWhiteBalancePreset.INDOOR_FLUORESCENT));
                range.add(Kernel.enumRawValue(CameraWhiteBalancePreset.INDOOR_INCANDESCENT));
                break;
            default:
                return null;
        }

        final List<EnumElement> enumElements = new ArrayList<>();
        for (final String rangeValue : range) {
            if (rangeValue != null && !rangeValue.equals("unknown")) {
                final String enumDisplay = enumDefinition.get(rangeValue);
                if (enumDisplay != null) {
                    enumElements.add(new EnumElement(enumDisplay, rangeValue));
                }
            }
        }

        return enumElements.isEmpty() ? null : enumElements;
    }
}
