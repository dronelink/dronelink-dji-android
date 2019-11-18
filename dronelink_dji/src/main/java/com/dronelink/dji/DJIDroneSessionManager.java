//  DJIControlSession.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

public class DJIDroneSessionManager implements DroneSessionManager {
    private static final String TAG = DJIDroneSessionManager.class.getCanonicalName();

    private final Context context;
    private DJIDroneSession session;
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private final List<Listener> listeners = new LinkedList<>();

    public DJIDroneSessionManager(final Context context) {
        this.context = context;
    }

    @Override
    public void addListener(final Listener listener) {
        this.listeners.add(listener);
        final DroneSession session = this.session;
        if (session != null) {
            listener.onOpened(session);
        }
    }

    @Override
    public void removeListener(final Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public DroneSession getSession() {
        return session;
    }

    public void register(final Context context) {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(context, new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                Log.i(TAG, "SDK Registered successfully");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                Log.e(TAG, "SDK Registered with error: " + djiError.getDescription());
                            }
                        }

                        @Override
                        public void onProductDisconnect() {
                            if (session != null) {
                                final DroneSession previousSession = session;
                                previousSession.close();
                                session = null;

                                for (final Listener listener : listeners) {
                                    listener.onClosed(previousSession);
                                }
                            }
                        }

                        @Override
                        public void onProductConnect(final BaseProduct baseProduct) {
                            if (baseProduct instanceof Aircraft) {
                                session = new DJIDroneSession(context, (Aircraft) baseProduct);
                                for (final Listener listener : listeners) {
                                    listener.onOpened(session);
                                }
                            }
                        }

                        @Override
                        public void onComponentChange(final BaseProduct.ComponentKey componentKey, final BaseComponent oldComponent, final BaseComponent newComponent) {
                            if (newComponent != null) {
                                final BaseComponent component = newComponent;
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        if (session != null) {
                                            if (isConnected) {
                                                session.componentConnected(component);
                                            } else {
                                                session.componentDisconnected(component);
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onDatabaseDownloadProgress(long current, long total) {

                        }
                    });
                }
            });
        }
    }
}
