package net.osdn.gokigen.a01d.camera.theta.wrapper.status;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.List;

public class ThetaCameraStatusWatcher implements ICameraStatus, ICameraStatusWatcher
{
    public ThetaCameraStatusWatcher()
    {

    }


    @Override
    public List<String> getStatusList(String key) {
        return null;
    }

    @Override
    public String getStatus(String key) {
        return null;
    }

    @Override
    public void setStatus(String key, String value) {

    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier) {

    }

    @Override
    public void stopStatusWatch() {

    }
}
