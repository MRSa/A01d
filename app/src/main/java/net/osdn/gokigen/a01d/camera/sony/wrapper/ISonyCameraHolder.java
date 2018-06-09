package net.osdn.gokigen.a01d.camera.sony.wrapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraChangeListener;

public interface ISonyCameraHolder
{
    void detectedCamera(@NonNull ISonyCamera camera);
    void prepare();
    void startEventWatch(@Nullable ICameraChangeListener listener);
}
