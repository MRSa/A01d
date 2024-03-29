package net.osdn.gokigen.a01d.camera.sony.wrapper;


import net.osdn.gokigen.a01d.camera.ICameraChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ISonyCameraHolder
{
    void detectedCamera(@NonNull ISonyCamera camera);
    void prepare();
    void startRecMode();
    void startEventWatch(@Nullable ICameraChangeListener listener);
}
