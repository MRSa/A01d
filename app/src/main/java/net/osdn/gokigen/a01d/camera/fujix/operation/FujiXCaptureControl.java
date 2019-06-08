package net.osdn.gokigen.a01d.camera.fujix.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.CaptureCommand;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

public class FujiXCaptureControl implements ICaptureControl, IFujiXCommandCallback
{
    private final String TAG = this.toString();
    private final IFujiXCommandPublisher issuer;
    private final IAutoFocusFrameDisplay frameDisplay;


    public FujiXCaptureControl(@NonNull IFujiXCommandPublisher issuer, IAutoFocusFrameDisplay frameDisplay)
    {
        this.issuer = issuer;
        this.frameDisplay = frameDisplay;

    }

    @Override
    public void doCapture(int kind)
    {
        try
        {
            issuer.enqueueCommand(new CaptureCommand(this));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, "Response Received.");
        frameDisplay.hideFocusFrame();
    }
}
