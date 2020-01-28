package net.osdn.gokigen.a01d.camera.canon.operation;

import android.app.Activity;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class CanonFocusingControl implements IFocusingControl
{
    private final Activity context;
    private final PtpIpCommandPublisher commandPublisher;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IIndicatorControl indicator;

    public CanonFocusingControl(@NonNull Activity context, @NonNull PtpIpCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        this.context = context;
        this.commandPublisher = commandPublisher;
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }


    @Override
    public boolean driveAutoFocus(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void unlockAutoFocus() {

    }

    @Override
    public void halfPressShutter(boolean isPressed) {

    }
}
