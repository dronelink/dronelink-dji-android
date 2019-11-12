//  DronelinkDJI.java
//  DronelinkCore
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;


import com.dronelink.core.mission.core.enums.CameraAEBCount;
import com.dronelink.core.mission.core.enums.CameraAperture;
import com.dronelink.core.mission.core.enums.CameraColor;
import com.dronelink.core.mission.core.enums.CameraExposureCompensation;
import com.dronelink.core.mission.core.enums.CameraExposureMode;
import com.dronelink.core.mission.core.enums.CameraFileIndexMode;
import com.dronelink.core.mission.core.enums.CameraFocusMode;
import com.dronelink.core.mission.core.enums.CameraISO;
import com.dronelink.core.mission.core.enums.CameraMode;
import com.dronelink.core.mission.core.enums.CameraPhotoAspectRatio;
import com.dronelink.core.mission.core.enums.CameraPhotoFileFormat;
import com.dronelink.core.mission.core.enums.CameraPhotoMode;
import com.dronelink.core.mission.core.enums.CameraShutterSpeed;
import com.dronelink.core.mission.core.enums.CameraStorageLocation;
import com.dronelink.core.mission.core.enums.CameraVideoFieldOfView;
import com.dronelink.core.mission.core.enums.CameraVideoFileCompressionStandard;
import com.dronelink.core.mission.core.enums.CameraVideoFileFormat;
import com.dronelink.core.mission.core.enums.CameraVideoFrameRate;
import com.dronelink.core.mission.core.enums.CameraVideoResolution;
import com.dronelink.core.mission.core.enums.CameraVideoStandard;
import com.dronelink.core.mission.core.enums.CameraWhiteBalancePreset;
import com.dronelink.core.mission.core.enums.DroneConnectionFailSafeBehavior;
import com.dronelink.core.mission.core.enums.DroneLightbridgeChannelSelectionMode;
import com.dronelink.core.mission.core.enums.DroneLightbridgeFrequencyBand;
import com.dronelink.core.mission.core.enums.DroneOcuSyncChannelSelectionMode;
import com.dronelink.core.mission.core.enums.DroneOcuSyncFrequencyBand;

import dji.common.airlink.ChannelSelectionMode;
import dji.common.airlink.LightbridgeFrequencyBand;
import dji.common.airlink.OcuSyncFrequencyBand;
import dji.common.camera.SettingsDefinitions;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.gimbal.GimbalMode;

public class DronelinkDJI {
    public static final double GimbalRotationMinTime = 0.1;
    public static final double DroneMaxVelocity = 15.0;

    public static ConnectionFailSafeBehavior getDroneConnectionFailSafeBehavior(final DroneConnectionFailSafeBehavior value) {
        switch (value) {
            case HOVER: return ConnectionFailSafeBehavior.HOVER;
            case RETURN_HOME: return ConnectionFailSafeBehavior.GO_HOME;
            case AUTO_LAND: return ConnectionFailSafeBehavior.LANDING;
            case UNKNOWN: return ConnectionFailSafeBehavior.UNKNOWN;
        }
        return ConnectionFailSafeBehavior.UNKNOWN;
    }

    public static ChannelSelectionMode getLightbridgeChannelSelectionMode(final DroneLightbridgeChannelSelectionMode value) {
        switch (value) {
            case AUTO: return ChannelSelectionMode.AUTO;
            case MANUAL: return ChannelSelectionMode.MANUAL;
            case UNKNOWN: return ChannelSelectionMode.UNKNOWN;
        }
        return ChannelSelectionMode.UNKNOWN;
    }

    public static LightbridgeFrequencyBand getLightbridgeFrequencyBand(final DroneLightbridgeFrequencyBand value) {
        switch (value) {
            case _2_DOT_4_GHZ: return LightbridgeFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
            case _5_DOT_7_GHZ: return LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_7_GHZ;
            case _5_DOT_8_GHZ: return LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
            case UNKNOWN: return LightbridgeFrequencyBand.UNKNOWN;
        }
        return LightbridgeFrequencyBand.UNKNOWN;
    }

