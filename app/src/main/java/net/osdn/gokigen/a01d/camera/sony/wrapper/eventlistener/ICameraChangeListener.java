package net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener;

import java.util.List;

/**
 *
 *
 */
public interface ICameraChangeListener
{
    void onApiListModified(List<String> apis);
    void onCameraStatusChanged(String status);
    void onLiveviewStatusChanged(boolean status);
    void onShootModeChanged(String shootMode);
    void onZoomPositionChanged(int zoomPosition);
    void onStorageIdChanged(String storageId);
    void onResponseError();
}
