package net.osdn.gokigen.a01d.camera.sony.wrapper;

import java.util.Map;

public interface ISonyCameraLiveViewListener
{
    void onUpdateLiveView(byte[] data, Map<String, Object> metadata);
}
