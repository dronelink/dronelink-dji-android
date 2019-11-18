//  DJIControlSession.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji;

import android.content.Context;
import android.util.Log;

import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneControlSession;
import com.dronelink.core.mission.core.Message;

import java.util.Date;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

public class DJIControlSession implements DroneControlSession {
    private static final String TAG = DJIControlSession.class.getCanonicalName();

    private enum State {
        TAKEOFF_START,
        TAKEOFF_ATTEMPTING,
        TAKEOFF_COMPLETE,
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

    public DJIControlSession(final Context context, final DJIDroneSession droneSession) {
        this.context = context;
        this.droneSession = droneSession;
    }

    public Message getDisengageReason() {
        if (attemptDisengageReason != null) {
            return attemptDisengageReason;
        }

        final DatedValue<FlightControllerState> flightControllerState = droneSession.getFlightControllerState();
        if (flightControllerState != null) {
            if (state == State.FLIGHT_MODE_JOYSTICK_COMPLETE && flightControllerState.value.getFlightMode() != FlightMode.JOYSTICK) {
                return new Message(context.getString(R.string.MissionDisengageReason_drone_control_override_title), flightControllerState.value.getFlightModeString());
            }
        }

        return null;
    }

    public boolean activate() {
        final FlightController flightController = droneSession.getAdapter().getDrone().getFlightController();
        if (flightController == null) {
            return false;
        }

        final DatedValue<FlightControllerState> flightControllerState = droneSession.getFlightControllerState();
        if (flightControllerState == null) {
            return false;
        }

        switch (state) {
            case TAKEOFF_START:
                if (flightControllerState.value.isFlying()) {
                    state = State.TAKEOFF_COMPLETE;
                    return activate();
                }

                state = State.TAKEOFF_ATTEMPTING;
                Log.i(TAG, "Attempting takeoff");
                flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            attemptDisengageReason = new Message(context.getString(R.string.MissionDisengageReason_take_off_failed_title), djiError.getDescription());
                            deactivate();
                            return;
                        }

                        Log.i(TAG, "Takeoff succeeded");
                        state = State.TAKEOFF_COMPLETE;
                    }
                });
                return false;

            case TAKEOFF_ATTEMPTING:
                return false;

            case TAKEOFF_COMPLETE:
                if (flightControllerState.value.isFlying() && flightControllerState.value.getFlightMode() != FlightMode.AUTO_TAKEOFF) {
                    state = State.VIRTUAL_STICK_START;
                    return activate();
                }
                return false;

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
                return false;

            case VIRTUAL_STICK_ATTEMPTING:
                return false;

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
                return false;

            case FLIGHT_MODE_JOYSTICK_COMPLETE:
                return true;

            case DEACTIVATED:
                return false;

            default:
                return false;
        }
    }

    public void deactivate() {
        droneSession.sendResetVelocityCommand(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                final FlightController flightController = droneSession.getAdapter().getDrone().getFlightController();
                if (flightController != null) {
                    flightController.setVirtualStickModeEnabled(false, null);
                }
            }
        });

        droneSession.sendResetGimbalCommands();
        droneSession.sendResetCameraCommands();

        state = State.DEACTIVATED;
    }
}
