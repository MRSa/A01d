package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.changemode;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.FujiXCommandBase;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

public class ChangeToLiveView3rd   extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public ChangeToLiveView3rd(@NonNull IFujiXCommandCallback callback)
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
        return (FujiXCameraConnectSequence.SEQ_CHANGE_TO_LIVEVIEW_3RD);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {

                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : single_part (0x1015) : 0xd212 (status_request)
                (byte)0x15, (byte)0x10,

                // sequence number
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,

                // data ...
                (byte)0x24, (byte)0xdf, (byte)0x00, (byte)0x00,
        });
    }
}
