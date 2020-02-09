package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.graphics.Bitmap;

import java.util.Map;

public interface ICanonLiveViewImageCallback
{
    void onCompleted(byte[] data, Map<String, Object> metadata);
    void onErrorOccurred(Exception  e);
}
