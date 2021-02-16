//  DronelinkDJI.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;


import android.content.Context;
import android.location.Location;

import com.dronelink.core.kernel.core.Message;
import com.dronelink.core.kernel.core.enums.CameraAEBCount;
import com.dronelink.core.kernel.core.enums.CameraAperture;
import com.dronelink.core.kernel.core.enums.CameraColor;
import com.dronelink.core.kernel.core.enums.CameraExposureCompensation;
import com.dronelink.core.kernel.core.enums.CameraExposureMode;
import com.dronelink.core.kernel.core.enums.CameraFileIndexMode;
import com.dronelink.core.kernel.core.enums.CameraFocusMode;
import com.dronelink.core.kernel.core.enums.CameraISO;
import com.dronelink.core.kernel.core.enums.CameraMeteringMode;
import com.dronelink.core.kernel.core.enums.CameraMode;
import com.dronelink.core.kernel.core.enums.CameraPhotoAspectRatio;
import com.dronelink.core.kernel.core.enums.CameraPhotoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraPhotoMode;
import com.dronelink.core.kernel.core.enums.CameraShutterSpeed;
import com.dronelink.core.kernel.core.enums.CameraStorageLocation;
import com.dronelink.core.kernel.core.enums.CameraVideoFieldOfView;
import com.dronelink.core.kernel.core.enums.CameraVideoFileCompressionStandard;
import com.dronelink.core.kernel.core.enums.CameraVideoFileFormat;
import com.dronelink.core.kernel.core.enums.CameraVideoFrameRate;
import com.dronelink.core.kernel.core.enums.CameraVideoMode;
import com.dronelink.core.kernel.core.enums.CameraVideoResolution;
import com.dronelink.core.kernel.core.enums.CameraVideoStandard;
import com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset;
import com.dronelink.core.kernel.core.enums.DroneConnectionFailSafeBehavior;
import com.dronelink.core.kernel.core.enums.DroneLightbridgeChannelSelectionMode;
import com.dronelink.core.kernel.core.enums.DroneLightbridgeFrequencyBand;
import com.dronelink.core.kernel.core.enums.DroneOcuSyncChannelSelectionMode;
import com.dronelink.core.kernel.core.enums.DroneOcuSyncFrequencyBand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dji.common.airlink.ChannelSelectionMode;
import dji.common.airlink.LightbridgeFrequencyBand;
import dji.common.airlink.OcuSyncFrequencyBand;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SettingsDefinitions.WhiteBalancePreset;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.flightcontroller.GoHomeExecutionState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.flyzone.FlyZoneState;
import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.GimbalMode;
import dji.common.realname.AppActivationState;
import dji.common.util.DJIParamCapability;
import dji.common.util.DJIParamMinMaxCapability;
import dji.internal.diagnostics.DiagnosticsBaseHandler;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.gimbal.Gimbal;

import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.CLOUDY;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.CUSTOM;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.INDOOR_FLUORESCENT;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.INDOOR_INCANDESCENT;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.NEUTRAL;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.SUNNY;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.UNKNOWN;
import static com.dronelink.core.kernel.core.enums.CameraWhiteBalancePreset.WATER_SURFACE;
import static dji.common.camera.SettingsDefinitions.ISO.FIXED;
import static dji.common.camera.SettingsDefinitions.WhiteBalancePreset.PRESET_NEUTRAL;

public class DronelinkDJI {
    public static final double GimbalRotationMinTime = 0.1;
    public static final double DroneMaxVelocity = 15.0;

    public static ConnectionFailSafeBehavior getDroneConnectionFailSafeBehavior(final DroneConnectionFailSafeBehavior value) {
        switch (value) {
            case HOVER:
                return ConnectionFailSafeBehavior.HOVER;
            case RETURN_HOME:
                return ConnectionFailSafeBehavior.GO_HOME;
            case AUTO_LAND:
                return ConnectionFailSafeBehavior.LANDING;
            case UNKNOWN:
                return ConnectionFailSafeBehavior.UNKNOWN;
        }
        return ConnectionFailSafeBehavior.UNKNOWN;
    }

    public static ChannelSelectionMode getLightbridgeChannelSelectionMode(final DroneLightbridgeChannelSelectionMode value) {
        switch (value) {
            case AUTO:
                return ChannelSelectionMode.AUTO;
            case MANUAL:
                return ChannelSelectionMode.MANUAL;
            case UNKNOWN:
                return ChannelSelectionMode.UNKNOWN;
        }
        return ChannelSelectionMode.UNKNOWN;
    }

    public static LightbridgeFrequencyBand getLightbridgeFrequencyBand(final DroneLightbridgeFrequencyBand value) {
        switch (value) {
            case _2_DOT_4_GHZ:
                return LightbridgeFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
            case _5_DOT_7_GHZ:
                return LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_7_GHZ;
            case _5_DOT_8_GHZ:
                return LightbridgeFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
            case UNKNOWN:
                return LightbridgeFrequencyBand.UNKNOWN;
        }
        return LightbridgeFrequencyBand.UNKNOWN;
    }

    public static ChannelSelectionMode getOcuSyncChannelSelectionMode(final DroneOcuSyncChannelSelectionMode value) {
        switch (value) {
            case AUTO:
                return ChannelSelectionMode.AUTO;
            case MANUAL:
                return ChannelSelectionMode.MANUAL;
            case UNKNOWN:
                return ChannelSelectionMode.UNKNOWN;
        }
        return ChannelSelectionMode.UNKNOWN;
    }

