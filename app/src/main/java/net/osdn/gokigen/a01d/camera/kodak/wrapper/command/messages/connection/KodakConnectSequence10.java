package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection;

import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandBase;

import static net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_CONNECT_10;

public class KodakConnectSequence10 extends KodakCommandBase
{
    private final IKodakCommandCallback callback;

    public KodakConnectSequence10(@Nullable IKodakCommandCallback callback)
    {
        this.callback = callback;
    }

    @Override
    public int getId()
    {
        return SEQ_CONNECT_10;
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]
                {
                        (byte) 0x2e , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x20 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0xed , (byte) 0x03 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x80 ,
                        (byte) 0x12 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x20 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x12 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x69 , (byte) 0x08 , (byte) 0x00 , (byte) 0x00 , (byte) 0x69 , (byte) 0x08 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00
                });
    }

    @Override
    public IKodakCommandCallback responseCallback()
    {
        return (this.callback);
    }
}
