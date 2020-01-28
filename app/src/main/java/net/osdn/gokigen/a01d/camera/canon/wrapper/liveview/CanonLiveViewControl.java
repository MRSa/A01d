package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class CanonLiveViewControl implements ILiveViewControl, ILiveViewListener, IPtpIpCommunication
{
    private final Activity context;
    private final String ipAddr;
    private final int portNo;

    public CanonLiveViewControl(@NonNull Activity context, @NonNull String ipAddr, int portNo)
    {
        this.context = context;
        this.ipAddr = ipAddr;
        this.portNo = portNo;
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (this);
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public void startLiveView()
    {

    }

    @Override
    public void stopLiveView()
    {

    }

    @Override
    public void updateDigitalZoom()
    {

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {

    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return 0;
    }

    @Override
    public float getDigitalZoomScale()
    {
        return 0;
    }

    @Override
    public void setCameraLiveImageView(IImageDataReceiver target)
    {

    }

    @Override
    public boolean connect()
    {
        return false;
    }

    @Override
    public void disconnect()
    {

    }
}
