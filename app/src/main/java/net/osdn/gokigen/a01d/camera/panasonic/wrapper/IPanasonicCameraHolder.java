package net.osdn.gokigen.a01d.camera.panasonic.wrapper;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.ICameraChangeListener;

public interface IPanasonicCameraHolder
{
    void detectedCamera(@NonNull IPanasonicCamera camera);
    void prepare();
    void startRecMode();
    void startEventWatch(@Nullable ICameraChangeListener listener);
}
