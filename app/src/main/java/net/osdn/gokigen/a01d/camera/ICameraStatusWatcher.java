package net.osdn.gokigen.a01d.camera;


import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import androidx.annotation.NonNull;

public interface ICameraStatusWatcher
{
    void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier);
    void stopStatusWatch();
}