    public static ChannelSelectionMode getOcuSyncChannelSelectionMode(final DroneOcuSyncChannelSelectionMode value) {
        switch (value) {
            case AUTO: return ChannelSelectionMode.AUTO;
            case MANUAL: return ChannelSelectionMode.MANUAL;
            case UNKNOWN: return ChannelSelectionMode.UNKNOWN;
        }
        return ChannelSelectionMode.UNKNOWN;
    }

    public static OcuSyncFrequencyBand getOcuSyncFrequencyBand(final DroneOcuSyncFrequencyBand value) {
        switch (value) {
            case _2_DOT_4_GHZ: return OcuSyncFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
            case _5_DOT_8_GHZ: return OcuSyncFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
            case DUAL: return OcuSyncFrequencyBand.FREQUENCY_BAND_DUAL;
            case UNKNOWN: return OcuSyncFrequencyBand.UNKNOWN;
        }
        return OcuSyncFrequencyBand.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoAEBCount getCameraAEBCount(final CameraAEBCount value) {
        switch (value) {
            case _3: return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_3;
            case _5: return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_5;
            case _7: return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_7;
            case UNKNOWN: return SettingsDefinitions.PhotoAEBCount.UNKNOWN;
        }
        return SettingsDefinitions.PhotoAEBCount.UNKNOWN;
    }

    public static SettingsDefinitions.Aperture getCameraAperture(final CameraAperture value) {
        switch (value) {
            case F_1_DOT_6: return SettingsDefinitions.Aperture.F_1_DOT_6;
            case F_1_DOT_7: return SettingsDefinitions.Aperture.F_1_DOT_7;
            case F_1_DOT_8: return SettingsDefinitions.Aperture.F_1_DOT_8;
            case F_2: return SettingsDefinitions.Aperture.F_2;
            case F_2_DOT_2: return SettingsDefinitions.Aperture.F_2_DOT_2;
            case F_2_DOT_4: return SettingsDefinitions.Aperture.F_2_DOT_4;
            case F_2_DOT_5: return SettingsDefinitions.Aperture.F_2_DOT_5;
            case F_2_DOT_6: return SettingsDefinitions.Aperture.F_2_DOT_6;
            case F_2_DOT_8: return SettingsDefinitions.Aperture.F_2_DOT_8;
            case F_3_DOT_2: return SettingsDefinitions.Aperture.F_3_DOT_2;
            case F_3_DOT_4: return SettingsDefinitions.Aperture.F_3_DOT_4;
            case F_3_DOT_5: return SettingsDefinitions.Aperture.F_3_DOT_5;
            case F_4: return SettingsDefinitions.Aperture.F_4;
            case F_4_DOT_5: return SettingsDefinitions.Aperture.F_4_DOT_5;
            case F_4_DOT_8: return SettingsDefinitions.Aperture.F_4_DOT_8;
            case F_5: return SettingsDefinitions.Aperture.F_5;
            case F_5_DOT_6: return SettingsDefinitions.Aperture.F_5_DOT_6;
            case F_6_DOT_3: return SettingsDefinitions.Aperture.F_6_DOT_3;
            case F_6_DOT_8: return SettingsDefinitions.Aperture.F_6_DOT_8;
            case F_7_DOT_1: return SettingsDefinitions.Aperture.F_7_DOT_1;
            case F_8: return SettingsDefinitions.Aperture.F_8;
            case F_9: return SettingsDefinitions.Aperture.F_9;
            case F_9_DOT_5: return SettingsDefinitions.Aperture.F_9_DOT_6; //missing F_9_DOT_5
            case F_9_DOT_6: return SettingsDefinitions.Aperture.F_9_DOT_6;
            case F_10: return SettingsDefinitions.Aperture.F_10;
            case F_11: return SettingsDefinitions.Aperture.F_11;
            case F_13: return SettingsDefinitions.Aperture.F_13;
            case F_14: return SettingsDefinitions.Aperture.F_14;
            case F_16: return SettingsDefinitions.Aperture.F_16;
            case F_18: return SettingsDefinitions.Aperture.F_18;
            case F_19: return SettingsDefinitions.Aperture.F_19;
            case F_20: return SettingsDefinitions.Aperture.F_20;
            case F_22: return SettingsDefinitions.Aperture.F_22;
            case UNKNOWN: return SettingsDefinitions.Aperture.UNKNOWN;
        }
        return SettingsDefinitions.Aperture.UNKNOWN;
    }

    public static SettingsDefinitions.CameraColor getCameraColor(final CameraColor value) {
        switch (value) {
            case NONE: return SettingsDefinitions.CameraColor.NONE;
            case ART: return SettingsDefinitions.CameraColor.ART;
            case BLACK_AND_WHITE: return SettingsDefinitions.CameraColor.BLACK_AND_WHITE;
            case BRIGHT: return SettingsDefinitions.CameraColor.BRIGHT;
            case D_CINELIKE: return SettingsDefinitions.CameraColor.D_CINELIKE;
            case PORTRAIT: return SettingsDefinitions.CameraColor.PORTRAIT;
            case M_31: return SettingsDefinitions.CameraColor.M_31;
            case K_DX: return SettingsDefinitions.CameraColor.K_DX;
            case PRISMO: return SettingsDefinitions.CameraColor.PRISMO;
            case JUGO: return SettingsDefinitions.CameraColor.JUGO;
            case D_LOG: return SettingsDefinitions.CameraColor.D_LOG;
            case TRUE_COLOR: return SettingsDefinitions.CameraColor.TRUE_COLOR;
            case INVERSE: return SettingsDefinitions.CameraColor.INVERSE;
            case REMINISCENCE: return SettingsDefinitions.CameraColor.REMINISCENCE;
            case SOLARIZE: return SettingsDefinitions.CameraColor.SOLARIZE;
            case POSTERIZE: return SettingsDefinitions.CameraColor.POSTERIZE;
            case WHITEBOARD: return SettingsDefinitions.CameraColor.WHITEBOARD;
            case BLACKBOARD: return SettingsDefinitions.CameraColor.BLACKBOARD;
            case AQUA: return SettingsDefinitions.CameraColor.AQUA;
            case DELTA: return SettingsDefinitions.CameraColor.DELTA;
            case DK79: return SettingsDefinitions.CameraColor.DK79;
            case VISION_4: return SettingsDefinitions.CameraColor.VISION_4;
            case VISION_6: return SettingsDefinitions.CameraColor.VISION_6;
            case TRUE_COLOR_EXT: return SettingsDefinitions.CameraColor.TRUE_COLOR_EXT;
            case FILM_A: return SettingsDefinitions.CameraColor.FILM_A;
            case FILM_B: return SettingsDefinitions.CameraColor.FILM_B;
            case FILM_C: return SettingsDefinitions.CameraColor.FILM_C;
            case FILM_D: return SettingsDefinitions.CameraColor.FILM_D;
            case FILM_E: return SettingsDefinitions.CameraColor.FILM_E;
            case FILM_F: return SettingsDefinitions.CameraColor.FILM_F;
            case FILM_G: return SettingsDefinitions.CameraColor.FILM_G;
            case FILM_H: return SettingsDefinitions.CameraColor.FILM_H;
            case FILM_I: return SettingsDefinitions.CameraColor.FILM_I;
            case HLG: return SettingsDefinitions.CameraColor.HLG;
            case UNKNOWN: return SettingsDefinitions.CameraColor.UNKNOWN;
        }
        return SettingsDefinitions.CameraColor.UNKNOWN;
    }

    public static SettingsDefinitions.ExposureCompensation getCameraExposureCompensation(final CameraExposureCompensation value) {
        switch (value) {
            case N_5_0: return SettingsDefinitions.ExposureCompensation.N_5_0;
            case N_4_7: return SettingsDefinitions.ExposureCompensation.N_4_7;
            case N_4_3: return SettingsDefinitions.ExposureCompensation.N_4_3;
            case N_4_0: return SettingsDefinitions.ExposureCompensation.N_4_0;
            case N_3_7: return SettingsDefinitions.ExposureCompensation.N_3_7;
            case N_3_3: return SettingsDefinitions.ExposureCompensation.N_3_3;
            case N_3_0: return SettingsDefinitions.ExposureCompensation.N_3_0;
            case N_2_7: return SettingsDefinitions.ExposureCompensation.N_2_7;
            case N_2_3: return SettingsDefinitions.ExposureCompensation.N_2_3;
            case N_2_0: return SettingsDefinitions.ExposureCompensation.N_2_0;
            case N_1_7: return SettingsDefinitions.ExposureCompensation.N_1_7;
            case N_1_3: return SettingsDefinitions.ExposureCompensation.N_1_3;
            case N_1_0: return SettingsDefinitions.ExposureCompensation.N_1_0;
            case N_0_7: return SettingsDefinitions.ExposureCompensation.N_0_7;
            case N_0_3: return SettingsDefinitions.ExposureCompensation.N_0_3;
            case N_0_0: return SettingsDefinitions.ExposureCompensation.N_0_0;
            case P_0_3: return SettingsDefinitions.ExposureCompensation.P_0_3;
            case P_0_7: return SettingsDefinitions.ExposureCompensation.P_0_7;
            case P_1_0: return SettingsDefinitions.ExposureCompensation.P_1_0;
            case P_1_3: return SettingsDefinitions.ExposureCompensation.P_1_3;
            case P_1_7: return SettingsDefinitions.ExposureCompensation.P_1_7;
            case P_2_0: return SettingsDefinitions.ExposureCompensation.P_2_0;
            case P_2_3: return SettingsDefinitions.ExposureCompensation.P_2_3;
            case P_2_7: return SettingsDefinitions.ExposureCompensation.P_2_7;
            case P_3_0: return SettingsDefinitions.ExposureCompensation.P_3_0;
            case P_3_3: return SettingsDefinitions.ExposureCompensation.P_3_3;
            case P_3_7: return SettingsDefinitions.ExposureCompensation.P_3_7;
            case P_4_0: return SettingsDefinitions.ExposureCompensation.P_4_0;
            case P_4_3: return SettingsDefinitions.ExposureCompensation.P_4_3;
            case P_4_7: return SettingsDefinitions.ExposureCompensation.P_4_7;
            case P_5_0: return SettingsDefinitions.ExposureCompensation.P_5_0;
            case UNKNOWN: return SettingsDefinitions.ExposureCompensation.UNKNOWN;
        }
        return SettingsDefinitions.ExposureCompensation.UNKNOWN;
    }

    public static SettingsDefinitions.ExposureMode getCameraExposureMode(final CameraExposureMode value) {
        switch (value) {
            case PROGRAM: return SettingsDefinitions.ExposureMode.PROGRAM;
            case SHUTTER_PRIORITY: return SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY;
            case APERTURE_PRIORITY: return SettingsDefinitions.ExposureMode.APERTURE_PRIORITY;
            case MANUAL: return SettingsDefinitions.ExposureMode.MANUAL;
            case UNKNOWN: return SettingsDefinitions.ExposureMode.UNKNOWN;
        }
        return SettingsDefinitions.ExposureMode.UNKNOWN;
    }

    public static SettingsDefinitions.FileIndexMode getCameraFileIndexMode(final CameraFileIndexMode value) {
        switch (value) {
            case RESET: return SettingsDefinitions.FileIndexMode.RESET;
            case SEQUENCE: return SettingsDefinitions.FileIndexMode.SEQUENCE;
            case UNKNOWN: return SettingsDefinitions.FileIndexMode.UNKNOWN;
        }
        return SettingsDefinitions.FileIndexMode.UNKNOWN;
    }

    public static SettingsDefinitions.FocusMode getCameraFocusMode(final CameraFocusMode value) {
        switch (value) {
            case MANUAL: return SettingsDefinitions.FocusMode.MANUAL;
            case AUTO: return SettingsDefinitions.FocusMode.AUTO;
            case AFC: return SettingsDefinitions.FocusMode.AFC;
            case UNKNOWN: return SettingsDefinitions.FocusMode.UNKNOWN;
        }
        return SettingsDefinitions.FocusMode.UNKNOWN;
    }

    public static SettingsDefinitions.ISO getCameraISO(final CameraISO value) {
        switch (value) {
            case AUTO: return SettingsDefinitions.ISO.AUTO;
            case _100: return SettingsDefinitions.ISO.ISO_100;
            case _200: return SettingsDefinitions.ISO.ISO_200;
            case _400: return SettingsDefinitions.ISO.ISO_400;
            case _800: return SettingsDefinitions.ISO.ISO_800;
            case _1600: return SettingsDefinitions.ISO.ISO_1600;
            case _3200: return SettingsDefinitions.ISO.ISO_3200;
            case _6400: return SettingsDefinitions.ISO.ISO_6400;
            case _12800: return SettingsDefinitions.ISO.ISO_12800;
            case _25600: return SettingsDefinitions.ISO.ISO_25600;
            case FIXED: return SettingsDefinitions.ISO.FIXED;
            case UNKNOWN: return SettingsDefinitions.ISO.UNKNOWN;
        }
        return SettingsDefinitions.ISO.UNKNOWN;
    }

    public static SettingsDefinitions.CameraMode getCameraMode(final CameraMode value) {
        switch (value) {
            case PHOTO: return SettingsDefinitions.CameraMode.SHOOT_PHOTO;
            case VIDEO: return SettingsDefinitions.CameraMode.RECORD_VIDEO;
            case PLAYBACK: return SettingsDefinitions.CameraMode.PLAYBACK;
            case DOWNLOAD: return SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD;
            case BROADCAST: return SettingsDefinitions.CameraMode.BROADCAST;
            case UNKNOWN: return SettingsDefinitions.CameraMode.UNKNOWN;
        }
        return SettingsDefinitions.CameraMode.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoAspectRatio getCameraPhotoAspectRatio(final CameraPhotoAspectRatio value) {
        switch (value) {
            case _4_3: return SettingsDefinitions.PhotoAspectRatio.RATIO_4_3;
            case _16_9: return SettingsDefinitions.PhotoAspectRatio.RATIO_16_9;
            case _3_2: return SettingsDefinitions.PhotoAspectRatio.RATIO_3_2;
            case UNKNOWN: return SettingsDefinitions.PhotoAspectRatio.UNKNOWN;
        }
        return SettingsDefinitions.PhotoAspectRatio.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoFileFormat getCameraPhotoFileFormat(final CameraPhotoFileFormat value) {
        switch (value) {
            case RAW: return SettingsDefinitions.PhotoFileFormat.RAW;
            case JPEG: return SettingsDefinitions.PhotoFileFormat.JPEG;
            case RAW_JPEG: return SettingsDefinitions.PhotoFileFormat.RAW_AND_JPEG;
            case TIFF_14_BIT: return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT;
            case RADIOMETRIC_JPEG: return SettingsDefinitions.PhotoFileFormat.RADIOMETRIC_JPEG;
            case TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION: return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION;
            case TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION: return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION;
            case UNKNOWN: return SettingsDefinitions.PhotoFileFormat.UNKNOWN;
        }
        return SettingsDefinitions.PhotoFileFormat.UNKNOWN;
    }

    public static SettingsDefinitions.ShootPhotoMode getCameraPhotoMode(final CameraPhotoMode value) {
        switch (value) {
            case SINGLE: return SettingsDefinitions.ShootPhotoMode.SINGLE;
            case HDR: return SettingsDefinitions.ShootPhotoMode.HDR;
            case BURST: return SettingsDefinitions.ShootPhotoMode.BURST;
            case AEB: return SettingsDefinitions.ShootPhotoMode.AEB;
            case INTERVAL: return SettingsDefinitions.ShootPhotoMode.INTERVAL;
            case TIME_LAPSE: return SettingsDefinitions.ShootPhotoMode.TIME_LAPSE;
            case RAW_BURST: return SettingsDefinitions.ShootPhotoMode.RAW_BURST;
            case SHALLOW_FOCUS: return SettingsDefinitions.ShootPhotoMode.SHALLOW_FOCUS;
            case PANORAMA: return SettingsDefinitions.ShootPhotoMode.PANORAMA;
            case EHDR: return SettingsDefinitions.ShootPhotoMode.EHDR;
            case HYPER_LIGHT: return SettingsDefinitions.ShootPhotoMode.HYPER_LIGHT;
            case UNKNOWN: return SettingsDefinitions.ShootPhotoMode.UNKNOWN;
        }
        return SettingsDefinitions.ShootPhotoMode.UNKNOWN;
    }

    public static SettingsDefinitions.ShutterSpeed getCameraShutterSpeed(final CameraShutterSpeed value) {
        switch (value) {
            case _1_8000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_8000;
            case _1_6400: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6400;
            case _1_6000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6000;
            case _1_5000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_5000;
            case _1_4000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_4000;
            case _1_3200: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3200;
            case _1_3000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3000;
            case _1_2500: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2500;
            case _1_2000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2000;
            case _1_1600: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1600;
            case _1_1500: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1500;
            case _1_1250: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1250;
            case _1_1000: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1000;
            case _1_800: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800;
            case _1_750: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800; //missing 1/750
            case _1_725: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_725;
            case _1_640: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_640;
            case _1_500: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_500;
            case _1_400: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_400;
            case _1_350: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_350;
            case _1_320: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_320;
            case _1_250: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_250;
            case _1_240: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_240;
            case _1_200: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_200;
            case _1_180: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_180;
            case _1_160: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_160;
            case _1_125: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_125;
            case _1_120: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_120;
            case _1_100: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_100;
            case _1_90: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_90;
            case _1_80: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_80;
            case _1_60: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_60;
            case _1_50: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_50;
            case _1_45: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_50; //missing 1/45
            case _1_40: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_40;
            case _1_30: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_30;
            case _1_25: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_25;
            case _1_20: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_20;
            case _1_15: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_15;
            case _1_12_DOT_5: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_12_DOT_5;
            case _1_10: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_10;
            case _1_8: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_8;
            case _1_6_DOT_25: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6_DOT_25;
            case _1_6: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6_DOT_25; //missing 1/6
            case _1_5: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_5;
            case _1_4: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_4;
            case _1_3: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3;
            case _1_2_DOT_5: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2_DOT_5;
            case _0_DOT_3: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2_DOT_5; //missing 0.3
            case _1_2: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2;
            case _1_1_DOT_67: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1_DOT_67;
            case _0_DOT_7: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1_DOT_67; //missing 0.7
            case _1: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1;
            case _1_DOT_3: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_3;
            case _1_DOT_4: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_3; //missing 1.4
            case _1_DOT_6: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_6;
            case _2: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_2;
            case _2_DOT_5: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_2_DOT_5;
            case _3: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_3;
            case _3_DOT_2: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_3_DOT_2;
            case _4: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_4;
            case _5: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_5;
            case _6: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_6;
            case _7: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_7;
            case _8: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_8;
            case _9: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_9;
            case _10: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_10;
            case _11: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_10; //missing 11
            case _13: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_13;
            case _15: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_15;
            case _16: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_15; //missing 16
            case _20: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_20;
            case _23: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_20; //missing 23
            case _25: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_25;
            case _30: return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_30;
            case UNKNOWN: return SettingsDefinitions.ShutterSpeed.UNKNOWN;
        }
        return SettingsDefinitions.ShutterSpeed.UNKNOWN;
    }

    public static SettingsDefinitions.StorageLocation getCameraStorageLocation(final CameraStorageLocation value) {
        switch (value) {
            case SD_CARD: return SettingsDefinitions.StorageLocation.SDCARD;
            case INTERNAL: return SettingsDefinitions.StorageLocation.INTERNAL_STORAGE;
            case UNKNOWN: return SettingsDefinitions.StorageLocation.UNKNOWN;
        }
        return SettingsDefinitions.StorageLocation.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFileCompressionStandard getCameraVideoFileCompressionStandard(final CameraVideoFileCompressionStandard value) {
        switch (value) {
            case H264: return SettingsDefinitions.VideoFileCompressionStandard.H264;
            case H265: return SettingsDefinitions.VideoFileCompressionStandard.H265;
            case UNKNOWN: return SettingsDefinitions.VideoFileCompressionStandard.Unknown;
        }
        return SettingsDefinitions.VideoFileCompressionStandard.Unknown;
    }

    public static SettingsDefinitions.VideoFileFormat getCameraVideoFileFormat(final CameraVideoFileFormat value) {
        switch (value) {
            case MOV: return SettingsDefinitions.VideoFileFormat.MOV;
            case MP4: return SettingsDefinitions.VideoFileFormat.MP4;
            case TIFF_SEQ: return SettingsDefinitions.VideoFileFormat.TIFF_SEQ;
            case SEQ: return SettingsDefinitions.VideoFileFormat.SEQ;
            case UNKNOWN: return SettingsDefinitions.VideoFileFormat.UNKNOWN;
        }
        return SettingsDefinitions.VideoFileFormat.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFrameRate getCameraVideoVideoFrameRate(final CameraVideoFrameRate value) {
        switch (value) {
            case _23_DOT_976: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_23_DOT_976_FPS;
            case _24: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_24_FPS;
            case _25: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_25_FPS;
            case _29_DOT_970: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_29_DOT_970_FPS;
            case _30: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_30_FPS;
            case _47_DOT_950: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_47_DOT_950_FPS;
            case _48: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_48_FPS;
            case _50: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_50_FPS;
            case _59_DOT_940: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_59_DOT_940_FPS;
            case _60: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_60_FPS;
            case _90: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_90_FPS;
            case _96: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_96_FPS;
            case _100: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_100_FPS;
            case _120: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_120_FPS;
            case _8_DOT_7: return SettingsDefinitions.VideoFrameRate.FRAME_RATE_8_DOT_7_FPS;
            case UNKNOWN: return SettingsDefinitions.VideoFrameRate.UNKNOWN;
        }
        return SettingsDefinitions.VideoFrameRate.UNKNOWN;
    }

    public static SettingsDefinitions.VideoResolution getCameraVideoVideoResolution(final CameraVideoResolution value) {
        switch (value) {
            case _336x256: return SettingsDefinitions.VideoResolution.RESOLUTION_336x256;
            case _640x360: return SettingsDefinitions.VideoResolution.RESOLUTION_640x360;
            case _640x480: return SettingsDefinitions.VideoResolution.RESOLUTION_640x480;
            case _640x512: return SettingsDefinitions.VideoResolution.RESOLUTION_640x512;
            case _1280x720: return SettingsDefinitions.VideoResolution.RESOLUTION_1280x720;
            case _1920x1080: return SettingsDefinitions.VideoResolution.RESOLUTION_1920x1080;
            case _2048x1080: return SettingsDefinitions.VideoResolution.RESOLUTION_2048x1080;
            case _2688x1512:  return SettingsDefinitions.VideoResolution.RESOLUTION_2688x1512;
            case _2704x1520: return SettingsDefinitions.VideoResolution.RESOLUTION_2704x1520;
            case _2720x1530: return SettingsDefinitions.VideoResolution.RESOLUTION_2720x1530;
            case _3712x2088: return SettingsDefinitions.VideoResolution.RESOLUTION_3712x2088;
            case _3840x1572: return SettingsDefinitions.VideoResolution.RESOLUTION_3840x1572;
            case _3840x2160: return SettingsDefinitions.VideoResolution.RESOLUTION_3840x2160;
            case _3944x2088: return SettingsDefinitions.VideoResolution.RESOLUTION_3944x2088;
            case _4096x2160: return SettingsDefinitions.VideoResolution.RESOLUTION_4096x2160;
            case _4608x2160: return SettingsDefinitions.VideoResolution.RESOLUTION_4608x2160;
            case _4608x2592: return SettingsDefinitions.VideoResolution.RESOLUTION_4608x2592;
            case _5280x2160: return SettingsDefinitions.VideoResolution.RESOLUTION_5280x2160;
            case _5280x2972: return SettingsDefinitions.VideoResolution.RESOLUTION_5280x2972;
            case _5760X3240: return SettingsDefinitions.VideoResolution.RESOLUTION_5760X3240;
            case _6016X3200: return SettingsDefinitions.VideoResolution.RESOLUTION_6016X3200;
            case MAX: return SettingsDefinitions.VideoResolution.RESOLUTION_MAX;
            case NO_SSD_VIDEO: return SettingsDefinitions.VideoResolution.NO_SSD_VIDEO;
            case UNKNOWN: return SettingsDefinitions.VideoResolution.UNKNOWN;
        }
        return SettingsDefinitions.VideoResolution.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFov getCameraVideoVideoFieldOfView(final CameraVideoFieldOfView value) {
        switch (value) {
            case DEFAULT: return SettingsDefinitions.VideoFov.DEFAULT;
            case MIDDLE: return SettingsDefinitions.VideoFov.MIDDLE;
            case NARROW: return SettingsDefinitions.VideoFov.NARROW;
            case WIDE: return SettingsDefinitions.VideoFov.WIDE;
            case UNKNOWN: return SettingsDefinitions.VideoFov.UNKNOWN;
        }
        return SettingsDefinitions.VideoFov.UNKNOWN;
    }

    public static SettingsDefinitions.VideoStandard getCameraVideoStandard(final CameraVideoStandard value) {
        switch (value) {
            case PAL: return SettingsDefinitions.VideoStandard.PAL;
            case NTSC: return SettingsDefinitions.VideoStandard.NTSC;
            case UNKNOWN: return SettingsDefinitions.VideoStandard.UNKNOWN;
        }
        return SettingsDefinitions.VideoStandard.UNKNOWN;
    }

    public static SettingsDefinitions.WhiteBalancePreset getCameraWhiteBalancePreset(final CameraWhiteBalancePreset value) {
        switch (value) {
            case AUTO: return SettingsDefinitions.WhiteBalancePreset.AUTO;
            case SUNNY: return SettingsDefinitions.WhiteBalancePreset.SUNNY;
            case CLOUDY: return SettingsDefinitions.WhiteBalancePreset.CLOUDY;
            case WATER_SURFACE: return SettingsDefinitions.WhiteBalancePreset.WATER_SURFACE;
            case INDOOR_INCANDESCENT: return SettingsDefinitions.WhiteBalancePreset.INDOOR_INCANDESCENT;
            case INDOOR_FLUORESCENT: return SettingsDefinitions.WhiteBalancePreset.INDOOR_FLUORESCENT;
            case CUSTOM: return SettingsDefinitions.WhiteBalancePreset.CUSTOM;
            case NEUTRAL: return SettingsDefinitions.WhiteBalancePreset.PRESET_NEUTRAL;
            case UNKNOWN: return SettingsDefinitions.WhiteBalancePreset.UNKNOWN;
        }
        return SettingsDefinitions.WhiteBalancePreset.UNKNOWN;
    }

    public static GimbalMode getGimbalMode(final com.dronelink.core.mission.core.enums.GimbalMode value) {
        switch (value) {
            case FREE: return GimbalMode.FREE;
            case FPV: return GimbalMode.FPV;
            case YAW_FOLLOW: return GimbalMode.YAW_FOLLOW;
            case UNKNOWN: return GimbalMode.UNKNOWN;
        }
        return GimbalMode.UNKNOWN;
    }
}
