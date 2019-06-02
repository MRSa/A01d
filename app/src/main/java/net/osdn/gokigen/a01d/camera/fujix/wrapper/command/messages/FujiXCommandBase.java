package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommand;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

public class FujiXCommandBase implements IFujiXCommand
{
    @Override
    public int getId()
    {
        return (FujiXCameraConnectSequence.SEQ_DUMMY);
    }

    @Override
    public boolean useSequenceNumber()
    {
        return (true);
    }

    @Override
    public boolean isIncrementSeqNumber()
    {
        return (true);
    }

    @Override
    public int receiveDelayMs()
    {
        return (50);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[12]);
    }

    @Override
    public byte[] commandBody2()
    {
        return (null);
    }

    @Override
    public IFujiXCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }
}
