package com.dronelink.dji.util;

import android.util.Log;

import androidx.annotation.Nullable;

import dji.common.error.DJIError;
import dji.keysdk.DJIKey;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.sdkmanager.DJISDKManager;

public class KeyedListenersUtil {

    public static KeyListener addKeyedListener(final DJIKey key, final KeyedListener result) {
        KeyListener listener = new KeyListener() {
            @Override
            public void onValueChange(@Nullable Object o, @Nullable final Object newValue) {
                result.onNext(newValue);
            }
        };
        DJISDKManager.getInstance().getKeyManager().addListener(key, listener);
        DJISDKManager.getInstance().getKeyManager().getValue(key, new GetCallback() {
            @Override
            public void onSuccess(final Object newValue) {
                result.onNext(newValue);
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.e("KeyedListener", "Error retrieving key: " + key.toString());
            }
        });

        return listener;
    }
}
