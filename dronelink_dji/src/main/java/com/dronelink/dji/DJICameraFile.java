//  DJICameraFile.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import com.dronelink.core.CameraFile;

import java.util.Date;

import dji.sdk.media.MediaFile;

public class DJICameraFile implements CameraFile {
    private final int channel;
    private MediaFile mediaFile;
    private Date created;

    public DJICameraFile(final int channel, final MediaFile mediaFile) {
        this.channel = channel;
        this.mediaFile = mediaFile;
        this.created = new Date();
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
    public Date getCreated() {
        return created;
    }
}
