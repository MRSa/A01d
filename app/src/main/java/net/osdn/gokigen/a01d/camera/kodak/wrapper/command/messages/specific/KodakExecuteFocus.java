package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific;

import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandBase;

import static net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_FOCUS;

public class KodakExecuteFocus  extends KodakCommandBase
{
    private final IKodakCommandCallback callback;

    private final byte data00;
    private final byte data01;
    private final byte data02;
    private final byte data03;

    private final byte data10;
    private final byte data11;
    private final byte data12;
    private final byte data13;

    public KodakExecuteFocus(@Nullable IKodakCommandCallback callback, int posX, int posY)
    {
        this.callback = callback;
        data00 = ((byte) (0x000000ff & posX));
        data01 = ((byte)((0x0000ff00 & posX) >> 8));
        data02 = ((byte)((0x00ff0000 & posX) >> 16));
        data03 = ((byte)((0xff000000 & posX) >> 24));

        data10 = ((byte) (0x000000ff & posY));
        data11 = ((byte)((0x0000ff00 & posY) >> 8));
        data12 = ((byte)((0x00ff0000 & posY) >> 16));
        data13 = ((byte)((0xff000000 & posY) >> 24));
    }

    @Override
    public int getId()
    {
        return SEQ_FOCUS;
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]
                {
                        (byte) 0x2e , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x18 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0xf6 , (byte) 0x03 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x80 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x01 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x18 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,      data00 ,      data01 ,      data02 ,      data03 ,      data10 ,      data11 ,      data12 ,      data13 ,
                        (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                        (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0xff , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 , (byte) 0x00 ,
                });
    }

    @Override
    public IKodakCommandCallback responseCallback()
    {
        return (this.callback);
    }
}
