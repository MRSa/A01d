package net.osdn.gokigen.a01d.camera;

import android.support.annotation.NonNull;

import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

public interface ICameraStatusWatcher
{
    void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier);
    void stopStatusWatch();
}
