package net.osdn.gokigen.a01d.camera.nikon.wrapper.connection;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.nikon.wrapper.status.NikonStatusChecker;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;

class NikonCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity activity;
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;
    private final NikonStatusChecker statusChecker;

    NikonCameraDisconnectSequence(Activity activity, @NonNull IPtpIpInterfaceProvider interfaceProvider, @NonNull NikonStatusChecker statusChecker)
    {
        this.activity = activity;
        this.command = interfaceProvider.getCommandCommunication();
        this.async = interfaceProvider.getAsyncEventCommunication();
        this.liveview = interfaceProvider.getLiveviewCommunication();
        this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            statusChecker.stopStatusWatch();
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
