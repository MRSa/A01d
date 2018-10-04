package net.osdn.gokigen.a01d.camera;

import android.view.MotionEvent;

/**
 *
 *
 */
public interface IFocusingControl
{
    boolean driveAutoFocus(MotionEvent motionEvent);
    void unlockAutoFocus();
    void halfPressShutter(boolean isPressed);
}
