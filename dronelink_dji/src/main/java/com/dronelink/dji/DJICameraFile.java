//  DJICameraFile.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.location.Location;

import com.dronelink.core.CameraFile;
import com.dronelink.core.kernel.core.Orientation3;

import java.util.Date;

import dji.sdk.media.MediaFile;

public class DJICameraFile implements CameraFile {
    private final int channel;
    private final Date created;
    private final Location coordinate;
    private final Double altitude;
    private final Orientation3 orientation;
    private final MediaFile mediaFile;

    public DJICameraFile(final int channel, final MediaFile mediaFile, final Location coordinate, final Double altitude, final Orientation3 orientation) {
        this.channel = channel;
        this.mediaFile = mediaFile;
        this.created = new Date();
        this.coordinate = coordinate;
        this.altitude = altitude;
        this.orientation = orientation;
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public String getName() {
        return mediaFile.getFileName();
    }

    @Override
    public long getSize() {
        return mediaFile.getFileSize();
    }

    @Override
    public String getMetadata() {
        return mediaFile.getCustomInformation();
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Location getCoordinate() {
        return coordinate;
    }

    @Override
    public Double getAltitude() {
        return altitude;
    }

    @Override
    public Orientation3 getOrientation() {
        return orientation;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }
}
