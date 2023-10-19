//  DJIRemoteControllerStateAdapter.java
//  DronelinkDJI
//
//  Created by Jim McAndrew on 11/6/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.adapters;

import android.location.Location;

import com.dronelink.core.adapters.RemoteControllerStateAdapter;
import com.dronelink.core.kernel.core.RemoteControllerButton;
import com.dronelink.core.kernel.core.RemoteControllerStick;
import com.dronelink.core.kernel.core.RemoteControllerWheel;
import com.dronelink.dji.DronelinkDJI;

import dji.common.remotecontroller.GPSData;
import dji.common.remotecontroller.HardwareState;

public class DJIRemoteControllerStateAdapter implements RemoteControllerStateAdapter {
    public final HardwareState state;
    private final GPSData gpsData;
    private HardwareState.Button functionButton;
    private String droneModel;

    public DJIRemoteControllerStateAdapter(final HardwareState state, final GPSData gpsData, final HardwareState.Button functionButton, final String droneModel) {
        this.state = state;
        this.gpsData = gpsData;
        this.functionButton = functionButton;
        this.droneModel = droneModel;
    }

    @Override
    public Location getLocation() {
        return DronelinkDJI.getLocation(gpsData);
    }

    /* Method that ensures custom buttons are not returned if the function button is pressed on a remote controller that combines function, c1, and c2. See:
     https://developer.dji.com/api-reference/android-api/Components/RemoteController/DJIRemoteController_DJIRCHardwareState.html?search=c1&i=2&#djiremotecontroller_djirchardwarestate_custombutton1_inline
     for more information */
    private boolean cancelCustomButtons() {
        final RemoteControllerButton funcButton = getFunctionButton();
        return funcButton != null && funcButton.pressed && ("DJI Mini 2".equals(droneModel) || "Mavic Air 2".equals(droneModel) || "DJI Air 2S".equals(droneModel));
    }

    public RemoteControllerStick getLeftStick() {
        return state == null || state.getLeftStick() == null ? null : new RemoteControllerStick(state.getLeftStick().getHorizontalPosition() / 660.0, state.getLeftStick().getVerticalPosition() / 660.0);
    }

    public RemoteControllerWheel getLeftWheel() {
        return state == null ? null : new RemoteControllerWheel(true, false, (double)state.getLeftDial() / 660.0);
    }

    public RemoteControllerStick getRightStick() {
        return state == null || state.getRightStick() == null ? null : new RemoteControllerStick(state.getRightStick().getHorizontalPosition() / 660.0, state.getRightStick().getVerticalPosition() / 660.0);
    }

    @Override
    public RemoteControllerButton getCaptureButton() {
        return state == null || state.getShootPhotoAndRecordButton() == null ? null : new RemoteControllerButton(state.getShootPhotoAndRecordButton().isPresent(), state.getShootPhotoAndRecordButton().isClicked());
    }

    @Override
    public RemoteControllerButton getVideoButton() {
        return state == null || state.getRecordButton() == null ? null : new RemoteControllerButton(state.getRecordButton().isPresent(), state.getRecordButton().isClicked());
    }

    @Override
    public RemoteControllerButton getPhotoButton() {
        return state == null || state.getShutterButton() == null ? null : new RemoteControllerButton(state.getShutterButton().isPresent(), state.getShutterButton().isClicked());
    }

    public RemoteControllerButton getPauseButton() {
        return state == null || state.getPauseButton() == null ? null : new RemoteControllerButton(state.getPauseButton().isPresent(), state.getPauseButton().isClicked());
    }

    public RemoteControllerButton getReturnHomeButton() {
        return state == null || state.getGoHomeButton() == null ? null : new RemoteControllerButton(state.getGoHomeButton().isPresent(), state.getGoHomeButton().isClicked());
    }

    @Override
    public RemoteControllerButton getFunctionButton() {
        return functionButton == null ? null : new RemoteControllerButton(functionButton.isPresent(), functionButton.isClicked());
    }

    public RemoteControllerButton getC1Button() {
        return state == null || state.getC1Button() == null || cancelCustomButtons() ? null : new RemoteControllerButton(state.getC1Button().isPresent(), state.getC1Button().isClicked());
    }

    public RemoteControllerButton getC2Button() {
        return state == null || state.getC2Button() == null || cancelCustomButtons() ? null : new RemoteControllerButton(state.getC2Button().isPresent(), state.getC2Button().isClicked());
    }

    @Override
    public RemoteControllerButton getC3Button() {
        return state == null || state.getC3Button() == null ? null : new RemoteControllerButton(state.getC3Button().isPresent(), state.getC3Button().isClicked());
    }

    @Override
    public RemoteControllerButton getUpButton() {
        return state == null || state.getFiveDButton() == null ? null : new RemoteControllerButton(state.getFiveDButton().isPresent(), state.getFiveDButton().getVerticalDirection() == HardwareState.FiveDButtonDirection.POSITIVE);
    }

    @Override
    public RemoteControllerButton getDownButton() {
        return state == null || state.getFiveDButton() == null ? null : new RemoteControllerButton(state.getFiveDButton().isPresent(), state.getFiveDButton().getVerticalDirection() == HardwareState.FiveDButtonDirection.NEGATIVE);
    }

    @Override
    public RemoteControllerButton getLeftButton() {
        return state == null || state.getFiveDButton() == null ? null : new RemoteControllerButton(state.getFiveDButton().isPresent(), state.getFiveDButton().getHorizontalDirection() == HardwareState.FiveDButtonDirection.NEGATIVE);
    }

    @Override
    public RemoteControllerButton getRightButton() {
        return state == null || state.getFiveDButton() == null ? null : new RemoteControllerButton(state.getFiveDButton().isPresent(), state.getFiveDButton().getHorizontalDirection() == HardwareState.FiveDButtonDirection.POSITIVE);
    }

    @Override
    public RemoteControllerButton getL1Button() {
        return null;
    }

    @Override
    public RemoteControllerButton getL2Button() {
        return null;
    }

    @Override
    public RemoteControllerButton getL3Button() {
        return null;
    }

    @Override
    public RemoteControllerButton getR1Button() {
        return null;
    }

    @Override
    public RemoteControllerButton getR2Button() {
        return null;
    }

    @Override
    public RemoteControllerButton getR3Button() {
        return null;
    }
}