//  DJIGimbalStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.kernel.core.RemoteControllerButton;
import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.kernel.core.RemoteControllerStick;
import com.dronelink.core.kernel.core.RemoteControllerWheel;

import dji.common.remotecontroller.HardwareState;

public class DJIRemoteControllerStateAdapter implements RemoteControllerStateAdapter {
    public final HardwareState state;

    public DJIRemoteControllerStateAdapter(final HardwareState state) {
        this.state = state;
    }

    public RemoteControllerStick getLeftStick() {
        return state == null ? null : new RemoteControllerStick(state.getLeftStick().getHorizontalPosition() / 660.0, state.getLeftStick().getVerticalPosition() / 660.0);
    }

    public RemoteControllerWheel getLeftWheel() {
        return state == null ? null : new RemoteControllerWheel(true, true, (double) state.getLeftDial() / 660.0);
    }

    public RemoteControllerStick getRightStick() {
        return state == null ? null : new RemoteControllerStick(state.getRightStick().getHorizontalPosition() / 660.0, state.getRightStick().getVerticalPosition() / 660.0);
    }

    public RemoteControllerButton getPauseButton() {
        return state == null ? null : new RemoteControllerButton(state.getPauseButton().isPresent(), state.getPauseButton().isClicked());
    }

    public RemoteControllerButton getReturnHomeButton() {
        return state == null ? null : new RemoteControllerButton(state.getGoHomeButton().isPresent(), state.getGoHomeButton().isClicked());
    }

    public RemoteControllerButton getC1Button() {
        return state == null ? null : new RemoteControllerButton(state.getC1Button().isPresent(), state.getC1Button().isClicked());
    }

    public RemoteControllerButton getC2Button() {
        return state == null ? null : new RemoteControllerButton(state.getC2Button().isPresent(), state.getC2Button().isClicked());
    }
}