package net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXCameraConnectSequence;

import static net.osdn.gokigen.a01d.camera.fujix.operation.FujiXFocusingControl.FOCUS_UNLOCK;

public class FocusUnlock extends FujiXCommandBase
{
    private final IFujiXCommandCallback callback;

    public FocusUnlock(@NonNull IFujiXCommandCallback callback)
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
        return (FOCUS_UNLOCK);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[] {

                // message_header.index : uint16 (0: terminate, 2: two_part_message, 1: other)
                (byte)0x01, (byte)0x00,

                // message_header.type : focus_unlock (0x9027)
                (byte)0x27, (byte)0x90,

                // message_id (0～1づつ繰り上がる...
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        });
    }
}
