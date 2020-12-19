package net.osdn.gokigen.a01d.camera.canon.wrapper.connection;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;

class CanonCameraDisconnectSequence implements Runnable
{
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;

    CanonCameraDisconnectSequence(@NonNull IPtpIpInterfaceProvider interfaceProvider)
    {
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
