package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection;

import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandBase;

import static net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_CONNECT_13;

public class KodakConnectSequence13 extends KodakCommandBase
{
    private final IKodakCommandCallback callback;

    public KodakConnectSequence13(@Nullable IKodakCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public int getId()
    {
        return SEQ_CONNECT_13;
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]
                {
                        (byte) 0x2e , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0xba , (byte) 0x0b , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x10 , (byte) 0x00 , (byte) 0x80 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00
                });
    }

    @Override
    public IKodakCommandCallback responseCallback()
    {
        return (this.callback);
    }
}
