package net.osdn.gokigen.a01d.liveview.liveviewlistener;

import java.util.Map;

public interface IImageDataReceiver
{
    void setImageData(byte[] data, Map<String, Object> metadata);
}
