package net.osdn.gokigen.a01d.camera.ricohgr2.operation;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.takepicture.RicohGr2SingleShotControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.IUsePentaxCommand;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

import androidx.annotation.NonNull;

public class RicohGr2CameraCaptureControl implements ICaptureControl
{
    private final RicohGr2SingleShotControl singleShotControl;

    public RicohGr2CameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IUsePentaxCommand usePentaxCommand)
    {
        singleShotControl = new RicohGr2SingleShotControl(frameDisplayer, usePentaxCommand);
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
