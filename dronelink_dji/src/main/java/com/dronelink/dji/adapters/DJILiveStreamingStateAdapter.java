//  DJILiveStreamingStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 5/1/23.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import android.content.Context;
import android.util.Log;

import com.dronelink.core.DatedValue;
import com.dronelink.core.adapters.LiveStreamingStateAdapter;
import com.dronelink.core.kernel.core.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;

public class DJILiveStreamingStateAdapter implements LiveStreamingStateAdapter, LiveStreamManager.OnLiveErrorStatusListener {
    private int errorCode;
    private String errorMessage;

    public DJILiveStreamingStateAdapter() {
        final LiveStreamManager liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (liveStreamManager != null) {
            liveStreamManager.addLiveErrorStatusListener(this);
            liveStreamManager.registerListener(new LiveStreamManager.OnLiveChangeListener() {
                @Override
                public void onStatusChanged(int i) {
                    Log.d("FIXME", "LiveStreamManager.OnLiveChangeListener.onStatusChanged: " + i);
                }
            });
        }
    }

    public DatedValue<LiveStreamingStateAdapter> toDatedValue() {
        return new DatedValue<>(this, new Date());
    }

    @Override
    public boolean isEnabled() {
        final LiveStreamManager liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (liveStreamManager != null) {
            return liveStreamManager.isStreaming();
        }
        return false;
    }

    @Override
    public List<Message> getStatusMessages() {
        final List<Message> messages = new ArrayList<>();

        final String error = errorMessage;
        if (error != null && !error.isEmpty()) {
            messages.add(new Message(error, Message.Level.ERROR));
        }

        return messages;
    }

    @Override
    public void onError(int code, final String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }
}
