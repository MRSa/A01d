package net.osdn.gokigen.a01d.camera.nikon.operation;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

public class NikonCaptureControl implements ICaptureControl
{
    private final PtpIpCommandPublisher commandPublisher;
    private final IAutoFocusFrameDisplay frameDisplayer;

    public NikonCaptureControl(@NonNull PtpIpCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
    {
        this.commandPublisher = commandPublisher;
        this.frameDisplayer = frameDisplayer;

    }


    @Override
    public void doCapture(int kind)
    {

    }
}