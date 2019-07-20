package net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener;

import java.util.List;

public interface ICameraStatusHolder
{
    String getCameraStatus();
    boolean getLiveviewStatus();
    String getShootMode();
    List<String> getAvailableShootModes();
    int getZoomPosition();
    String getStorageId();

}
