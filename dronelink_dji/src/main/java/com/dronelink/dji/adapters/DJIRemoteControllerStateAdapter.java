//  DJIGimbalStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import static com.dronelink.dji.DronelinkDJI.getRemoteControllerPairingState;

import com.dronelink.core.kernel.core.RemoteControllerButton;
import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.kernel.core.RemoteControllerStick;
import com.dronelink.core.kernel.core.RemoteControllerWheel;
import com.dronelink.core.kernel.core.enums.RemoteControllerPairingState;

import dji.common.remotecontroller.HardwareState;
import dji.common.remotecontroller.PairingState;

public class DJIRemoteControllerStateAdapter implements RemoteControllerStateAdapter {
    public final HardwareState hardwareState;
    public final PairingState pairingState;

    public DJIRemoteControllerStateAdapter(final HardwareState hardwareState, final PairingState pairingState) {
        this.hardwareState = hardwareState;
        this.pairingState = pairingState;
    }

    public RemoteControllerStick getLeftStick() {
        return hardwareState == null ? null : new RemoteControllerStick(hardwareState.getLeftStick().getHorizontalPosition() / 660.0, hardwareState.getLeftStick().getVerticalPosition() / 660.0);
    }

    public RemoteControllerWheel getLeftWheel() {
        return hardwareState == null ? null : new RemoteControllerWheel(true, true, (double) hardwareState.getLeftDial() / 660.0);
    }

    public RemoteControllerStick getRightStick() {
        return hardwareState == null ? null : new RemoteControllerStick(hardwareState.getRightStick().getHorizontalPosition() / 660.0, hardwareState.getRightStick().getVerticalPosition() / 660.0);
    }

    public RemoteControllerButton getPauseButton() {
        return hardwareState == null ? null : new RemoteControllerButton(hardwareState.getPauseButton().isPresent(), hardwareState.getPauseButton().isClicked());
    }

    public RemoteControllerButton getReturnHomeButton() {
        return hardwareState == null ? null : new RemoteControllerButton(hardwareState.getGoHomeButton().isPresent(), hardwareState.getGoHomeButton().isClicked());
    }

    public RemoteControllerButton getC1Button() {
        return hardwareState == null ? null : new RemoteControllerButton(hardwareState.getC1Button().isPresent(), hardwareState.getC1Button().isClicked());
    }

    public RemoteControllerButton getC2Button() {
        return hardwareState == null ? null : new RemoteControllerButton(hardwareState.getC2Button().isPresent(), hardwareState.getC2Button().isClicked());
    }

    public RemoteControllerPairingState pairing() {
        return getRemoteControllerPairingState(pairingState);
    }
}