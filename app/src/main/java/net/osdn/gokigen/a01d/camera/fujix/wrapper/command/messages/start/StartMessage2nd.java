package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.FujiXCommandBase;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

public class StartMessage2nd extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public StartMessage2nd(@NonNull IFujiXCommandCallback callback)
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
        return (FujiXCameraConnectSequence.SEQ_START_2ND);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {
                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : START (0x1002)  : OpenSession
                (byte)0x02, (byte)0x10,

                // sequence number
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,
        });
    }
}
