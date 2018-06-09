package net.osdn.gokigen.a01d.camera.sony.wrapper;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.wrapper.connection.SonyCameraConnection;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.CameraEventObserver;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraChangeListener;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraEventObserver;

public class SonyCameraWrapper implements ISonyCameraHolder, ISonyInterfaceProvider
{
    private final String TAG = toString();
    private final Activity context;
    private final ICameraStatusReceiver provider;
    private ISonyCamera sonyCamera = null;
    private ISonyCameraApi sonyCameraApi = null;
    private ICameraEventObserver eventObserver = null;

    public SonyCameraWrapper(final Activity context, final ICameraStatusReceiver statusReceiver)
    {
        this.context = context;
        this.provider = statusReceiver;
    }

    @Override
    public void prepare()
    {
        Log.v(TAG, " prepare : " + sonyCamera.getFriendlyName() + " " + sonyCamera.getModelName());
        sonyCameraApi = SonyCameraApi.newInstance(sonyCamera);
        eventObserver = CameraEventObserver.newInstance(context, sonyCameraApi);

}

    @Override
    public void startEventWatch(@Nullable ICameraChangeListener listener)
    {
        try
        {
            if (eventObserver != null)
            {
                if (listener != null)
                {
                    eventObserver.setEventListener(listener);
                }
                eventObserver.activate();
                eventObserver.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void detectedCamera(@NonNull ISonyCamera camera)
    {
        sonyCamera = camera;
    }

    @Override
    public ICameraConnection getSonyCameraConnection()
    {
        return (new SonyCameraConnection(context, provider, this));
    }
}
