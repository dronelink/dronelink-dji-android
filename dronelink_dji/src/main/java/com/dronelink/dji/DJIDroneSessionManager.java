//  DJIDroneSessionManager.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.command.Command;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.kernel.core.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.flyzone.FlyZoneState;
import dji.common.realname.AppActivationState;
import dji.common.remotecontroller.PairingState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

public class DJIDroneSessionManager implements DroneSessionManager {
    private static final String TAG = DJIDroneSessionManager.class.getCanonicalName();

    private final Context context;
    private DatedValue<FlyZoneState> flyZoneState;
    private DatedValue<AppActivationState> appActivationState;
    private DJIDroneSession session;
    private final AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private final List<Listener> listeners = new LinkedList<>();

    public DJIDroneSessionManager(final Context context) {
        this.context = context;

        initFlyZoneManagerCallback(0);
        initAppActivationManagerStateListener(0);
    }

    private void initFlyZoneManagerCallback(final int attempt) {
        if (attempt < 10) {
            if (DJISDKManager.getInstance().getFlyZoneManager() == null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initFlyZoneManagerCallback(attempt + 1);
                    }
                }, attempt * 1000);
                return;
            }

            DJISDKManager.getInstance().getFlyZoneManager().setFlyZoneStateCallback(new FlyZoneState.Callback() {
                @Override
                public void onUpdate(@NonNull final FlyZoneState state) {
                    flyZoneState = new DatedValue<>(state);
                }
            });
        }
        else {
            Log.e(TAG, "Unable to initialize DJI FlyZoneManager callback");
        }
    }

    private void initAppActivationManagerStateListener(final int attempt) {
        if (attempt < 10) {
            if (DJISDKManager.getInstance().getAppActivationManager() == null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initAppActivationManagerStateListener(attempt + 1);
                    }
                }, attempt * 1000);
                return;
            }

            DJISDKManager.getInstance().getAppActivationManager().addAppActivationStateListener(new AppActivationState.AppActivationStateListener() {
                @Override
                public void onUpdate(final AppActivationState state) {
                    appActivationState = new DatedValue<>(state);
                }
            });
        }
        else {
            Log.e(TAG, "Unable to initialize DJI AppActivationManagerState listener");
        }
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
    public void startRemoteControllerLinking(final Command.Finisher finisher) {
        final Aircraft aircraft = ((Aircraft) DJISDKManager.getInstance().getProduct());
        if (aircraft == null) {
            if (finisher != null) {
                finisher.execute(new CommandError(context.getString(R.string.DJIDroneSessionManager_remoteControllerLinking_unavailable)));
            }
            return;
        }

        final RemoteController remoteController = aircraft.getRemoteController();
        if (remoteController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError(context.getString(R.string.DJIDroneSessionManager_remoteControllerLinking_unavailable)));
            }
            return;
        }

        remoteController.startPairing(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public void stopRemoteControllerLinking(final Command.Finisher finisher) {
        final Aircraft aircraft = ((Aircraft) DJISDKManager.getInstance().getProduct());
        if (aircraft == null) {
            if (finisher != null) {
                finisher.execute(new CommandError(context.getString(R.string.DJIDroneSessionManager_remoteControllerLinking_unavailable)));
            }
            return;
        }

        final RemoteController remoteController = aircraft.getRemoteController();
        if (remoteController == null) {
            if (finisher != null) {
                finisher.execute(new CommandError(context.getString(R.string.DJIDroneSessionManager_remoteControllerLinking_unavailable)));
            }
            return;
        }

        remoteController.stopPairing(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {
                if (finisher != null) {
                    finisher.execute(djiError == null ? null : new CommandError(djiError.getDescription(), djiError.getErrorCode()));
                }
            }
        });
    }

    @Override
    public DroneSession getSession() {
        return session;
    }

    @Override
    public List<Message> getStatusMessages() {
        final List<Message> messages = new ArrayList<>();

        final DatedValue<FlyZoneState> flyZoneState = this.flyZoneState;
        if (flyZoneState != null && flyZoneState.value != null) {
            final Message message = DronelinkDJI.getMessage(context, flyZoneState.value);
            if (message != null) {
                messages.add(message);
            }
        }

        final DatedValue<AppActivationState> appActivationState = this.appActivationState;
        if (appActivationState != null && appActivationState.value != null) {
            final Message message = DronelinkDJI.getMessage(context, appActivationState.value);
            if (message != null) {
                messages.add(message);
            }
        }

        return messages;
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
                                Log.i(TAG, "DJI SDK registered successfully");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                Log.e(TAG, "DJI SDK registered with error: " + djiError.getDescription());
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
