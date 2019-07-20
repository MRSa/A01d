package net.osdn.gokigen.a01d.camera;

import android.app.Activity;
import android.content.SharedPreferences;

import net.osdn.gokigen.a01d.camera.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.FujiXInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.panasonic.IPanasonicInterfaceProvider;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.PanasonicCameraWrapper;
import net.osdn.gokigen.a01d.camera.ricohgr2.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.RicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.wrapper.SonyCameraWrapper;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

public class CameraInterfaceProvider implements IInterfaceProvider
{
    private final Activity context;
    private final OlympusInterfaceProvider olympus;
    private final SonyCameraWrapper sony;
    private final RicohGr2InterfaceProvider ricohGr2;
    private final FujiXInterfaceProvider fujiX;
    private final PanasonicCameraWrapper panasonic;
    private final CameraStatusListener statusListener;

    public CameraInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.context = context;
        this.statusListener = new CameraStatusListener();
        olympus = new OlympusInterfaceProvider(context, provider);
        sony = new SonyCameraWrapper(context, provider, statusListener);
        fujiX = new FujiXInterfaceProvider(context, provider, statusListener);
        panasonic = new PanasonicCameraWrapper(context, provider, statusListener);
        ricohGr2 = new RicohGr2InterfaceProvider(context, provider);
    }

    @Override
    public void setUpdateReceiver(@NonNull ICameraStatusUpdateNotify receiver)
    {
        try
        {
            statusListener.setUpdateReceiver(receiver);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    @Override
    public IFujiXInterfaceProvider getFujiXInterface()
    {
        return (fujiX);
    }

    @Override
    public IPanasonicInterfaceProvider getPanasonicInterface()
    {
        return (panasonic);
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
            else if (connectionMethod.contains("FUJI_X"))
            {
                ret = ICameraConnection.CameraConnectionMethod.FUJI_X;
            }
            else if (connectionMethod.contains("PANASONIC"))
            {
                ret = ICameraConnection.CameraConnectionMethod.PANASONIC;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }
}
