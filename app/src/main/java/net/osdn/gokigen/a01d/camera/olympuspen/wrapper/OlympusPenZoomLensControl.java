package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympuspen.IOlympusPenInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympuspen.operation.OlympusPenAutoFocusControl;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.hardware.OlympusPenHardwareStatus;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OlympusPenZoomLensControl  implements IZoomLensControl
{
    private final String TAG = toString();
    private boolean isZooming = false;
    private final OlympusPenHardwareStatus hardwareStatus;
    private static final int TIMEOUT_MS = 3000;
    private final String COMMUNICATION_URL = "http://192.168.0.10/";
    private Map<String, String> headerMap;


    OlympusPenZoomLensControl(@NonNull OlympusPenHardwareStatus hardwareStatus)
    {
        Log.v(TAG, "OlympusPenZoomLensControl()");

        headerMap = new HashMap<>();
        headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
        headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

        this.hardwareStatus = hardwareStatus;
    }

    @Override
    public boolean canZoom()
    {
        Log.v(TAG, "canZoom()");
        return (true);
    }

    @Override
    public void updateStatus()
    {
        hardwareStatus.updateStatus();
    }

    @Override
    public float getMaximumFocalLength()
    {
        return (hardwareStatus.getMaximumFocalLength());
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (hardwareStatus.getMinimumFocalLength());
    }

    @Override
    public float getCurrentFocalLength()
    {
        return (hardwareStatus.getActualFocalLength());
    }

    @Override
    public void driveZoomLens(float targetLength)
    {
        Log.v(TAG, "driveZoomLens() : " + targetLength);
    }

    @Override
    public void moveInitialZoomPosition()
    {
        Log.v(TAG, "moveInitialZoomPosition()");
    }

    @Override
    public boolean isDrivingZoomLens()
    {
        Log.v(TAG, "isDrivingZoomLens()");
        return (isZooming);
    }

    /**
     *
     *
     */
    @Override
    public void driveZoomLens(final boolean isZoomIn)
    {
        Log.v(TAG, "driveZoomLens() : " + isZoomIn);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String command;
                        if (isZooming)
                        {
                            command = "exec_takemisc.cgi?com=ctrlzoom&move=off";
                            //command = (isZoomIn) ? "exec_takemisc.cgi?com=ctrlzoom&move=teleterm" : "exec_takemisc.cgi?com=ctrlzoom&move=wideterm";
                        }
                        else
                        {
                            command = (isZoomIn) ? "exec_takemisc.cgi?com=ctrlzoom&move=telemove" : "exec_takemisc.cgi?com=ctrlzoom&move=widemove";
                        }
                        String reply =  SimpleHttpClient.httpGetWithHeader(COMMUNICATION_URL + command , headerMap, null, TIMEOUT_MS);
                        isZooming = !isZooming;
                        Log.v(TAG, "ZOOM : " + isZooming + " cmd : " + command + "  RET : " + reply);
/*
                        if (reply.contains("ok"))
                        {
                            isZooming = !isZooming;
                        }
                        else
                        {
                            Log.v(TAG, "driveZoomLens() reply is failure.");
                        }
*/
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
