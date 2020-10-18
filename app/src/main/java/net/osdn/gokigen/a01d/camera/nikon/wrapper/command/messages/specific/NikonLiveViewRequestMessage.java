package net.osdn.gokigen.a01d.camera.nikon.wrapper.command.messages.specific;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandBase;

public class NikonLiveViewRequestMessage extends PtpIpCommandBase
{
    private final IPtpIpCommandCallback callback;
    private final boolean isDumpLog;
    private final int delayMs;

    public NikonLiveViewRequestMessage(@NonNull IPtpIpCommandCallback callback,int delayMs, boolean isDumpLog)
    {
        this.callback = callback;
        this.delayMs = delayMs;
        this.isDumpLog = isDumpLog;
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (callback);
    }

    @Override
    public int getId()
    {
        return (SEQ_GET_VIEWFRAME);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[]{

                // packet type
                (byte) 0x06, (byte) 0x00,  (byte) 0x00, (byte) 0x00,

                // data phase info
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,

                // operation code
                (byte) 0x03, (byte) 0x92,

                // sequence number
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        });
    }

    @Override
    public boolean dumpLog()
    {
        return (isDumpLog);
    }

    @Override
    public int receiveDelayMs()
    {
        return (delayMs);
    }

    @Override
    public boolean isRetrySend()
    {
        return (true);
    }

    @Override
    public int maxRetryCount()
    {
        return (5);
    }

    @Override
    public boolean isIncrementSequenceNumberToRetry()
    {
        return (true);
    }

}
