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
import dji.sdk.flightcontroller.FlightController;
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
        listeners.add(listener);
        final DroneSession session = this.session;
        if (session != null) {
            listener.onOpened(session);
        }
    }

    @Override
    public void removeListener(final Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void closeSession() {
        final DroneSession previousSession = session;
        if (previousSession != null) {
            previousSession.close();
            session = null;

            for (final Listener listener : listeners) {
                listener.onClosed(previousSession);
            }
        }
    }

    @Override
    public DroneSession getSession() {
        return session;
    }

    public void register(final Context context) {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            final DJIDroneSessionManager self = this;
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
                            closeSession();
                        }

                        @Override
                        public void onProductConnect(final BaseProduct baseProduct) {
                            if (baseProduct instanceof Aircraft) {
                                final Aircraft drone = (Aircraft) baseProduct;
                                if (session != null) {
                                    if (session.getAdapter().drone == drone) {
                                        return;
                                    }
                                    closeSession();
                                }

                                session = new DJIDroneSession(context, self, drone);
                                for (final Listener listener : listeners) {
                                    listener.onOpened(session);
                                }
                            }
                        }

                        @Override
                        public void onProductChanged(final BaseProduct baseProduct) {}

                        @Override
                        public void onComponentChange(final BaseProduct.ComponentKey componentKey, final BaseComponent oldComponent, final BaseComponent newComponent) {
                            if (newComponent != null) {
                                if (newComponent.isConnected()) {
                                    componentConnected(newComponent);
                                }
                                else {
                                    componentDisconnected(newComponent);
                                }

                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        if (isConnected) {
                                            componentConnected(newComponent);
                                        }
                                        else {
                                            componentDisconnected(newComponent);
                                        }
                                    }
                                });
                            }
                            else if (oldComponent != null) {
                                if (!oldComponent.isConnected()) {
                                    componentDisconnected(newComponent);
                                }
                            }
                        }

                        @Override
                        public void onDatabaseDownloadProgress(long current, long total) {}

                        public void componentConnected(final BaseComponent component) {
                            final DJIDroneSession sessionLocal = session;
                            if (component instanceof FlightController && sessionLocal == null) {
                                onProductConnect(DJISDKManager.getInstance().getProduct());
                                return;
                            }

                            if (sessionLocal != null) {
                                sessionLocal.componentConnected(component);
                            }
                        }

                        public void componentDisconnected(final BaseComponent component) {
                            if (component instanceof FlightController) {
                                onProductDisconnect();
                                return;
                            }

                            final DJIDroneSession sessionLocal = session;
                            if (sessionLocal != null) {
                                sessionLocal.componentDisconnected(component);
                            }
                        }
                    });
                }
            });
        }
    }
}
