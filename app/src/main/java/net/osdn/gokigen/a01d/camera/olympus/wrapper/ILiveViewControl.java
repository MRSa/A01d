package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import net.osdn.gokigen.a01d.liveview.CameraLiveViewListenerImpl;

/**
 *
 *
 */
public interface ILiveViewControl
{
    void changeLiveViewSize(String size);
    void setLiveViewListener(CameraLiveViewListenerImpl listener);
    void startLiveView();

    void setDigitalZoom(float scale);
    void setLiveViewScale(float scale);

    void updateDigitalZoom();
    void updateMagnifyingLiveViewScale();
}
