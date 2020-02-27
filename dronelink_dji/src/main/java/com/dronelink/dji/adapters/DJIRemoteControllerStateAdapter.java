//  DJIGimbalStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import com.dronelink.core.adapters.RemoteControllerButtonState;
import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.adapters.RemoteControllerStickState;

import dji.common.remotecontroller.HardwareState;

public class DJIRemoteControllerStateAdapter implements RemoteControllerStateAdapter {
    public final HardwareState state;

    public DJIRemoteControllerStateAdapter(final HardwareState state) {
        this.state = state;
    }

    public RemoteControllerStickState getLeftStickState() {
        return state == null ? null : new RemoteControllerStickState(state.getLeftStick().getHorizontalPosition() / 660, state.getLeftStick().getVerticalPosition() / 660);
    }

    public RemoteControllerStickState getRightStickState() {
        return state == null ? null : new RemoteControllerStickState(state.getRightStick().getHorizontalPosition() / 660, state.getRightStick().getVerticalPosition() / 660);
    }

    public RemoteControllerButtonState getPauseButtonState() {
        return state == null ? null : new RemoteControllerButtonState(state.getPauseButton().isPresent(), state.getPauseButton().isClicked());
    }

    public RemoteControllerButtonState getC1ButtonState() {
        return state == null ? null : new RemoteControllerButtonState(state.getC1Button().isPresent(), state.getC1Button().isClicked());
    }

    public RemoteControllerButtonState getC2ButtonState() {
        return state == null ? null : new RemoteControllerButtonState(state.getC2Button().isPresent(), state.getC2Button().isClicked());
    }
}