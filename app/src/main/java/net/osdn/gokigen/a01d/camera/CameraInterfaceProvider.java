package net.osdn.gokigen.a01d.camera;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.sony.wrapper.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.wrapper.SonyCameraWrapper;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class CameraInterfaceProvider implements IInterfaceProvider
{
    private final Activity context;
    private final OlympusInterfaceProvider olympus;
    private final SonyCameraWrapper sony;

    public CameraInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.context = context;
        olympus = new OlympusInterfaceProvider(context, provider);
        sony = new SonyCameraWrapper(context, provider);
    }

    @Override
    public IOlympusInterfaceProvider getOlympusInterface()
    {
        return (olympus);
    }

    @Override
    public IDisplayInjector getOlympusDisplayInjector()
    {
        return (olympus);
    }

    @Override
    public IOlympusLiveViewListener getOlympusLiveViewListener()
    {
        return (olympus.getLiveViewListener());
    }

    @Override
    public ISonyInterfaceProvider getSonyInterface()
    {
        return (sony);
    }

    /**
     *   OPCカメラを使用するかどうか
     *
     * @return  true : OPCカメラ /  false : OPCカメラではない
     */
    public boolean useOlympusCamera()
    {
        boolean ret = true;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String connectionMethod = preferences.getString(IPreferencePropertyAccessor.CONNECTION_METHOD, "OPC");
            ret = connectionMethod.contains("OPC");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }
}
