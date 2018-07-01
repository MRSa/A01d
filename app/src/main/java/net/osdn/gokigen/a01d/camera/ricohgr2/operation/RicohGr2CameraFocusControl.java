package net.osdn.gokigen.a01d.camera.ricohgr2.operation;

import android.view.MotionEvent;

import net.osdn.gokigen.a01d.camera.IFocusingControl;

public class RicohGr2CameraFocusControl implements IFocusingControl
{

    @Override
    public boolean driveAutoFocus(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void unlockAutoFocus()
    {

    }
}
