package net.osdn.gokigen.a01d.camera.sony.wrapper;

import java.util.List;

public interface ISonyCamera
{
    boolean hasApiService(String serviceName);
    List<SonyApiService> getApiServices();

    String getFriendlyName();
    String getModelName();
}
