package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

public class ReceiveOnly extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public ReceiveOnly(@NonNull IFujiXCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public IFujiXCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (FujiXCameraConnectSequence.SEQ_START_2ND_RECEIVE);
    }

    @Override
    public byte[] commandBody()
    {
        return (null);
    }
}
