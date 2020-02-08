package net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class CanonSetDevicePropertyValue extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int id;
    private final int holdId;
    private final int delayMs;

    private final byte data00;
    private final byte data01;
    private final byte data02;
    private final byte data03;

    private final byte data10;
    private final byte data11;
    private final byte data12;
    private final byte data13;


    public CanonSetDevicePropertyValue(@NonNull IPtpIpCommandCallback callback, int id, boolean isDumpLog, int holdId, int delayMs, int objectId, int value)
    {
        this.callback = callback;
        this.isDumpLog = isDumpLog;
        this.id = id;
        this.holdId = holdId;
        this.delayMs = delayMs;

        data00 = ((byte) (0x000000ff & objectId));
        data01 = ((byte)((0x0000ff00 & objectId) >> 8));
        data02 = ((byte)((0x00ff0000 & objectId) >> 16));
        data03 = ((byte)((0xff000000 & objectId) >> 24));

        data10 = ((byte) (0x000000ff & value));
        data11 = ((byte)((0x0000ff00 & value) >> 8));
        data12 = ((byte)((0x00ff0000 & value) >> 16));
        data13 = ((byte)((0xff000000 & value) >> 24));
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
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
        return (new byte[]{
                // packet type
                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // data phase info
                (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // operation code
                (byte) 0x10, (byte) 0x91,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

        });
    }

    @Override
    public byte[] commandBody2()
    {
        return (new byte[]{

                // packet type
                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // データサイズ
                (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

    @Override
    public byte[] commandBody3()
    {
        return (new byte[]{

                // packet type
                (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // data
                (byte) 0x0c, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                data00, data01, data02, data03,
                data10, data11, data12, data13,
        });
    }

    @Override
    public int receiveDelayMs()
    {
        return (delayMs);
    }

    @Override
    public int getHoldId()
    {
        return (holdId);
    }

    @Override
    public boolean dumpLog()
    {
        return (isDumpLog);
    }

}
