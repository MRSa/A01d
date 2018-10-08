package net.osdn.gokigen.a01d.camera;

import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.CameraChangeListerTemplate;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

public class CameraStatusListener extends CameraChangeListerTemplate implements ICameraStatusUpdateNotify
{
    private  final String TAG = toString();
    private ICameraStatusUpdateNotify updateReceiver = null;


    CameraStatusListener()
    {
        Log.v(TAG, "CameraStatusListener()");
    }

    void setUpdateReceiver(@NonNull ICameraStatusUpdateNotify receiver)
    {
        updateReceiver = receiver;
    }

    @Override
    public void onFocusStatusChanged(String focusStatus)
    {
        Log.v(TAG, "onFocusStatusChanged() : " + focusStatus);
        if ((focusStatus == null)||(updateReceiver == null))
        {
            Log.v(TAG, "focusStatus or updateReceiver is NULL.");
            return;
        }
        switch (focusStatus)
        {
            case "Focused":
                updateReceiver.updateFocusedStatus(true, true);
                break;
            case "Failed":
                updateReceiver.updateFocusedStatus(false, true);
                break;
            case "Focusing":
            case "Not Focusing":
            default:
                updateReceiver.updateFocusedStatus(false, false);
                break;
        }
    }

    @Override
    public void updateDriveMode(String driveMode)
    {
        Log.v(TAG, "updateDriveMode() : " + driveMode);
    }

    @Override
    public void updateAeLockState(boolean isAeLocked)
    {

    }

    @Override
    public void updateCameraStatus(String message)
    {

    }

    @Override
    public void updateLevelGauge(String orientation, float roll, float pitch)
    {

    }

    @Override
    public void updatedTakeMode(String mode)
    {

    }

    @Override
    public void updatedShutterSpeed(String tv)
    {

    }

    @Override
    public void updatedAperture(String av)
    {

    }

    @Override
    public void updatedExposureCompensation(String xv)
    {

    }

    @Override
    public void updatedMeteringMode(String meteringMode)
    {

    }

    @Override
    public void updatedWBMode(String wbMode)
    {

    }

    @Override
    public void updateRemainBattery(int percentage)
    {

    }

    @Override
    public void updateFocusedStatus(boolean focused, boolean focusLocked)
    {
        if (updateReceiver != null)
        {
            updateReceiver.updateFocusedStatus(focused, focusLocked);
        }
    }

    @Override
    public void updateIsoSensitivity(String sv)
    {

    }

    @Override
    public void updateWarning(String warning)
    {

    }

    @Override
    public void updateStorageStatus(String status)
    {

    }
}
