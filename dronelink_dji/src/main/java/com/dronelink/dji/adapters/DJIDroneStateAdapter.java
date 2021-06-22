//  DJIDroneStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import android.location.Location;

import com.dronelink.core.Convert;
import com.dronelink.core.DatedValue;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.kernel.core.Orientation3;

import java.util.Date;
import java.util.UUID;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.ObstacleDetectionSector;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.adsb.AirSenseSystemInformation;
import dji.common.model.LocationCoordinate2D;

public class DJIDroneStateAdapter implements DroneStateAdapter {
    public DatedValue<FlightControllerState> flightControllerState;
    public DatedValue<BatteryState> batteryState;
    public DatedValue<VisionDetectionState> visionDetectionState;
    public DatedValue<Integer> airLinkSignalQuality;
    public DatedValue<Integer> lowBatteryWarningThreshold;
    public String id = UUID.randomUUID().toString();
    public String serialNumber;
    public String name;
    public String model;
    public String firmwarePackageVersion;
    public boolean initialized = false;
    public boolean located = false;
    public Location lastKnownGroundLocation;

    public DatedValue<DroneStateAdapter> toDatedValue() {
        return new DatedValue<DroneStateAdapter>(this, flightControllerState == null ? new Date() : flightControllerState.date);
    }

    @Override
    public boolean isFlying() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return flightControllerState != null && flightControllerState.value.isFlying();
    }

    @Override
    public Location getLocation() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        if (flightControllerState == null) {
            return null;
        }

        final LocationCoordinate3D aircraftLocation = flightControllerState.value.getAircraftLocation();
        if (aircraftLocation == null || flightControllerState.value.getSatelliteCount() == 0 || Double.isNaN(aircraftLocation.getLatitude()) || Double.isNaN(aircraftLocation.getLongitude())) {
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

    @Override
    public Location getHomeLocation() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        if (flightControllerState == null || !flightControllerState.value.isHomeLocationSet()) {
            return null;
        }

        final LocationCoordinate2D homeLocation = flightControllerState.value.getHomeLocation();
        if (homeLocation == null) {
            return null;
        }

        final Location location = new Location("");
        location.setLatitude(homeLocation.getLatitude());
        location.setLongitude(homeLocation.getLongitude());
        return location;
    }

    @Override
    public Location getLastKnownGroundLocation() {
        return lastKnownGroundLocation;
    }

    @Override
    public Location getTakeoffLocation() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        if (flightControllerState != null && flightControllerState.value.isFlying()) {
            if (lastKnownGroundLocation != null) {
                return lastKnownGroundLocation;
            }

            if (flightControllerState.value.isHomeLocationSet()) {
                return getHomeLocation();
            }
        }

        return getLocation();
    }

    @Override
    public Double getTakeoffAltitude() {
        //DJI reports "MSL" altitude based on barometer...no good
        //if (getTakeoffLocation() != null) {
        //    final float altitude = flightControllerState.value.getTakeoffLocationAltitude();
        //    return altitude == 0 ? null : new Double(flightControllerState.value.getTakeoffLocationAltitude());
        //}

        return null;
    }

    @Override
    public double getCourse() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return flightControllerState == null ? 0 : Math.atan2(flightControllerState.value.getVelocityY(), flightControllerState.value.getVelocityX());
    }

    @Override
    public double getHorizontalSpeed() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return flightControllerState == null ? 0 : Math.sqrt(Math.pow(flightControllerState.value.getVelocityX(), 2) + Math.pow(flightControllerState.value.getVelocityY(), 2));
    }

    @Override
    public double getVerticalSpeed() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return flightControllerState == null ? 0 : flightControllerState.value.getVelocityZ() == 0 ? 0 : -flightControllerState.value.getVelocityZ();
    }

    @Override
    public double getAltitude() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        if (flightControllerState == null) {
            return 0;
        }

        final LocationCoordinate3D location = flightControllerState.value.getAircraftLocation();
        if (location == null) {
            return 0;
        }

        return location.getAltitude();
    }


    @Override
    public Double getUltrasonicAltitude() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        if (flightControllerState == null) {
            return null;
        }

        if (flightControllerState.value.isUltrasonicBeingUsed()) {
            return (double)flightControllerState.value.getUltrasonicHeightInMeters();
        }

        return null;
    }

    @Override
    public Double getBatteryPercent() {
        final DatedValue<BatteryState> batteryState = this.batteryState;
        if (batteryState == null) {
            return null;
        }

        return (double)batteryState.value.getChargeRemainingInPercent() / 100.0;
    }

    @Override
    public Double getLowBatteryThreshold() {
        if (lowBatteryWarningThreshold == null) {
            return null;
        }
        return ((double)lowBatteryWarningThreshold.value / 100);
    }

    @Override
    public Double getObstacleDistance() {
        final DatedValue<VisionDetectionState> visionDetectionState = this.visionDetectionState;
        if (visionDetectionState == null) {
            return null;
        }

        double minObstacleDistance = 0.0;
        final ObstacleDetectionSector[] detectionSectors = visionDetectionState.value.getDetectionSectors();
        if (detectionSectors != null) {
            for (final ObstacleDetectionSector detectionSector : detectionSectors) {
                minObstacleDistance = minObstacleDistance == 0 ? detectionSector.getObstacleDistanceInMeters() : Math.min(minObstacleDistance, detectionSector.getObstacleDistanceInMeters());
            }
        }

        if (minObstacleDistance == 0) {
            return null;
        }

        return minObstacleDistance;
    }

    @Override
    public Orientation3 getOrientation() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        final Orientation3 orientation = new Orientation3();
        if (flightControllerState != null) {
            final Attitude attitude = flightControllerState.value.getAttitude();
            orientation.x = Convert.DegreesToRadians(attitude.pitch);
            orientation.y = Convert.DegreesToRadians(attitude.roll);
            orientation.z = Convert.DegreesToRadians(attitude.yaw);
        }
        return orientation;
    }

    @Override
    public Integer getGPSSatellites() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return flightControllerState == null ? null : flightControllerState.value.getSatelliteCount();
    }

    @Override
    public Double getSignalStrength() {
        final DatedValue<FlightControllerState> flightControllerState = this.flightControllerState;
        return airLinkSignalQuality == null ? null : airLinkSignalQuality.value.doubleValue();
    }
}
