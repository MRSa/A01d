package net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraChangeListener;

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
