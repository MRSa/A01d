package net.osdn.gokigen.a01d.camera.olympus.operation;

import android.view.MotionEvent;

/**
 *
 *
 */
public interface IFocusingControl
{
    boolean driveAutoFocus(MotionEvent motionEvent);
    void unlockAutoFocus();
}
