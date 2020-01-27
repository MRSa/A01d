package net.osdn.gokigen.a01d.camera.canon.wrapper.connection;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.canon.ICanonInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;

class CanonCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity activity;
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;

    CanonCameraDisconnectSequence(Activity activity, @NonNull ICanonInterfaceProvider interfaceProvider)
    {
        this.activity = activity;
        this.command = interfaceProvider.getCommandCommunication();
        this.async = interfaceProvider.getAsyncEventCommunication();
        this.liveview = interfaceProvider.getLiveviewCommunication();
    }

    @Override
    public void run()
    {
        try
        {
            liveview.disconnect();
            async.disconnect();
            command.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
