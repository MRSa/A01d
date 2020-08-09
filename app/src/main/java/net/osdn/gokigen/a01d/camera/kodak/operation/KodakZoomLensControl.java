package net.osdn.gokigen.a01d.camera.kodak.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakExecuteZoom;

public class KodakZoomLensControl implements IZoomLensControl, IKodakCommandCallback
{
    private final String TAG = this.toString();

    private final IKodakCommandPublisher commandPublisher;

    public KodakZoomLensControl(@NonNull IKodakCommandPublisher commandPublisher)
    {
        this.commandPublisher = commandPublisher;
    }

    @Override
    public boolean canZoom()
    {
        return (true);
    }

    @Override
    public void updateStatus()
    {

    }

    @Override
    public float getMaximumFocalLength()
    {
        return (0);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (0);
    }

    @Override
    public float getCurrentFocalLength()
    {
        return (0);
    }

    @Override
    public void driveZoomLens(float targetLength)
    {

    }

    @Override
    public void driveZoomLens(boolean isZoomIn)
    {
        try
        {
            Log.v(TAG, " Zoom in : " + isZoomIn);
            commandPublisher.enqueueCommand(new KodakExecuteZoom(this, (isZoomIn) ? 1 : -1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void moveInitialZoomPosition()
    {

    }

    @Override
    public boolean isDrivingZoomLens()
    {
        return (false);
    }


    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " KodakFocusingControl::receivedMessage() : ");
    }
}
