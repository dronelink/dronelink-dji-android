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
import com.dronelink.core.mission.core.Orientation3;

import java.util.UUID;

import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;

public class DJIDroneStateAdapter implements DroneStateAdapter {
    public DatedValue<FlightControllerState> flightControllerState;
    public String id = UUID.randomUUID().toString();
    public String serialNumber;
    public String name;
    public String model;
    public String firmwarePackageVersion;
    public boolean initialized = false;
    public boolean located = false;
    public Location lastKnownGroundLocation;

    @Override
    public boolean isFlying() {
        return flightControllerState != null && flightControllerState.value.isFlying();
    }

    @Override
    public Location getLocation() {
        if (flightControllerState == null) {
            return null;
        }

        final LocationCoordinate3D aircraftLocation = flightControllerState.value.getAircraftLocation();
        if (aircraftLocation == null || !flightControllerState.value.isHomeLocationSet() || Double.isNaN(aircraftLocation.getLatitude()) || Double.isNaN(aircraftLocation.getLongitude())) {
            return null;
        }

        if (aircraftLocation.getLatitude() == 0 && aircraftLocation.getLongitude() == 0) {
            return null;
        }

        final Location location = new Location("");
        location.setLatitude(aircraftLocation.getLatitude());
        location.setLongitude(aircraftLocation.getLongitude());
        return location;
    }

    @Override
    public Location getHomeLocation() {
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
        return flightControllerState == null ? 0 : Math.atan2(flightControllerState.value.getVelocityY(), flightControllerState.value.getVelocityX());
    }

    @Override
    public double getHorizontalSpeed() {
        return flightControllerState == null ? 0 : Math.sqrt(Math.pow(flightControllerState.value.getVelocityX(), 2) + Math.pow(flightControllerState.value.getVelocityY(), 2));
    }

    @Override
    public double getVerticalSpeed() {
        return flightControllerState == null ? 0 : flightControllerState.value.getVelocityZ() == 0 ? 0 : -flightControllerState.value.getVelocityZ();
    }

    @Override
    public double getAltitude() {
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
    public Orientation3 getMissionOrientation() {
        final Orientation3 orientation = new Orientation3();
        if (flightControllerState != null) {
            final Attitude attitude = flightControllerState.value.getAttitude();
            orientation.x = Convert.DegreesToRadians(attitude.pitch);
            orientation.y = Convert.DegreesToRadians(attitude.roll);
            orientation.z = Convert.DegreesToRadians(attitude.yaw);
        }
        return orientation;
    }
}
