package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages;

import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;

public class KodakCommandReceiveOnly extends KodakCommandBase
{
    private final IKodakCommandCallback callback;
    private final int id;

    public KodakCommandReceiveOnly(int id, @Nullable IKodakCommandCallback callback)
    {
        this.callback = callback;
        this.id = id;
    }

    @Override
    public IKodakCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (id);
    }

    @Override
    public byte[] commandBody()
    {
        return (null);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }

}
