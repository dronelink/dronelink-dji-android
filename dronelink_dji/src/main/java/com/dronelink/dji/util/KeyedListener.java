package com.dronelink.dji.util;

import dji.common.error.DJIError;

public interface KeyedListener {
    <T> void onNext(T result);
}
