package net.osdn.gokigen.a01d.camera.canon.operation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific.CanonSetDevicePropertyValue;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY;

public class CanonZoomLensControl implements IZoomLensControl, IPtpIpCommandCallback
{
    private final String TAG = this.toString();

    private float maxZoomMagnification;
    private float zoomStep;
    private float currentZoomPosition = 1.0f;
    private final PtpIpCommandPublisher commandPublisher;
    private boolean isDrivingZoom = false;

    public CanonZoomLensControl(@NonNull Activity context, @NonNull PtpIpCommandPublisher commandPublisher)
    {
        this.commandPublisher = commandPublisher;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String zoomSize = preferences.getString(IPreferencePropertyAccessor.CANON_ZOOM_MAGNIFICATION, IPreferencePropertyAccessor.CANON_ZOOM_MAGNIFICATION_DEFAULT_VALUE);
            String zoomResolution = preferences.getString(IPreferencePropertyAccessor.CANON_ZOOM_RESOLUTION, IPreferencePropertyAccessor.CANON_ZOOM_RESOLUTION_DEFAULT_VALUE);

            this.maxZoomMagnification = (float) Integer.parseInt(zoomSize);
            float zoomResolutionFloat = (float) Integer.parseInt(zoomResolution);
            if (zoomResolutionFloat < 5.0f)
            {
                zoomResolutionFloat = 5.0f;
            }
            else if (zoomResolutionFloat > 100.0f)
            {
                zoomResolutionFloat = 100.0f;
            }
            zoomStep = maxZoomMagnification / zoomResolutionFloat;
            Log.v(TAG, "ZOOM MAGNIFICATION : " + maxZoomMagnification + " [step: " + this.zoomStep + "]");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            maxZoomMagnification = 0.0f;
            zoomStep = 1.0f;
        }
    }

    @Override
    public boolean canZoom()
    {
        return (!(maxZoomMagnification < 1.0f));
    }

    @Override
    public void updateStatus()
    {

    }

    @Override
    public float getMaximumFocalLength()
    {
        return (maxZoomMagnification);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (1.0f);
    }

    @Override
    public float getCurrentFocalLength()
    {
        return (currentZoomPosition);
    }

    @Override
    public void driveZoomLens(float targetLength)
    {
        int targetLengthInt;
        try
        {
            if (targetLength > maxZoomMagnification)
            {
                targetLengthInt = (int) Math.ceil(maxZoomMagnification * 10.0f);
                targetLength = maxZoomMagnification;
            }
            else if (targetLength < 1.0f)
            {
                targetLengthInt = 10;
                targetLength = 1.0f;
            }
            else
            {
                targetLengthInt = Math.round(targetLength * 10.0f);
            }
            commandPublisher.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_SET_DEVICE_PROPERTY, false, 0, 30, 0xd055, targetLengthInt));
            currentZoomPosition = targetLength;
            isDrivingZoom = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void driveZoomLens(boolean isZoomIn)
    {
        float targetLength = currentZoomPosition + ((isZoomIn)? zoomStep : -zoomStep);
        if (targetLength < 1.0f)
        {
            targetLength = 1.0f;
        }
        else if (targetLength > maxZoomMagnification)
        {
            targetLength = maxZoomMagnification;
        }
        driveZoomLens(targetLength);
    }

    @Override
    public void moveInitialZoomPosition()
    {
        driveZoomLens(1.0f);
    }

    @Override
    public boolean isDrivingZoomLens()
    {
        return (isDrivingZoom);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        isDrivingZoom =false;
        Log.v(TAG, " ZOOM FINISHED.  : " + currentZoomPosition);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
