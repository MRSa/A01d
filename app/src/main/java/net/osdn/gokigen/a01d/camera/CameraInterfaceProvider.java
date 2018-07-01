package net.osdn.gokigen.a01d.camera;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.RicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.wrapper.SonyCameraWrapper;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class CameraInterfaceProvider implements IInterfaceProvider
{
    private final Activity context;
    private final OlympusInterfaceProvider olympus;
    private final SonyCameraWrapper sony;
    private final RicohGr2InterfaceProvider ricohGr2;

    public CameraInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.context = context;
        olympus = new OlympusInterfaceProvider(context, provider);
        sony = new SonyCameraWrapper(context, provider);
        ricohGr2 = new RicohGr2InterfaceProvider(context, provider);
    }

    @Override
    public IOlympusInterfaceProvider getOlympusInterface()
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

    @Override
    public IRicohGr2InterfaceProvider getRicohGr2Infterface()
    {
        return (ricohGr2);
    }

    /**
     *   OPCカメラを使用するかどうか
     *
     * @return OPC / SONY / RICOH_GR2  (ICameraConnection.CameraConnectionMethod)
     */
    public ICameraConnection.CameraConnectionMethod getCammeraConnectionMethod()
    {
        ICameraConnection.CameraConnectionMethod ret = ICameraConnection.CameraConnectionMethod.OPC;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String connectionMethod = preferences.getString(IPreferencePropertyAccessor.CONNECTION_METHOD, "OPC");
            if (connectionMethod.contains("SONY"))
            {
                ret = ICameraConnection.CameraConnectionMethod.SONY;
            }
            else if (connectionMethod.contains("RICOH_GR2"))
            {
                ret = ICameraConnection.CameraConnectionMethod.RICOH_GR2;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }
}
