//  DJIVirtualStickSession.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneControlSession;
import com.dronelink.core.kernel.core.Message;
import com.dronelink.core.kernel.core.enums.ExecutionEngine;

import java.util.Date;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.remotecontroller.SoftSwitchJoyStickMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.remotecontroller.RemoteController;

public class DJIVirtualStickSession implements DroneControlSession {
    private static final String TAG = DJIVirtualStickSession.class.getCanonicalName();

    private enum State {
        TAKEOFF_START,
        TAKEOFF_ATTEMPTING,
        TAKEOFF_COMPLETE,
        SOFT_SWITCH_JOYSTICK_MODE_START,
        SOFT_SWITCH_JOYSTICK_MODE_ATTEMPTING,
        VIRTUAL_STICK_START,
        VIRTUAL_STICK_ATTEMPTING,
        FLIGHT_MODE_JOYSTICK_ATTEMPTING,
        FLIGHT_MODE_JOYSTICK_COMPLETE,
        DEACTIVATED
    }

    private final Context context;
    private final DJIDroneSession droneSession;

    private State state = State.TAKEOFF_START;
    private int virtualStickAttempts = 0;
    private Date virtualStickAttemptPrevious = null;
    private Date flightModeJoystickAttemptingStarted = null;
    private Message attemptDisengageReason = null;

    public DJIVirtualStickSession(final Context context, final DJIDroneSession droneSession) {
        this.context = context;
        this.droneSession = droneSession;
    }

    @Override
    public ExecutionEngine getExecutionEngine() {
        return ExecutionEngine.DRONELINK_KERNEL;
    }

    public Message getDisengageReason() {
        if (attemptDisengageReason != null) {
            return attemptDisengageReason;
        }

        final DatedValue<FlightControllerState> flightControllerState = droneSession.getFlightControllerState();
        if (flightControllerState != null) {
            if (state == State.FLIGHT_MODE_JOYSTICK_COMPLETE) {
                if (flightControllerState.value.getFlightMode() != FlightMode.JOYSTICK) {
                    return new Message(context.getString(R.string.MissionDisengageReason_drone_control_override_title), context.getString(R.string.MissionDisengageReason_drone_control_override_details));
                }
            }
        }

        return null;
    }

    @Override
    public boolean isReengaging() {
        return false;
    }

