package net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview;

import java.util.Map;

public interface IPtpIpLiveViewImageCallback
{
    void onCompleted(byte[] data, Map<String, Object> metadata);
    void onErrorOccurred(Exception  e);
}
