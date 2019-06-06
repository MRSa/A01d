package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

public class SetPropertyValue extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;
    private final boolean isShortMessage;
    private final byte id0;
    private final byte id1;
    private final byte data0;
    private final byte data1;
    private final byte data2;
    private final byte data3;

    public SetPropertyValue(@NonNull IFujiXCommandCallback callback, int id, int value, boolean isShort)
    {
        this.callback = callback;
        this.isShortMessage = isShort;

        id0 = ((byte) (0x000000ff & id));
        id1 = ((byte)((0x0000ff00 & id) >> 8));

        data0 = ((byte) (0x000000ff & value));
        data1 = ((byte)((0x0000ff00 & value) >> 8));
        data2 = ((byte)((0x00ff0000 & value) >> 16));
        data3 = ((byte)((0xff000000 & value) >> 24));
    }

    @Override
    public IFujiXCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (FujiXCameraConnectSequence.SEQ_STATUS_REQUEST);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {
                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : two_part (0x1016)
                (byte)0x16, (byte)0x10,

                // message_id (0～1づつ繰り上がる)
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,

                // command code
                id0, id1, (byte)0x00, (byte)0x00,
        });
    }

    @Override
    public byte[] commandBody2()
    {
        if (isShortMessage) {
            return (new byte[]{
                    // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                    (byte) 0x02, (byte) 0x00,

                    // message_header.type : two_part (0x1016)
                    (byte) 0x16, (byte) 0x10,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // ...data...
                    data0, data1,
            });
        } else {
            return (new byte[]{
                    // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                    (byte) 0x02, (byte) 0x00,

                    // message_header.type : two_part (0x1016)
                    (byte) 0x16, (byte) 0x10,

                    // sequence number
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                    // ...data...
                    data0, data1, data2, data3,
            });
        }
    }

    @Override
    public boolean dumpLog()
    {
        return (false);
    }
}
