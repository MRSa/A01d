package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommand;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;

import static net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_DUMMY;

public class KodakCommandBase  implements IKodakCommand
{
    @Override
    public int getId()
    {
        return SEQ_DUMMY;
    }

    @Override
    public int receiveDelayMs()
    {
        return (30);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[0]);
    }

    @Override
    public byte[] commandBody2()
    {
        return (null);
    }

    @Override
    public int maxRetryCount()
    {
        return (50);
    }

    @Override
    public boolean sendRetry()
    {
        return (false);
    }

    @Override
    public IKodakCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }
}
