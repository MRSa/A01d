package net.osdn.gokigen.a01d.camera.kodak.wrapper.connection;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.kodak.IKodakInterfaceProvider;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommunication;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.KodakStatusChecker;

class KodakCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final IKodakCommunication command;
    private final KodakStatusChecker statusChecker;

    KodakCameraDisconnectSequence(@NonNull IKodakInterfaceProvider interfaceProvider, @NonNull KodakStatusChecker statusChecker)
    {
        this.command = interfaceProvider.getCommandCommunication();
        this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            statusChecker.stopStatusWatch();
            command.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
