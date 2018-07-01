package net.osdn.gokigen.a01d.camera;

import java.util.Map;

public interface ICameraLiveViewListener
{
    void onUpdateLiveView(byte[] data, Map<String, Object> metadata);
}