    public static OcuSyncFrequencyBand getOcuSyncFrequencyBand(final DroneOcuSyncFrequencyBand value) {
        switch (value) {
            case _2_DOT_4_GHZ:
                return OcuSyncFrequencyBand.FREQUENCY_BAND_2_DOT_4_GHZ;
            case _5_DOT_8_GHZ:
                return OcuSyncFrequencyBand.FREQUENCY_BAND_5_DOT_8_GHZ;
            case DUAL:
                return OcuSyncFrequencyBand.FREQUENCY_BAND_DUAL;
            case UNKNOWN:
                return OcuSyncFrequencyBand.UNKNOWN;
        }
        return OcuSyncFrequencyBand.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoAEBCount getCameraAEBCount(final CameraAEBCount value) {
        switch (value) {
            case _3:
                return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_3;
            case _5:
                return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_5;
            case _7:
                return SettingsDefinitions.PhotoAEBCount.AEB_COUNT_7;
            case UNKNOWN:
                return SettingsDefinitions.PhotoAEBCount.UNKNOWN;
        }
        return SettingsDefinitions.PhotoAEBCount.UNKNOWN;
    }

    public static SettingsDefinitions.Aperture getCameraAperture(final CameraAperture value) {
        switch (value) {
            case F_1_DOT_6:
                return SettingsDefinitions.Aperture.F_1_DOT_6;
            case F_1_DOT_7:
                return SettingsDefinitions.Aperture.F_1_DOT_7;
            case F_1_DOT_8:
                return SettingsDefinitions.Aperture.F_1_DOT_8;
            case F_2:
                return SettingsDefinitions.Aperture.F_2;
            case F_2_DOT_2:
                return SettingsDefinitions.Aperture.F_2_DOT_2;
            case F_2_DOT_4:
                return SettingsDefinitions.Aperture.F_2_DOT_4;
            case F_2_DOT_5:
                return SettingsDefinitions.Aperture.F_2_DOT_5;
            case F_2_DOT_6:
                return SettingsDefinitions.Aperture.F_2_DOT_6;
            case F_2_DOT_8:
                return SettingsDefinitions.Aperture.F_2_DOT_8;
            case F_3_DOT_2:
                return SettingsDefinitions.Aperture.F_3_DOT_2;
            case F_3_DOT_4:
                return SettingsDefinitions.Aperture.F_3_DOT_4;
            case F_3_DOT_5:
                return SettingsDefinitions.Aperture.F_3_DOT_5;
            case F_4:
                return SettingsDefinitions.Aperture.F_4;
            case F_4_DOT_5:
                return SettingsDefinitions.Aperture.F_4_DOT_5;
            case F_4_DOT_8:
                return SettingsDefinitions.Aperture.F_4_DOT_8;
            case F_5:
                return SettingsDefinitions.Aperture.F_5;
            case F_5_DOT_6:
                return SettingsDefinitions.Aperture.F_5_DOT_6;
            case F_6_DOT_3:
                return SettingsDefinitions.Aperture.F_6_DOT_3;
            case F_6_DOT_8:
                return SettingsDefinitions.Aperture.F_6_DOT_8;
            case F_7_DOT_1:
                return SettingsDefinitions.Aperture.F_7_DOT_1;
            case F_8:
                return SettingsDefinitions.Aperture.F_8;
            case F_9:
                return SettingsDefinitions.Aperture.F_9;
            case F_9_DOT_5:
                return SettingsDefinitions.Aperture.F_9_DOT_6; //missing F_9_DOT_5
            case F_9_DOT_6:
                return SettingsDefinitions.Aperture.F_9_DOT_6;
            case F_10:
                return SettingsDefinitions.Aperture.F_10;
            case F_11:
                return SettingsDefinitions.Aperture.F_11;
            case F_13:
                return SettingsDefinitions.Aperture.F_13;
            case F_14:
                return SettingsDefinitions.Aperture.F_14;
            case F_16:
                return SettingsDefinitions.Aperture.F_16;
            case F_18:
                return SettingsDefinitions.Aperture.F_18;
            case F_19:
                return SettingsDefinitions.Aperture.F_19;
            case F_20:
                return SettingsDefinitions.Aperture.F_20;
            case F_22:
                return SettingsDefinitions.Aperture.F_22;
            case UNKNOWN:
                return SettingsDefinitions.Aperture.UNKNOWN;
        }
        return SettingsDefinitions.Aperture.UNKNOWN;
    }

    public static CameraAperture getCameraAperture(final SettingsDefinitions.Aperture value) {
        switch (value) {
            case F_1_DOT_6:
                return CameraAperture.F_1_DOT_6;
            case F_1_DOT_7:
                return CameraAperture.F_1_DOT_7;
            case F_1_DOT_8:
                return CameraAperture.F_1_DOT_8;
            case F_2:
                return CameraAperture.F_2;
            case F_2_DOT_2:
                return CameraAperture.F_2_DOT_2;
            case F_2_DOT_4:
                return CameraAperture.F_2_DOT_4;
            case F_2_DOT_5:
                return CameraAperture.F_2_DOT_5;
            case F_2_DOT_6:
                return CameraAperture.F_2_DOT_6;
            case F_2_DOT_8:
                return CameraAperture.F_2_DOT_8;
            case F_3_DOT_2:
                return CameraAperture.F_3_DOT_2;
            case F_3_DOT_4:
                return CameraAperture.F_3_DOT_4;
            case F_3_DOT_5:
                return CameraAperture.F_3_DOT_5;
            case F_4:
                return CameraAperture.F_4;
            case F_4_DOT_5:
                return CameraAperture.F_4_DOT_5;
            case F_4_DOT_8:
                return CameraAperture.F_4_DOT_8;
            case F_5:
                return CameraAperture.F_5;
            case F_5_DOT_6:
                return CameraAperture.F_5_DOT_6;
            case F_6_DOT_3:
                return CameraAperture.F_6_DOT_3;
            case F_6_DOT_8:
                return CameraAperture.F_6_DOT_8;
            case F_7_DOT_1:
                return CameraAperture.F_7_DOT_1;
            case F_8:
                return CameraAperture.F_8;
            case F_9:
                return CameraAperture.F_9;
            case F_9_DOT_6:
                return CameraAperture.F_9_DOT_6;
            case F_10:
                return CameraAperture.F_10;
            case F_11:
                return CameraAperture.F_11;
            case F_13:
                return CameraAperture.F_13;
            case F_14:
                return CameraAperture.F_14;
            case F_16:
                return CameraAperture.F_16;
            case F_18:
                return CameraAperture.F_18;
            case F_19:
                return CameraAperture.F_19;
            case F_20:
                return CameraAperture.F_20;
            case F_22:
                return CameraAperture.F_22;
            case UNKNOWN:
                return CameraAperture.UNKNOWN;
        }
        return CameraAperture.UNKNOWN;
    }

    public static SettingsDefinitions.CameraColor getCameraColor(final CameraColor value) {
        switch (value) {
            case NONE:
                return SettingsDefinitions.CameraColor.NONE;
            case ART:
                return SettingsDefinitions.CameraColor.ART;
            case BLACK_AND_WHITE:
                return SettingsDefinitions.CameraColor.BLACK_AND_WHITE;
            case BRIGHT:
                return SettingsDefinitions.CameraColor.BRIGHT;
            case D_CINELIKE:
                return SettingsDefinitions.CameraColor.D_CINELIKE;
            case PORTRAIT:
                return SettingsDefinitions.CameraColor.PORTRAIT;
            case M_31:
                return SettingsDefinitions.CameraColor.M_31;
            case K_DX:
                return SettingsDefinitions.CameraColor.K_DX;
            case PRISMO:
                return SettingsDefinitions.CameraColor.PRISMO;
            case JUGO:
                return SettingsDefinitions.CameraColor.JUGO;
            case D_LOG:
                return SettingsDefinitions.CameraColor.D_LOG;
            case TRUE_COLOR:
                return SettingsDefinitions.CameraColor.TRUE_COLOR;
            case INVERSE:
                return SettingsDefinitions.CameraColor.INVERSE;
            case REMINISCENCE:
                return SettingsDefinitions.CameraColor.REMINISCENCE;
            case SOLARIZE:
                return SettingsDefinitions.CameraColor.SOLARIZE;
            case POSTERIZE:
                return SettingsDefinitions.CameraColor.POSTERIZE;
            case WHITEBOARD:
                return SettingsDefinitions.CameraColor.WHITEBOARD;
            case BLACKBOARD:
                return SettingsDefinitions.CameraColor.BLACKBOARD;
            case AQUA:
                return SettingsDefinitions.CameraColor.AQUA;
            case DELTA:
                return SettingsDefinitions.CameraColor.DELTA;
            case DK79:
                return SettingsDefinitions.CameraColor.DK79;
            case VISION_4:
                return SettingsDefinitions.CameraColor.VISION_4;
            case VISION_6:
                return SettingsDefinitions.CameraColor.VISION_6;
            case TRUE_COLOR_EXT:
                return SettingsDefinitions.CameraColor.TRUE_COLOR_EXT;
            case FILM_A:
                return SettingsDefinitions.CameraColor.FILM_A;
            case FILM_B:
                return SettingsDefinitions.CameraColor.FILM_B;
            case FILM_C:
                return SettingsDefinitions.CameraColor.FILM_C;
            case FILM_D:
                return SettingsDefinitions.CameraColor.FILM_D;
            case FILM_E:
                return SettingsDefinitions.CameraColor.FILM_E;
            case FILM_F:
                return SettingsDefinitions.CameraColor.FILM_F;
            case FILM_G:
                return SettingsDefinitions.CameraColor.FILM_G;
            case FILM_H:
                return SettingsDefinitions.CameraColor.FILM_H;
            case FILM_I:
                return SettingsDefinitions.CameraColor.FILM_I;
            case HLG:
                return SettingsDefinitions.CameraColor.HLG;
            case UNKNOWN:
                return SettingsDefinitions.CameraColor.UNKNOWN;
        }
        return SettingsDefinitions.CameraColor.UNKNOWN;
    }

    public static SettingsDefinitions.ExposureCompensation getCameraExposureCompensation(final CameraExposureCompensation value) {
        switch (value) {
            case N_5_0:
                return SettingsDefinitions.ExposureCompensation.N_5_0;
            case N_4_7:
                return SettingsDefinitions.ExposureCompensation.N_4_7;
            case N_4_3:
                return SettingsDefinitions.ExposureCompensation.N_4_3;
            case N_4_0:
                return SettingsDefinitions.ExposureCompensation.N_4_0;
            case N_3_7:
                return SettingsDefinitions.ExposureCompensation.N_3_7;
            case N_3_3:
                return SettingsDefinitions.ExposureCompensation.N_3_3;
            case N_3_0:
                return SettingsDefinitions.ExposureCompensation.N_3_0;
            case N_2_7:
                return SettingsDefinitions.ExposureCompensation.N_2_7;
            case N_2_3:
                return SettingsDefinitions.ExposureCompensation.N_2_3;
            case N_2_0:
                return SettingsDefinitions.ExposureCompensation.N_2_0;
            case N_1_7:
                return SettingsDefinitions.ExposureCompensation.N_1_7;
            case N_1_3:
                return SettingsDefinitions.ExposureCompensation.N_1_3;
            case N_1_0:
                return SettingsDefinitions.ExposureCompensation.N_1_0;
            case N_0_7:
                return SettingsDefinitions.ExposureCompensation.N_0_7;
            case N_0_3:
                return SettingsDefinitions.ExposureCompensation.N_0_3;
            case N_0_0:
                return SettingsDefinitions.ExposureCompensation.N_0_0;
            case P_0_3:
                return SettingsDefinitions.ExposureCompensation.P_0_3;
            case P_0_7:
                return SettingsDefinitions.ExposureCompensation.P_0_7;
            case P_1_0:
                return SettingsDefinitions.ExposureCompensation.P_1_0;
            case P_1_3:
                return SettingsDefinitions.ExposureCompensation.P_1_3;
            case P_1_7:
                return SettingsDefinitions.ExposureCompensation.P_1_7;
            case P_2_0:
                return SettingsDefinitions.ExposureCompensation.P_2_0;
            case P_2_3:
                return SettingsDefinitions.ExposureCompensation.P_2_3;
            case P_2_7:
                return SettingsDefinitions.ExposureCompensation.P_2_7;
            case P_3_0:
                return SettingsDefinitions.ExposureCompensation.P_3_0;
            case P_3_3:
                return SettingsDefinitions.ExposureCompensation.P_3_3;
            case P_3_7:
                return SettingsDefinitions.ExposureCompensation.P_3_7;
            case P_4_0:
                return SettingsDefinitions.ExposureCompensation.P_4_0;
            case P_4_3:
                return SettingsDefinitions.ExposureCompensation.P_4_3;
            case P_4_7:
                return SettingsDefinitions.ExposureCompensation.P_4_7;
            case P_5_0:
                return SettingsDefinitions.ExposureCompensation.P_5_0;
            case UNKNOWN:
                return SettingsDefinitions.ExposureCompensation.UNKNOWN;
        }
        return SettingsDefinitions.ExposureCompensation.UNKNOWN;
    }

    public static CameraExposureCompensation getCameraExposureCompensation(final SettingsDefinitions.ExposureCompensation value) {
        switch (value) {
            case N_5_0:
                return CameraExposureCompensation.N_5_0;
            case N_4_7:
                return CameraExposureCompensation.N_4_7;
            case N_4_3:
                return CameraExposureCompensation.N_4_3;
            case N_4_0:
                return CameraExposureCompensation.N_4_0;
            case N_3_7:
                return CameraExposureCompensation.N_3_7;
            case N_3_3:
                return CameraExposureCompensation.N_3_3;
            case N_3_0:
                return CameraExposureCompensation.N_3_0;
            case N_2_7:
                return CameraExposureCompensation.N_2_7;
            case N_2_3:
                return CameraExposureCompensation.N_2_3;
            case N_2_0:
                return CameraExposureCompensation.N_2_0;
            case N_1_7:
                return CameraExposureCompensation.N_1_7;
            case N_1_3:
                return CameraExposureCompensation.N_1_3;
            case N_1_0:
                return CameraExposureCompensation.N_1_0;
            case N_0_7:
                return CameraExposureCompensation.N_0_7;
            case N_0_3:
                return CameraExposureCompensation.N_0_3;
            case N_0_0:
                return CameraExposureCompensation.N_0_0;
            case P_0_3:
                return CameraExposureCompensation.P_0_3;
            case P_0_7:
                return CameraExposureCompensation.P_0_7;
            case P_1_0:
                return CameraExposureCompensation.P_1_0;
            case P_1_3:
                return CameraExposureCompensation.P_1_3;
            case P_1_7:
                return CameraExposureCompensation.P_1_7;
            case P_2_0:
                return CameraExposureCompensation.P_2_0;
            case P_2_3:
                return CameraExposureCompensation.P_2_3;
            case P_2_7:
                return CameraExposureCompensation.P_2_7;
            case P_3_0:
                return CameraExposureCompensation.P_3_0;
            case P_3_3:
                return CameraExposureCompensation.P_3_3;
            case P_3_7:
                return CameraExposureCompensation.P_3_7;
            case P_4_0:
                return CameraExposureCompensation.P_4_0;
            case P_4_3:
                return CameraExposureCompensation.P_4_3;
            case P_4_7:
                return CameraExposureCompensation.P_4_7;
            case P_5_0:
                return CameraExposureCompensation.P_5_0;
            case FIXED:
            case UNKNOWN:
                return CameraExposureCompensation.UNKNOWN;
        }
        return CameraExposureCompensation.UNKNOWN;
    }

    public static SettingsDefinitions.ExposureMode getCameraExposureMode(final CameraExposureMode value) {
        switch (value) {
            case PROGRAM:
                return SettingsDefinitions.ExposureMode.PROGRAM;
            case SHUTTER_PRIORITY:
                return SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY;
            case APERTURE_PRIORITY:
                return SettingsDefinitions.ExposureMode.APERTURE_PRIORITY;
            case MANUAL:
                return SettingsDefinitions.ExposureMode.MANUAL;
            case UNKNOWN:
                return SettingsDefinitions.ExposureMode.UNKNOWN;
        }
        return SettingsDefinitions.ExposureMode.UNKNOWN;
    }

    public static SettingsDefinitions.FileIndexMode getCameraFileIndexMode(final CameraFileIndexMode value) {
        switch (value) {
            case RESET:
                return SettingsDefinitions.FileIndexMode.RESET;
            case SEQUENCE:
                return SettingsDefinitions.FileIndexMode.SEQUENCE;
            case UNKNOWN:
                return SettingsDefinitions.FileIndexMode.UNKNOWN;
        }
        return SettingsDefinitions.FileIndexMode.UNKNOWN;
    }

    public static SettingsDefinitions.FocusMode getCameraFocusMode(final CameraFocusMode value) {
        switch (value) {
            case MANUAL:
                return SettingsDefinitions.FocusMode.MANUAL;
            case AUTO:
                return SettingsDefinitions.FocusMode.AUTO;
            case AFC:
                return SettingsDefinitions.FocusMode.AFC;
            case UNKNOWN:
                return SettingsDefinitions.FocusMode.UNKNOWN;
        }
        return SettingsDefinitions.FocusMode.UNKNOWN;
    }

    public static SettingsDefinitions.ISO getCameraISO(final CameraISO value) {
        switch (value) {
            case AUTO:
                return SettingsDefinitions.ISO.AUTO;
            case _100:
                return SettingsDefinitions.ISO.ISO_100;
            case _200:
                return SettingsDefinitions.ISO.ISO_200;
            case _400:
                return SettingsDefinitions.ISO.ISO_400;
            case _800:
                return SettingsDefinitions.ISO.ISO_800;
            case _1600:
                return SettingsDefinitions.ISO.ISO_1600;
            case _3200:
                return SettingsDefinitions.ISO.ISO_3200;
            case _6400:
                return SettingsDefinitions.ISO.ISO_6400;
            case _12800:
                return SettingsDefinitions.ISO.ISO_12800;
            case _25600:
                return SettingsDefinitions.ISO.ISO_25600;
            case FIXED:
                return FIXED;
            case UNKNOWN:
                return SettingsDefinitions.ISO.UNKNOWN;
        }
        return SettingsDefinitions.ISO.UNKNOWN;
    }

    public static CameraISO getCameraISO(final SettingsDefinitions.ISO iso) {
        switch (iso) {
            case AUTO:
                return CameraISO.AUTO;
            case ISO_50:
                return CameraISO.UNKNOWN; // not found
            case ISO_100:
                return CameraISO._100;
            case ISO_200:
                return CameraISO._200;
            case ISO_400:
                return CameraISO._400;
            case ISO_800:
                return CameraISO._800;
            case ISO_1600:
                return CameraISO._1600;
            case ISO_3200:
                return CameraISO._3200;
            case ISO_6400:
                return CameraISO._6400;
            case ISO_12800:
                return CameraISO._12800;
            case ISO_25600:
                return CameraISO._25600;
            case FIXED:
                return CameraISO.FIXED;
            case UNKNOWN:
                return CameraISO.UNKNOWN;
        }
        return CameraISO.UNKNOWN;
    }

    public static SettingsDefinitions.MeteringMode getCameraMeteringMode(final CameraMeteringMode value) {
        switch (value) {
            case CENTER:
                return SettingsDefinitions.MeteringMode.CENTER;
            case AVERAGE:
                return SettingsDefinitions.MeteringMode.AVERAGE;
            case SPOT:
                return SettingsDefinitions.MeteringMode.SPOT;
            case UNKNOWN:
                return SettingsDefinitions.MeteringMode.UNKNOWN;
        }
        return SettingsDefinitions.MeteringMode.UNKNOWN;
    }

    public static SettingsDefinitions.CameraMode getCameraMode(final CameraMode value) {
        switch (value) {
            case PHOTO:
                return SettingsDefinitions.CameraMode.SHOOT_PHOTO;
            case VIDEO:
                return SettingsDefinitions.CameraMode.RECORD_VIDEO;
            case PLAYBACK:
                return SettingsDefinitions.CameraMode.PLAYBACK;
            case DOWNLOAD:
                return SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD;
            case BROADCAST:
                return SettingsDefinitions.CameraMode.BROADCAST;
            case UNKNOWN:
                return SettingsDefinitions.CameraMode.UNKNOWN;
        }
        return SettingsDefinitions.CameraMode.UNKNOWN;
    }

    public static CameraMode getCameraMode(final SettingsDefinitions.CameraMode value) {
        switch (value) {
            case SHOOT_PHOTO:
                return CameraMode.PHOTO;
            case RECORD_VIDEO:
                return CameraMode.VIDEO;
            case PLAYBACK:
                return CameraMode.PLAYBACK;
            case MEDIA_DOWNLOAD:
                return CameraMode.DOWNLOAD;
            case BROADCAST:
                return CameraMode.BROADCAST;
            case UNKNOWN:
                return CameraMode.UNKNOWN;
        }
        return CameraMode.UNKNOWN;
    }

    public static SettingsDefinitions.FlatCameraMode getCameraModeFlat(final CameraMode value) {
        switch (value) {
            case PHOTO:
                return SettingsDefinitions.FlatCameraMode.PHOTO_SINGLE;
            case VIDEO:
                return SettingsDefinitions.FlatCameraMode.VIDEO_NORMAL;
            case PLAYBACK:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
            case DOWNLOAD:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
            case BROADCAST:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
            case UNKNOWN:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
        }
        return SettingsDefinitions.FlatCameraMode.UNKNOWN;
    }

    public static SettingsDefinitions.FlatCameraMode getCameraModeFlat(final CameraPhotoMode value) {
        switch (value) {
            case SINGLE:
                return SettingsDefinitions.FlatCameraMode.PHOTO_SINGLE;
            case HDR:
                return SettingsDefinitions.FlatCameraMode.PHOTO_HDR;
            case BURST:
                return SettingsDefinitions.FlatCameraMode.PHOTO_BURST;
            case AEB:
                return SettingsDefinitions.FlatCameraMode.PHOTO_AEB;
            case INTERVAL:
                return SettingsDefinitions.FlatCameraMode.PHOTO_INTERVAL;
            case TIME_LAPSE:
                return SettingsDefinitions.FlatCameraMode.PHOTO_TIME_LAPSE;
            case RAW_BURST:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
            case SHALLOW_FOCUS:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
            case PANORAMA:
                return SettingsDefinitions.FlatCameraMode.PHOTO_PANORAMA;
            case EHDR:
                return SettingsDefinitions.FlatCameraMode.PHOTO_EHDR;
            case HYPER_LIGHT:
                return SettingsDefinitions.FlatCameraMode.PHOTO_HYPER_LIGHT;
            case HIGH_RESOLUTION:
                return SettingsDefinitions.FlatCameraMode.PHOTO_HIGH_RESOLUTION;
            //FIXME case SMART: return SettingsDefinitions.FlatCameraMode.PHOTO_SMART;
            case INTERNAL_AI_SPOT_CHECKING:
                return SettingsDefinitions.FlatCameraMode.INTERNAL_AI_SPOT_CHECKING;
            case UNKNOWN:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
        }
        return SettingsDefinitions.FlatCameraMode.UNKNOWN;
    }

    public static SettingsDefinitions.FlatCameraMode getCameraModeFlat(final CameraVideoMode value) {
        switch (value) {
            case NORMAL:
                return SettingsDefinitions.FlatCameraMode.VIDEO_NORMAL;
            case HDR:
                return SettingsDefinitions.FlatCameraMode.VIDEO_HDR;
            case SLOW_MOTION:
                return SettingsDefinitions.FlatCameraMode.SLOW_MOTION;
            case UNKNOWN:
                return SettingsDefinitions.FlatCameraMode.UNKNOWN;
        }
        return SettingsDefinitions.FlatCameraMode.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoAspectRatio getCameraPhotoAspectRatio(final CameraPhotoAspectRatio value) {
        switch (value) {
            case _4_3:
                return SettingsDefinitions.PhotoAspectRatio.RATIO_4_3;
            case _16_9:
                return SettingsDefinitions.PhotoAspectRatio.RATIO_16_9;
            case _3_2:
                return SettingsDefinitions.PhotoAspectRatio.RATIO_3_2;
            case UNKNOWN:
                return SettingsDefinitions.PhotoAspectRatio.UNKNOWN;
        }
        return SettingsDefinitions.PhotoAspectRatio.UNKNOWN;
    }

    public static SettingsDefinitions.PhotoFileFormat getCameraPhotoFileFormat(final CameraPhotoFileFormat value) {
        switch (value) {
            case RAW:
                return SettingsDefinitions.PhotoFileFormat.RAW;
            case JPEG:
                return SettingsDefinitions.PhotoFileFormat.JPEG;
            case RAW_JPEG:
                return SettingsDefinitions.PhotoFileFormat.RAW_AND_JPEG;
            case TIFF_14_BIT:
                return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT;
            case RADIOMETRIC_JPEG:
                return SettingsDefinitions.PhotoFileFormat.RADIOMETRIC_JPEG;
            case TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION:
                return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION;
            case TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION:
                return SettingsDefinitions.PhotoFileFormat.TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION;
            case UNKNOWN:
                return SettingsDefinitions.PhotoFileFormat.UNKNOWN;
        }
        return SettingsDefinitions.PhotoFileFormat.UNKNOWN;
    }

    public static SettingsDefinitions.ShootPhotoMode getCameraPhotoMode(final CameraPhotoMode value) {
        switch (value) {
            case SINGLE:
                return SettingsDefinitions.ShootPhotoMode.SINGLE;
            case HDR:
                return SettingsDefinitions.ShootPhotoMode.HDR;
            case BURST:
                return SettingsDefinitions.ShootPhotoMode.BURST;
            case AEB:
                return SettingsDefinitions.ShootPhotoMode.AEB;
            case INTERVAL:
                return SettingsDefinitions.ShootPhotoMode.INTERVAL;
            case TIME_LAPSE:
                return SettingsDefinitions.ShootPhotoMode.TIME_LAPSE;
            case RAW_BURST:
                return SettingsDefinitions.ShootPhotoMode.RAW_BURST;
            case SHALLOW_FOCUS:
                return SettingsDefinitions.ShootPhotoMode.SHALLOW_FOCUS;
            case PANORAMA:
                return SettingsDefinitions.ShootPhotoMode.PANORAMA;
            case EHDR:
                return SettingsDefinitions.ShootPhotoMode.EHDR;
            case HYPER_LIGHT:
                return SettingsDefinitions.ShootPhotoMode.HYPER_LIGHT;
            case UNKNOWN:
                return SettingsDefinitions.ShootPhotoMode.UNKNOWN;
        }
        return SettingsDefinitions.ShootPhotoMode.UNKNOWN;
    }

    public static CameraStorageLocation getStorageLocation(final SettingsDefinitions.StorageLocation value) {
        switch (value) {
            case SDCARD:
                return CameraStorageLocation.SD_CARD;
            case INTERNAL_STORAGE:
                return CameraStorageLocation.INTERNAL;
            case UNKNOWN:
                return CameraStorageLocation.UNKNOWN;
        }
        return CameraStorageLocation.UNKNOWN;
    }

    public static SettingsDefinitions.ShutterSpeed getCameraShutterSpeed(final CameraShutterSpeed value) {
        switch (value) {
            case _1_8000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_8000;
            case _1_6400:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6400;
            case _1_6000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6000;
            case _1_5000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_5000;
            case _1_4000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_4000;
            case _1_3200:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3200;
            case _1_3000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3000;
            case _1_2500:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2500;
            case _1_2000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2000;
            case _1_1600:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1600;
            case _1_1500:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1500;
            case _1_1250:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1250;
            case _1_1000:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1000;
            case _1_800:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800;
            case _1_750:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_800; //missing 1/750
            case _1_725:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_725;
            case _1_640:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_640;
            case _1_500:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_500;
            case _1_400:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_400;
            case _1_350:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_350;
            case _1_320:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_320;
            case _1_250:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_250;
            case _1_240:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_240;
            case _1_200:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_200;
            case _1_180:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_180;
            case _1_160:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_160;
            case _1_125:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_125;
            case _1_120:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_120;
            case _1_100:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_100;
            case _1_90:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_90;
            case _1_80:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_80;
            case _1_60:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_60;
            case _1_50:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_50;
            case _1_45:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_50; //missing 1/45
            case _1_40:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_40;
            case _1_30:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_30;
            case _1_25:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_25;
            case _1_20:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_20;
            case _1_15:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_15;
            case _1_12_DOT_5:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_12_DOT_5;
            case _1_10:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_10;
            case _1_8:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_8;
            case _1_6_DOT_25:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6_DOT_25;
            case _1_6:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_6_DOT_25; //missing 1/6
            case _1_5:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_5;
            case _1_4:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_4;
            case _1_3:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_3;
            case _1_2_DOT_5:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2_DOT_5;
            case _0_DOT_3:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2_DOT_5; //missing 0.3
            case _1_2:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_2;
            case _1_1_DOT_67:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1_DOT_67;
            case _0_DOT_7:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1_DOT_67; //missing 0.7
            case _1:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1;
            case _1_DOT_3:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_3;
            case _1_DOT_4:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_3; //missing 1.4
            case _1_DOT_6:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_DOT_6;
            case _2:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_2;
            case _2_DOT_5:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_2_DOT_5;
            case _3:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_3;
            case _3_DOT_2:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_3_DOT_2;
            case _4:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_4;
            case _5:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_5;
            case _6:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_6;
            case _7:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_7;
            case _8:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_8;
            case _9:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_9;
            case _10:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_10;
            case _11:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_10; //missing 11
            case _13:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_13;
            case _15:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_15;
            case _16:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_15; //missing 16
            case _20:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_20;
            case _23:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_20; //missing 23
            case _25:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_25;
            case _30:
                return SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_30;
            case UNKNOWN:
                return SettingsDefinitions.ShutterSpeed.UNKNOWN;
        }
        return SettingsDefinitions.ShutterSpeed.UNKNOWN;
    }

    public static CameraShutterSpeed getCameraShutterSpeed(final SettingsDefinitions.ShutterSpeed value) {
        switch (value) {
            case SHUTTER_SPEED_1:
                return CameraShutterSpeed._1;
            case SHUTTER_SPEED_1_1_DOT_25:
                return CameraShutterSpeed.UNKNOWN; // not found
            case SHUTTER_SPEED_1_1_DOT_67:
                return CameraShutterSpeed._1_1_DOT_67;
            case SHUTTER_SPEED_1_20000:
                return CameraShutterSpeed._1_2000;
            case SHUTTER_SPEED_1_16000:
                return CameraShutterSpeed._1_1600;
            case SHUTTER_SPEED_1_12800:
                return CameraShutterSpeed.UNKNOWN; // not found
            case SHUTTER_SPEED_1_10000:
                return CameraShutterSpeed._1_1000;
            case SHUTTER_SPEED_1_8000:
                return CameraShutterSpeed._1_8000;
            case SHUTTER_SPEED_1_6400:
                return CameraShutterSpeed._1_6400;
            case SHUTTER_SPEED_1_6000:
                return CameraShutterSpeed._1_6000;
            case SHUTTER_SPEED_1_5000:
                return CameraShutterSpeed._1_5000;
            case SHUTTER_SPEED_1_4000:
                return CameraShutterSpeed._1_4000;
            case SHUTTER_SPEED_1_3200:
                return CameraShutterSpeed._1_3200;
            case SHUTTER_SPEED_1_3000:
                return CameraShutterSpeed._1_3000;
            case SHUTTER_SPEED_1_2500:
                return CameraShutterSpeed._1_2500;
            case SHUTTER_SPEED_1_2000:
                return CameraShutterSpeed._1_2000;
            case SHUTTER_SPEED_1_1600:
                return CameraShutterSpeed._1_1600;
            case SHUTTER_SPEED_1_1500:
                return CameraShutterSpeed._1_1500;
            case SHUTTER_SPEED_1_1250:
                return CameraShutterSpeed._1_1250;
            case SHUTTER_SPEED_1_1000:
                return CameraShutterSpeed._1_1000;
            case SHUTTER_SPEED_1_800:
                return CameraShutterSpeed._1_800;
            case SHUTTER_SPEED_1_725:
                return CameraShutterSpeed._1_725;
            case SHUTTER_SPEED_1_640:
                return CameraShutterSpeed._1_640;
            case SHUTTER_SPEED_1_500:
                return CameraShutterSpeed._1_500;
            case SHUTTER_SPEED_1_400:
                return CameraShutterSpeed._1_400;
            case SHUTTER_SPEED_1_350:
                return CameraShutterSpeed._1_350;
            case SHUTTER_SPEED_1_320:
                return CameraShutterSpeed._1_320;
            case SHUTTER_SPEED_1_250:
                return CameraShutterSpeed._1_250;
            case SHUTTER_SPEED_1_240:
                return CameraShutterSpeed._1_240;
            case SHUTTER_SPEED_1_200:
                return CameraShutterSpeed._1_200;
            case SHUTTER_SPEED_1_180:
                return CameraShutterSpeed._1_180;
            case SHUTTER_SPEED_1_160:
                return CameraShutterSpeed._1_160;
            case SHUTTER_SPEED_1_125:
                return CameraShutterSpeed._1_125;
            case SHUTTER_SPEED_1_120:
                return CameraShutterSpeed._1_120;
            case SHUTTER_SPEED_1_100:
                return CameraShutterSpeed._1_100;
            case SHUTTER_SPEED_1_90:
                return CameraShutterSpeed._1_90;
            case SHUTTER_SPEED_1_80:
                return CameraShutterSpeed._1_80;
            case SHUTTER_SPEED_1_60:
                return CameraShutterSpeed._1_60;
            case SHUTTER_SPEED_1_50:
                return CameraShutterSpeed._1_50;
            case SHUTTER_SPEED_1_40:
                return CameraShutterSpeed._1_40;
            case SHUTTER_SPEED_1_30:
                return CameraShutterSpeed._1_30;
            case SHUTTER_SPEED_1_25:
                return CameraShutterSpeed._1_25;
            case SHUTTER_SPEED_1_20:
                return CameraShutterSpeed._1_20;
            case SHUTTER_SPEED_1_15:
                return CameraShutterSpeed._1_15;
            case SHUTTER_SPEED_1_12_DOT_5:
                return CameraShutterSpeed._1_12_DOT_5;
            case SHUTTER_SPEED_1_10:
                return CameraShutterSpeed._1_10;
            case SHUTTER_SPEED_1_8:
                return CameraShutterSpeed._1_8;
            case SHUTTER_SPEED_1_6_DOT_25:
                return CameraShutterSpeed._1_6_DOT_25;
            case SHUTTER_SPEED_1_5:
                return CameraShutterSpeed._1_5;
            case SHUTTER_SPEED_1_4:
                return CameraShutterSpeed._1_4;
            case SHUTTER_SPEED_1_3:
                return CameraShutterSpeed._1_3;
            case SHUTTER_SPEED_1_2_DOT_5:
                return CameraShutterSpeed._1_2_DOT_5;
            case SHUTTER_SPEED_1_2:
                return CameraShutterSpeed._1_2;
            case SHUTTER_SPEED_1_DOT_3:
                return CameraShutterSpeed._1_DOT_3;
            case SHUTTER_SPEED_1_DOT_6:
                return CameraShutterSpeed._1_DOT_6;
            case SHUTTER_SPEED_2:
                return CameraShutterSpeed._2;
            case SHUTTER_SPEED_2_DOT_5:
                return CameraShutterSpeed._2_DOT_5;
            case SHUTTER_SPEED_3:
                return CameraShutterSpeed._3;
            case SHUTTER_SPEED_3_DOT_2:
                return CameraShutterSpeed._3_DOT_2;
            case SHUTTER_SPEED_4:
                return CameraShutterSpeed._4;
            case SHUTTER_SPEED_5:
                return CameraShutterSpeed._5;
            case SHUTTER_SPEED_6:
                return CameraShutterSpeed._6;
            case SHUTTER_SPEED_7:
                return CameraShutterSpeed._7;
            case SHUTTER_SPEED_8:
                return CameraShutterSpeed._8;
            case SHUTTER_SPEED_9:
                return CameraShutterSpeed._9;
            case SHUTTER_SPEED_10:
                return CameraShutterSpeed._10;
            case SHUTTER_SPEED_13:
                return CameraShutterSpeed._13;
            case SHUTTER_SPEED_15:
                return CameraShutterSpeed._15;
            case SHUTTER_SPEED_20:
                return CameraShutterSpeed._20;
            case SHUTTER_SPEED_25:
                return CameraShutterSpeed._25;
            case SHUTTER_SPEED_30:
                return CameraShutterSpeed._30;
            case UNKNOWN:
                return CameraShutterSpeed.UNKNOWN;
        }
        return CameraShutterSpeed.UNKNOWN;
    }

    public static SettingsDefinitions.StorageLocation getCameraStorageLocation(final CameraStorageLocation value) {
        switch (value) {
            case SD_CARD:
                return SettingsDefinitions.StorageLocation.SDCARD;
            case INTERNAL:
                return SettingsDefinitions.StorageLocation.INTERNAL_STORAGE;
            case UNKNOWN:
                return SettingsDefinitions.StorageLocation.UNKNOWN;
        }
        return SettingsDefinitions.StorageLocation.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFileCompressionStandard getCameraVideoFileCompressionStandard(final CameraVideoFileCompressionStandard value) {
        switch (value) {
            case H264:
                return SettingsDefinitions.VideoFileCompressionStandard.H264;
            case H265:
                return SettingsDefinitions.VideoFileCompressionStandard.H265;
            case UNKNOWN:
                return SettingsDefinitions.VideoFileCompressionStandard.Unknown;
        }
        return SettingsDefinitions.VideoFileCompressionStandard.Unknown;
    }

    public static SettingsDefinitions.VideoFileFormat getCameraVideoFileFormat(final CameraVideoFileFormat value) {
        switch (value) {
            case MOV:
                return SettingsDefinitions.VideoFileFormat.MOV;
            case MP4:
                return SettingsDefinitions.VideoFileFormat.MP4;
            case TIFF_SEQ:
                return SettingsDefinitions.VideoFileFormat.TIFF_SEQ;
            case SEQ:
                return SettingsDefinitions.VideoFileFormat.SEQ;
            case UNKNOWN:
                return SettingsDefinitions.VideoFileFormat.UNKNOWN;
        }
        return SettingsDefinitions.VideoFileFormat.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFrameRate getCameraVideoVideoFrameRate(final CameraVideoFrameRate value) {
        switch (value) {
            case _23_DOT_976:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_23_DOT_976_FPS;
            case _24:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_24_FPS;
            case _25:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_25_FPS;
            case _29_DOT_970:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_29_DOT_970_FPS;
            case _30:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_30_FPS;
            case _47_DOT_950:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_47_DOT_950_FPS;
            case _48:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_48_FPS;
            case _50:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_50_FPS;
            case _59_DOT_940:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_59_DOT_940_FPS;
            case _60:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_60_FPS;
            case _90:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_90_FPS;
            case _96:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_96_FPS;
            case _100:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_100_FPS;
            case _120:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_120_FPS;
            case _8_DOT_7:
                return SettingsDefinitions.VideoFrameRate.FRAME_RATE_8_DOT_7_FPS;
            case UNKNOWN:
                return SettingsDefinitions.VideoFrameRate.UNKNOWN;
        }
        return SettingsDefinitions.VideoFrameRate.UNKNOWN;
    }

    public static SettingsDefinitions.VideoResolution getCameraVideoVideoResolution(final CameraVideoResolution value) {
        switch (value) {
            case _336x256:
                return SettingsDefinitions.VideoResolution.RESOLUTION_336x256;
            case _640x360:
                return SettingsDefinitions.VideoResolution.RESOLUTION_640x360;
            case _640x480:
                return SettingsDefinitions.VideoResolution.RESOLUTION_640x480;
            case _640x512:
                return SettingsDefinitions.VideoResolution.RESOLUTION_640x512;
            case _1280x720:
                return SettingsDefinitions.VideoResolution.RESOLUTION_1280x720;
            case _1920x1080:
                return SettingsDefinitions.VideoResolution.RESOLUTION_1920x1080;
            case _2048x1080:
                return SettingsDefinitions.VideoResolution.RESOLUTION_2048x1080;
            case _2688x1512:
                return SettingsDefinitions.VideoResolution.RESOLUTION_2688x1512;
            case _2704x1520:
                return SettingsDefinitions.VideoResolution.RESOLUTION_2704x1520;
            case _2720x1530:
                return SettingsDefinitions.VideoResolution.RESOLUTION_2720x1530;
            case _3712x2088:
                return SettingsDefinitions.VideoResolution.RESOLUTION_3712x2088;
            case _3840x1572:
                return SettingsDefinitions.VideoResolution.RESOLUTION_3840x1572;
            case _3840x2160:
                return SettingsDefinitions.VideoResolution.RESOLUTION_3840x2160;
            case _3944x2088:
                return SettingsDefinitions.VideoResolution.RESOLUTION_3944x2088;
            case _4096x2160:
                return SettingsDefinitions.VideoResolution.RESOLUTION_4096x2160;
            case _4608x2160:
                return SettingsDefinitions.VideoResolution.RESOLUTION_4608x2160;
            case _4608x2592:
                return SettingsDefinitions.VideoResolution.RESOLUTION_4608x2592;
            case _5280x2160:
                return SettingsDefinitions.VideoResolution.RESOLUTION_5280x2160;
            case _5280x2972:
                return SettingsDefinitions.VideoResolution.RESOLUTION_5280x2972;
            case _5760X3240:
                return SettingsDefinitions.VideoResolution.RESOLUTION_5760X3240;
            case _6016X3200:
                return SettingsDefinitions.VideoResolution.RESOLUTION_6016X3200;
            case MAX:
                return SettingsDefinitions.VideoResolution.RESOLUTION_MAX;
            case NO_SSD_VIDEO:
                return SettingsDefinitions.VideoResolution.NO_SSD_VIDEO;
            case UNKNOWN:
                return SettingsDefinitions.VideoResolution.UNKNOWN;
        }
        return SettingsDefinitions.VideoResolution.UNKNOWN;
    }

    public static SettingsDefinitions.VideoFov getCameraVideoVideoFieldOfView(final CameraVideoFieldOfView value) {
        switch (value) {
            case DEFAULT:
                return SettingsDefinitions.VideoFov.DEFAULT;
            case MIDDLE:
                return SettingsDefinitions.VideoFov.MIDDLE;
            case NARROW:
                return SettingsDefinitions.VideoFov.NARROW;
            case WIDE:
                return SettingsDefinitions.VideoFov.WIDE;
            case UNKNOWN:
                return SettingsDefinitions.VideoFov.UNKNOWN;
        }
        return SettingsDefinitions.VideoFov.UNKNOWN;
    }

    public static SettingsDefinitions.VideoStandard getCameraVideoStandard(final CameraVideoStandard value) {
        switch (value) {
            case PAL:
                return SettingsDefinitions.VideoStandard.PAL;
            case NTSC:
                return SettingsDefinitions.VideoStandard.NTSC;
            case UNKNOWN:
                return SettingsDefinitions.VideoStandard.UNKNOWN;
        }
        return SettingsDefinitions.VideoStandard.UNKNOWN;
    }

    public static WhiteBalancePreset getCameraWhiteBalancePreset(final CameraWhiteBalancePreset value) {
        switch (value) {
            case AUTO:
                return WhiteBalancePreset.AUTO;
            case SUNNY:
                return WhiteBalancePreset.SUNNY;
            case CLOUDY:
                return WhiteBalancePreset.CLOUDY;
            case WATER_SURFACE:
                return WhiteBalancePreset.WATER_SURFACE;
            case INDOOR_INCANDESCENT:
                return WhiteBalancePreset.INDOOR_INCANDESCENT;
            case INDOOR_FLUORESCENT:
                return WhiteBalancePreset.INDOOR_FLUORESCENT;
            case CUSTOM:
                return WhiteBalancePreset.CUSTOM;
            case NEUTRAL:
                return PRESET_NEUTRAL;
            case UNKNOWN:
                return WhiteBalancePreset.UNKNOWN;
        }
        return WhiteBalancePreset.UNKNOWN;
    }

    public static CameraWhiteBalancePreset getCameraWhiteBalancePreset(final WhiteBalancePreset value) {
        switch (value) {
            case AUTO:
                return CameraWhiteBalancePreset.AUTO;
            case SUNNY:
                return SUNNY;
            case CLOUDY:
                return CLOUDY;
            case WATER_SURFACE:
                return WATER_SURFACE;
            case INDOOR_INCANDESCENT:
                return INDOOR_INCANDESCENT;
            case INDOOR_FLUORESCENT:
                return INDOOR_FLUORESCENT;
            case CUSTOM:
                return CUSTOM;
            case PRESET_NEUTRAL:
                return NEUTRAL;
            case UNKNOWN:
                return UNKNOWN;
        }
        return UNKNOWN;
    }

    public static GimbalMode getGimbalMode(final com.dronelink.core.kernel.core.enums.GimbalMode value) {
        switch (value) {
            case FREE:
                return GimbalMode.FREE;
            case FPV:
                return GimbalMode.FPV;
            case YAW_FOLLOW:
                return GimbalMode.YAW_FOLLOW;
            case UNKNOWN:
                return GimbalMode.UNKNOWN;
        }
        return GimbalMode.UNKNOWN;
    }

    public static boolean isAdjustPitchSupported(final Gimbal gimbal) {
        final Map<CapabilityKey, DJIParamCapability> capabilities = gimbal.getCapabilities();
        return capabilities != null && capabilities.containsKey(CapabilityKey.ADJUST_PITCH) && capabilities.get(CapabilityKey.ADJUST_PITCH).isSupported();
    }

    public static boolean isAdjustRollSupported(final Gimbal gimbal) {
        final Map<CapabilityKey, DJIParamCapability> capabilities = gimbal.getCapabilities();
        return capabilities != null && capabilities.containsKey(CapabilityKey.ADJUST_ROLL) && capabilities.get(CapabilityKey.ADJUST_ROLL).isSupported();
    }

    public static boolean isAdjustYawSupported(final Gimbal gimbal) {
        final Map<CapabilityKey, DJIParamCapability> capabilities = gimbal.getCapabilities();
        return capabilities != null && capabilities.containsKey(CapabilityKey.ADJUST_YAW) && capabilities.get(CapabilityKey.ADJUST_YAW).isSupported();
    }

    public static boolean isAdjustYaw360Supported(final Gimbal gimbal) {
        final Map<CapabilityKey, DJIParamCapability> capabilities = gimbal.getCapabilities();
        if (capabilities != null && capabilities.containsKey(CapabilityKey.ADJUST_YAW)) {
            final DJIParamCapability capability = capabilities.get(CapabilityKey.ADJUST_YAW);
            if (capability instanceof DJIParamMinMaxCapability) {
                final DJIParamMinMaxCapability capabilityMinMax = (DJIParamMinMaxCapability) capability;
                return capabilityMinMax.isSupported() && capabilityMinMax.getMin().intValue() <= -180 && capabilityMinMax.getMax().intValue() >= 180;
            }
        }
        return false;
    }

    public static Message getMessage(final DJIDiagnostics diagnostics) {
        Message.Level level = null;

        final DiagnosticsBaseHandler.DJIDiagnosticsError error = DiagnosticsBaseHandler.DJIDiagnosticsError.find(diagnostics.getCode());
        if (error != null) {
            switch (error) {
                case CAMERA_UPGRADE_ERROR:
                case CAMERA_SENSOR_ERROR:
                case CAMERA_OVER_HEAT:
                case CAMERA_ENCRYPTION_ERROR:
                case CAMERA_SD_CARD_ERROR:
                case CAMERA_CHIP_OVER_HEAT:
                case CAMERA_TemperaturesTooHighToStopRecord:
                case GIMBAL_GYROSCOPE_ERROR:
                case GIMBAL_PITCH_ERROR:
                case GIMBAL_ROLL_ERROR:
                case GIMBAL_YAW_ERROR:
                case GIMBAL_CONNECT_TO_FC_ERROR:
                case BATTERY_CELL_BROKEN:
                case BATTERY_COMMUNICATION_FAIL:
                case REMOTE_CONTROLLER_FPGA_ERROR:
                case REMOTE_CONTROLLER_TRANSMITTER_ERROR:
                case REMOTE_CONTROLLER_BATTERY_ERROR:
                case REMOTE_CONTROLLER_GPS_ERROR:
                case REMOTE_CONTROLLER_ENCRYPTION_ERROR:
                case REMOTE_CONTROLLER_IDLE_TOO_LONG:
                case REMOTE_CONTROLLER_RESET:
                case REMOTE_CONTROLLER_OVER_HEAT:
                case REMOTE_CONTROLLER_GO_HOME_FAIL:
                case CENTRAL_BOARD_CONNECT_TO_BATTERY_ERROR:
                case CENTRAL_BOARD_CONNECT_TO_GPS_ERROR:
                case CENTRAL_BOARD_CONNECT_TO_FC_ERROR:
                case VIDEO_DECODER_ENCRYPTION_ERROR:
                case VIDEO_DECODER_CONNECT_TO_DESERIALIZER_ERROR:
                case AIR_ENCODER_ERROR:
                case AIR_ENCODER_UPGRADE:
                case AIR_LINK_NO_SIGNAL:
                case FLIGHT_CONTROLLER_IMU_DATA_ERROR:
                case FLIGHT_CONTROLLER_IMU_ERROR:
                case FLIGHT_CONTROLLER_IMU_INIT_FAILED:
                case FLIGHT_CONTROLLER_BAROMETER_INIT_FAILED:
                case FLIGHT_CONTROLLER_BAROMETER_ERROR:
                case FLIGHT_CONTROLLER_ACCELEROMETER_INIT_FAILED:
                case FLIGHT_CONTROLLER_GYROSCOPE_ERROR:
                case FLIGHT_CONTROLLER_ATTITUDE_ERROR:
                case FLIGHT_CONTROLLER_DATA_RECORD_ERROR:
                case FLIGHT_CONTROLLER_TAKEOFF_FAILED:
                case FLIGHT_CONTROLLER_SYSTEM_ERROR:
                case FLIGHT_CONTROLLER_COMPASS_NEED_RESTART:
                case FLIGHT_CONTROLLER_MOTOR_START_ERROR:
                case FLIGHT_CONTROLLER_NO_PROPELLER:
                case FLIGHT_CONTROLLER_MOTOR_STOP_REASON:
                case FLIGHT_CONTROLLER_THREE_PROPELLER_EMERGENCY_LANDING:
                case FLIGHT_CONTROLLER_LANDING_PROTECTION:
                case FLIGHT_CONTROLLER_AIRCRAFT_PROPULSION_SYSTEM_ERROR:
                case FLIGHT_CONTROLLER_KERNEL_BOARD_HIGH_TEMPERATURE:
                case FLIGHT_CONTROLLER_MC_DATA_ERROR:
                case FLIGHT_CONTROLLER_BATTERY_NOT_IN_POSITION:
                case FLIGHT_CONTROLLER_ENABLE_NEAR_GROUND_ALERT:
                case FLIGHT_CONTROLLER_MOTOR_BLOCKED:
                case FLIGHT_CONTROLLER_NOT_ENOUGH_FORCE:
                case FLIGHT_CONTROLLER_OVER_HEAT_GO_HOME:
                case FLIGHT_CONTROLLER_COMPASS_INSTALL_ERROR:
                case FLIGHT_CONTROLLER_GPS_ERROR:
                case FLIGHT_CONTROLLER_MOTOR_STOP_FOR_ESC_SHORT_CIRCUIT:
                case FLIGHT_CONTROLLER_ENV_STATE_TEMP_TOO_LOW:
                case FLIGHT_CONTROLLER_ENV_STATE_TEMP_TOO_HIGH:
                case FLIGHT_CONTROLLER_ONLY_SUPPORT_ATTI_MODE:
                case FLIGHT_CONTROLLER_LOW_VOLTAGE_GOING_HOME:
                case FLIGHT_CONTROLLER_SMART_LOW_POWER_GO_HOME:
                case FLIGHT_CONTROLLER_LOW_VOLTAGE_LANDING:
                case VISION_PROPELLER_GUARD:
                case VISION_SENSOR_ERROR:
                case VISION_SENSOR_CALIBRATION_ERROR:
                case VISION_SENSOR_COMMUNICATION_ERROR:
                case VISION_SYSTEM_ERROR:
                case GIMBAL_LOCATE_ERROR:
                case RTK_POSITIONING_ERROR:
                case RTK_ORIENTEERING_ERROR:
                    level = Message.Level.ERROR;
                    break;

                case BATTERY_DANGEROUS_WARNING_SERIOUS:
                case FLIGHT_CONTROLLER_OUT_OF_CONTROL_GOING_HOME:
                case FLIGHT_CONTROLLER_COMPASS_ABNORMAL:
                case FLIGHT_CONTROLLER_STRONG_GALE_WARNING:
                case FLIGHT_CONTROLLER_GPS_SIGNAL_BLOCKED_BY_GIMBAL:
                    level = Message.Level.DANGER;
                    break;

                case BATTERY_DISCHARGE_OVER_CURRENT:
                case BATTERY_DISCHARGE_OVER_HEAT:
                case BATTERY_LOW_TEMPERATURE:
                case BATTERY_ILLEGAL:
                case BATTERY_DIFF_USAGE:
                case BATTERY_OVER_LOAD:
                case BATTERY_LOW_VOLTAGE:
                case REMOTE_CONTROLLER_BATTERY_LOW:
                case AIR_LINK_LOW_RC_SIGNAL:
                case AIR_LINK_STRONG_RC_RADIO_SIGNAL_NOISE:
                case AIR_LINK_LOW_RADIO_SIGNAL:
                case AIR_LINK_STRONG_RADIO_SIGNAL_NOISE:
                case AIR_LINK_WIFI_MAGNETIC_INTERFERENCE_HIGH:
                case FLIGHT_CONTROLLER_USING_WRONG_PROPELLERS:
                case FLIGHT_CONTROLLER_OUT_OF_FLIGHT_RADIUS_LIMIT:
                case FLIGHT_CONTROLLER_HEIGHT_LIMIT_REASON_NO_GPS:
                case FLIGHT_CONTROLLER_HEIGHT_LIMIT_REASON_COMPASS_INTERRUPT:
                case FLIGHT_CONTROLLER_NO_REAL_NAME_HEIGHT_LIMIT:
                case FLIGHT_CONTROLLER_WATER_SURFACE_WARNING:
                case FLIGHT_CONTROLLER_PADDLE_HAS_ICE_ON_IT:
                case FLIGHT_CONTROLLER_COVER_FLIGHT_ENABLE_LIMIT:
                case VISION_WEAK_AMBIENT_LIGHT:
                case FLIGHT_CONTROLLER_HEADING_CONTROL_ABNORMAL:
                case FLIGHT_CONTROLLER_AIRCRAFT_VIBRATION_ABNORMAL:
                case FLIGHT_CONTROLLER_TILT_CONTROL_ABNORMAL:
                    level = Message.Level.WARNING;
                    break;

                case CAMERA_NO_SD_CARD:
                case CAMERA_SD_CARD_NO_SPACE:
                case CAMERA_SD_CARD_FULL:
                case CAMERA_SD_CARD_READ_ONLY:
                case CAMERA_SD_CARD_NOT_FORMATTED:
                case BATTERY_NEED_STUDY:
                case SINGLE_BATTERY_MODE:
                case FAKE_BATTERY_MODE:
                case BATTERY_CYCLE_TIME_OVER:
                case BATTERY_SHORT_CUT:
                case REMOTE_CONTROLLER_NEED_CALIBRATION:
                case FLIGHT_CONTROLLER_IMU_NEED_CALIBRATION:
                case FLIGHT_CONTROLLER_IMU_CALIBRATION_INCOMPLETE:
                case FLIGHT_CONTROLLER_IMU_HEATING:
                case FLIGHT_CONTROLLER_MC_READING_DATA:
                case VISION_SYSTEM_NEED_CALIBRATION:
                    level = Message.Level.INFO;
                    break;

                case a:
                    break;
            }
        }

        if (level == null) {
            return null;
        }

        return new Message(diagnostics.getReason(), diagnostics.getSolution(), level);
    }

    public static Message getMessage(final Context context, final FlyZoneState flyZoneState) {
        Message.Level level = null;

        switch (flyZoneState) {
            case CLEAR:
            case UNKNOWN:
                break;

            case NEAR_RESTRICTED_ZONE:
            case IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
            case IN_WARNING_ZONE:
            case SUSPECTED_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
            case PHONE_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
            case IN_ENHANCE_WARNING_ZONE:
            case SUSPECTED_IN_RESTRICTED_ZONE:
            case NEAR_HEIGHT_LIMITED_ZONE:
                level = Message.Level.WARNING;
                break;

            case IN_RESTRICTED_ZONE:
            case PHONE_IN_RESTRICTED_ZONE:
                level = Message.Level.DANGER;
                break;

            case IN_AUTHORIZED_ZONE:
            case IN_AUTHORIZED_ZONE_WITH_LICENSE:
            case SUSPECTED_IN_AUTHORIZED_ZONE:
            case NEAR_AUTHORIZED_ZONE_WITH_LICENSE:
            case NEAR_AUTHORIZED_ZONE:
            case NEAR_MULTIPLE_TYPE_FLY_ZONE:
            case OUTSIDE_SPECIAL_UNLOCK_ZONE:
            case FLY_TOUCH_AUTHORIZE_HAS_LICENSE:
                level = Message.Level.INFO;
                break;
        }

        if (level == null) {
            return null;
        }

        String details = "";
        switch (flyZoneState) {
            case CLEAR:
                details = context.getString(R.string.FlyZoneState_value_CLEAR);
                break;

            case UNKNOWN:
                details = context.getString(R.string.FlyZoneState_value_UNKNOWN);
                break;

            case NEAR_RESTRICTED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_NEAR_RESTRICTED_ZONE);
                break;

            case IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
                details = context.getString(R.string.FlyZoneState_value_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION);
                break;

            case IN_WARNING_ZONE:
                details = context.getString(R.string.FlyZoneState_value_IN_WARNING_ZONE);
                break;

            case SUSPECTED_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
                details = context.getString(R.string.FlyZoneState_value_SUSPECTED_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION);
                break;

            case PHONE_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION:
                details = context.getString(R.string.FlyZoneState_value_PHONE_IN_WARNING_ZONE_WITH_HEIGHT_LIMITATION);
                break;

            case IN_ENHANCE_WARNING_ZONE:
                details = context.getString(R.string.FlyZoneState_value_IN_ENHANCE_WARNING_ZONE);
                break;

            case SUSPECTED_IN_RESTRICTED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_SUSPECTED_IN_RESTRICTED_ZONE);
                break;

            case NEAR_HEIGHT_LIMITED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_NEAR_HEIGHT_LIMITED_ZONE);
                break;

            case IN_RESTRICTED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_IN_RESTRICTED_ZONE);
                break;

            case PHONE_IN_RESTRICTED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_PHONE_IN_RESTRICTED_ZONE);
                break;

            case IN_AUTHORIZED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_IN_AUTHORIZED_ZONE);
                break;

