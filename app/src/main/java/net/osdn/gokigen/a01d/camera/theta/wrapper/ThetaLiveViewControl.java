package net.osdn.gokigen.a01d.camera.theta.wrapper;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.CameraStatusListener;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.theta.wrapper.status.ThetaCameraStatusWatcher;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;


public class ThetaLiveViewControl implements ILiveViewControl, ILiveViewListener
{
    ThetaLiveViewControl(@NonNull ThetaCameraStatusWatcher statusHolder, @NonNull CameraStatusListener statusListener)
    {

    }

    ILiveViewListener getLiveViewListener()
    {
        return (this);
    }

    @Override
    public void changeLiveViewSize(String size) {

    }

    @Override
    public void startLiveView() {

    }

    @Override
    public void stopLiveView() {

    }

    @Override
    public void updateDigitalZoom() {

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale) {

    }

    @Override
    public float getMagnifyingLiveViewScale() {
        return 0;
    }

    @Override
    public float getDigitalZoomScale() {
        return 0;
    }

    @Override
    public void setCameraLiveImageView(IImageDataReceiver target) {

    }
}
