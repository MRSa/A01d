package net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview;

import androidx.annotation.Nullable;

import java.util.Map;

public interface IPtpIpLiveViewImageCallback
{
    void onCompleted(@Nullable byte[] data, @Nullable Map<String, Object> metadata);
    void onErrorOccurred(Exception  e);
}