            case IN_AUTHORIZED_ZONE_WITH_LICENSE:
                details = context.getString(R.string.FlyZoneState_value_IN_AUTHORIZED_ZONE_WITH_LICENSE);
                break;

            case SUSPECTED_IN_AUTHORIZED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_SUSPECTED_IN_AUTHORIZED_ZONE);
                break;

            case NEAR_AUTHORIZED_ZONE_WITH_LICENSE:
                details = context.getString(R.string.FlyZoneState_value_NEAR_AUTHORIZED_ZONE_WITH_LICENSE);
                break;

            case NEAR_AUTHORIZED_ZONE:
                details = context.getString(R.string.FlyZoneState_value_NEAR_AUTHORIZED_ZONE);
                break;

            case NEAR_MULTIPLE_TYPE_FLY_ZONE:
                details = context.getString(R.string.FlyZoneState_value_NEAR_MULTIPLE_TYPE_FLY_ZONE);
                break;

            case OUTSIDE_SPECIAL_UNLOCK_ZONE:
                details = context.getString(R.string.FlyZoneState_value_OUTSIDE_SPECIAL_UNLOCK_ZONE);
                break;

            case FLY_TOUCH_AUTHORIZE_HAS_LICENSE:
                details = context.getString(R.string.FlyZoneState_value_FLY_TOUCH_AUTHORIZE_HAS_LICENSE);
                break;
        }

        return new Message(context.getString(R.string.FlyZoneState_title), details, level);
    }

    public static Message getMessage(final Context context, final AppActivationState appActivationState) {
        switch (appActivationState) {
            case ACTIVATED:
            case UNKNOWN:
                return null;

            case NOT_SUPPORTED:
                return new Message(context.getString(R.string.AppActivationState_title), context.getString(R.string.AppActivationState_value_NOT_SUPPORTED), Message.Level.ERROR);

            case LOGIN_REQUIRED:
                return new Message(context.getString(R.string.AppActivationState_title), context.getString(R.string.AppActivationState_value_LOGIN_REQUIRED), Message.Level.WARNING);
        }

        return null;
    }

    public static Message getMessage(final Context context, final GoHomeExecutionState goHomeExecutionState) {
        String details = null;

        switch (goHomeExecutionState) {
            case NOT_EXECUTING:
            case COMPLETED:
            case UNKNOWN:
                break;

            case TURN_DIRECTION_TO_HOME_POINT:
                details = context.getString(R.string.GoHomeExecutionState_value_TURN_DIRECTION_TO_HOME_POINT);
                break;

            case GO_UP_TO_HEIGHT:
                details = context.getString(R.string.GoHomeExecutionState_value_GO_UP_TO_HEIGHT);
                break;

            case AUTO_FLY_TO_HOME_POINT:
                details = context.getString(R.string.GoHomeExecutionState_value_AUTO_FLY_TO_HOME_POINT);
                break;

            case GO_DOWN_TO_GROUND:
                details = context.getString(R.string.GoHomeExecutionState_value_GO_DOWN_TO_GROUND);
                break;

            case BRAKING:
                details = context.getString(R.string.GoHomeExecutionState_value_BRAKING);
                break;

            case BYPASSING:
                details = context.getString(R.string.GoHomeExecutionState_value_BYPASSING);
                break;
        }

        if (details == null) {
            return null;
        }

        return new Message(context.getString(R.string.GoHomeExecutionState_title), details, Message.Level.WARNING);
    }

    public static Location getLocation(final FlightControllerState flightControllerState) {
        final LocationCoordinate3D aircraftLocation = flightControllerState.getAircraftLocation();
        if (aircraftLocation == null || flightControllerState.getSatelliteCount() == 0 || Double.isNaN(aircraftLocation.getLatitude()) || Double.isNaN(aircraftLocation.getLongitude())) {
            return null;
        }

        if (Math.abs(aircraftLocation.getLatitude()) < 0.000001 && Math.abs(aircraftLocation.getLongitude()) < 0.000001) {
            return null;
        }

        final Location location = new Location("");
        location.setLatitude(aircraftLocation.getLatitude());
        location.setLongitude(aircraftLocation.getLongitude());
        return location;
    }

    public static List<Message> getStatusMessages(final Context context, final FlightControllerState flightControllerState) {
        final List<Message> messages = new ArrayList<>();

        final Message message = getMessage(context, flightControllerState.getGoHomeExecutionState());
        if (message != null) {
            if (flightControllerState.getFlightMode() == FlightMode.CONFIRM_LANDING) {
                messages.add(new Message(flightControllerState.getFlightModeString(), Message.Level.WARNING));
            } else {
                messages.add(message);
            }
        } else {
            if (flightControllerState.isLowerThanSeriousBatteryWarningThreshold()) {
                messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_isLowerThanSeriousBatteryWarningThreshold_title), Message.Level.DANGER));
            } else if (flightControllerState.isLowerThanBatteryWarningThreshold()) {
                messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_isLowerThanBatteryWarningThreshold_title), Message.Level.WARNING));
            }

            if (flightControllerState.hasReachedMaxFlightRadius()) {
                messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_hasReachedMaxFlightRadius_title), Message.Level.WARNING));
            }

            if (flightControllerState.hasReachedMaxFlightHeight()) {
                messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_hasReachedMaxFlightHeight_title), Message.Level.WARNING));
            }

            switch (flightControllerState.getFlightMode()) {
                case ASSISTED_TAKEOFF:
                case AUTO_TAKEOFF:
                case AUTO_LANDING:
                case MOTORS_JUST_STARTED:
                case CONFIRM_LANDING:
                    messages.add(new Message(flightControllerState.getFlightModeString(), Message.Level.WARNING));
                    break;

                case GPS_WAYPOINT:
                    messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_flightMode_gpsWaypoint_title), Message.Level.WARNING));
                    break;

                case MANUAL:
                case ATTI:
                case ATTI_COURSE_LOCK:
                case ATTI_HOVER:
                case HOVER:
                case GPS_BLAKE:
                case GPS_ATTI:
                case GPS_COURSE_LOCK:
                case GPS_HOME_LOCK:
                case GPS_HOT_POINT:
                case ATTI_LANDING:
                case GO_HOME:
                case CLICK_GO:
                case JOYSTICK:
                case GPS_ATTI_WRISTBAND:
                case CINEMATIC:
                case ATTI_LIMITED:
                case DRAW:
                case GPS_FOLLOW_ME:
                case ACTIVE_TRACK:
                case TAP_FLY:
                case PANO:
                case FARMING:
                case FPV:
                case GPS_SPORT:
                case GPS_NOVICE:
                case TERRAIN_FOLLOW:
                case PALM_CONTROL:
                case QUICK_SHOT:
                case TRIPOD:
                case TRACK_SPOTLIGHT:
                case DETOUR:
                case TIME_LAPSE:
                case POI2:
                case OMNI_MOVING:
                case ADSB_AVOIDING:
                case SMART_TRACK:
                case MOTOR_STOP_LANDING:
                case UNKNOWN:
                    break;
            }
        }

        if (getLocation(flightControllerState) == null) {
            messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_locationUnavailable_title), Message.Level.WARNING));
        }

        if (!flightControllerState.isHomeLocationSet()) {
            messages.add(new Message(context.getString(R.string.DJIDronelink_FlightControllerState_statusMessages_homeLocationNotSet_title), Message.Level.WARNING));
        }

        return messages;
    }
}
