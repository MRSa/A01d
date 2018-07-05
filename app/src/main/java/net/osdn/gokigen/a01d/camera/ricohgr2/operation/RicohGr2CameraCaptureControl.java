package net.osdn.gokigen.a01d.camera.ricohgr2.operation;

import android.support.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.takepicture.RicohGr2SingleShotControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

public class RicohGr2CameraCaptureControl implements ICaptureControl
{
    private final RicohGr2SingleShotControl singleShotControl;

    public RicohGr2CameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer)
    {
        singleShotControl = new RicohGr2SingleShotControl(frameDisplayer);
    }

    @Override
    public void doCapture(int kind)
    {
        try
        {
            singleShotControl.singleShot();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