    public Boolean activate() {
        final FlightController flightController = droneSession.getAdapter().getDrone().getFlightController();
        if (flightController == null) {
            deactivate();
            return false;
        }

        final DatedValue<FlightControllerState> flightControllerState = droneSession.getFlightControllerState();
        if (flightControllerState == null) {
            deactivate();
            return false;
        }

        switch (state) {
            case TAKEOFF_START:
                if (flightControllerState.value.isFlying()) {
                    state = State.TAKEOFF_COMPLETE;
                    return activate();
                }

                state = State.TAKEOFF_ATTEMPTING;

                Log.i(TAG, "Attempting precision takeoff");
                flightController.startPrecisionTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (djiError != null) {
                            Log.e(TAG, "Precision takeoff failed: " + djiError.getDescription());
                            Log.i(TAG, "Attempting takeoff");
                            flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(final DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e(TAG, "Takeoff failed: " + djiError.getDescription());
                                        attemptDisengageReason = new Message(context.getString(R.string.MissionDisengageReason_take_off_failed_title), djiError.getDescription());
                                        deactivate();
                                        return;
                                    }

                                    Log.i(TAG, "Takeoff succeeded");
                                    state = State.TAKEOFF_COMPLETE;
                                }
                            });
                            return;
                        }

                        Log.i(TAG, "Precision takeoff succeeded");
                        state = State.TAKEOFF_COMPLETE;
                    }
                });

                return null;

            case TAKEOFF_ATTEMPTING:
                return null;

            case TAKEOFF_COMPLETE:
                if (flightControllerState.value.isFlying() && flightControllerState.value.getFlightMode() != FlightMode.AUTO_TAKEOFF) {
                    state = State.SOFT_SWITCH_JOYSTICK_MODE_START;
                    return activate();
                }
                return null;

            case SOFT_SWITCH_JOYSTICK_MODE_START:
                final RemoteController remoteController = droneSession.getAdapter().getDrone().getRemoteController();
                if (remoteController == null) {
                    state = State.VIRTUAL_STICK_START;
                    return activate();
                }

                Log.i(TAG, "Verifying soft switch joystick mode");
                remoteController.getSoftSwitchJoyStickMode(new CommonCallbacks.CompletionCallbackWith<SoftSwitchJoyStickMode>() {
                    @Override
                    public void onSuccess(final SoftSwitchJoyStickMode softSwitchJoyStickMode) {
                        if (softSwitchJoyStickMode == SoftSwitchJoyStickMode.POSITION) {
                            state = State.VIRTUAL_STICK_START;
                            return;
                        }

                        Log.i(TAG, "Changing soft switch joystick mode to P");
                        remoteController.setSoftSwitchJoyStickMode(SoftSwitchJoyStickMode.POSITION, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(final DJIError djiError) {
                                //if try to activate virtual stick immediately it can fail, so delay
                                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        state = State.VIRTUAL_STICK_START;
                                        return;
                                    }
                                }, 1000);
                            }
                        });
                    }

                    @Override
                    public void onFailure(final DJIError djiError) {
                        state = State.VIRTUAL_STICK_START;
                    }
                });
                return null;

            case SOFT_SWITCH_JOYSTICK_MODE_ATTEMPTING:
                return null;

            case VIRTUAL_STICK_START:
                if (virtualStickAttemptPrevious == null || (System.currentTimeMillis() - virtualStickAttemptPrevious.getTime()) > 2000) {
                    state = State.VIRTUAL_STICK_ATTEMPTING;
                    virtualStickAttemptPrevious = new Date();
                    virtualStickAttempts += 1;

                    Log.i(TAG, String.format("Attempting virtual stick mode control: %d", virtualStickAttempts));
                    flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                if (virtualStickAttempts >= 5) {
                                    attemptDisengageReason = new Message(context.getString(R.string.MissionDisengageReason_take_control_failed_title), djiError.getDescription());
                                    deactivate();
                                }
                                else {
                                    state = State.VIRTUAL_STICK_START;
                                }
                                return;
                            }

                            Log.i(TAG, "Virtual stick mode control enabled");
                            flightModeJoystickAttemptingStarted = new Date();
                            state = State.FLIGHT_MODE_JOYSTICK_ATTEMPTING;
                        }
                    });
                }
                return null;

            case VIRTUAL_STICK_ATTEMPTING:
                return null;

            case FLIGHT_MODE_JOYSTICK_ATTEMPTING:
                if (flightControllerState.value.getFlightMode() == FlightMode.JOYSTICK) {
                    Log.i(TAG, "Flight mode joystick achieved");
                    state = State.FLIGHT_MODE_JOYSTICK_COMPLETE;
                    return activate();
                }

                if (flightModeJoystickAttemptingStarted != null) {
                    if ((System.currentTimeMillis() - flightModeJoystickAttemptingStarted.getTime()) > 2000) {
                        attemptDisengageReason = new Message(context.getString(R.string.MissionDisengageReason_take_control_failed_title));
                        deactivate();
                        return false;
                    }
                }

                droneSession.sendResetVelocityCommand(null);
                return null;

            case FLIGHT_MODE_JOYSTICK_COMPLETE:
                return true;

            case DEACTIVATED:
                return false;
        }

        return false;
    }

    public void deactivate() {
        droneSession.sendResetVelocityCommand(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError djiError) {}
        });
        final FlightController flightController = droneSession.getAdapter().getDrone().getFlightController();
        if (flightController != null) {
            flightController.setVirtualStickModeEnabled(false, null);
        }
        state = State.DEACTIVATED;
    }
}
