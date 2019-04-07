package net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener;

import androidx.annotation.NonNull;

public interface ICameraEventObserver
{
    void activate();
    boolean start();
    void stop();
    void release();

    void setEventListener(@NonNull ICameraChangeListener listener);
    void clearEventListener();

    ICameraStatusHolder getCameraStatusHolder();
}
