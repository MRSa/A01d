package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages;

import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommand;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages;

public class PtpIpCommandBase implements IPtpIpCommand, IPtpIpMessages
{
    @Override
    public int getId()
    {
        return (SEQ_DUMMY);
    }

    @Override
    public boolean receiveAgainShortLengthMessage()
    {
        return (true);
    }

    @Override
    public boolean useSequenceNumber()
    {
        return (true);
    }

    @Override
    public boolean isIncrementSeqNumber()
    {
        return (true);
    }

    @Override
    public int receiveDelayMs()
    {
        return (15);
    }

    @Override
    public int embeddedSequenceNumberIndex()
    {
        return (14);
    }

    @Override
    public int embeddedSequenceNumberIndex2()
    {
        return (8);
    }

    @Override
    public int embeddedSequenceNumberIndex3()
    {
        return (8);
    }

    @Override
    public int estimatedReceiveDataSize()
    {
        return (-1);
    }

    @Override
    public byte[] commandBody()
    {
        return (new byte[12]);
    }

    @Override
    public byte[] commandBody2()
    {
        return (null);
    }

    @Override
    public byte[] commandBody3()
    {
        return (null);
    }

    @Override
    public IPtpIpCommandCallback responseCallback()
    {
        return (null);
    }

    @Override
    public int getHoldId()
    {
        return (0);
    }

    @Override
    public boolean isHold()
    {
        return (false);
    }

    @Override
    public boolean isRelease()
    {
        return (false);
    }

    @Override
    public boolean dumpLog()
    {
        return (true);
    }

    @Override
    public boolean isRetrySend()
    {
        return (true);
    }

    @Override
    public boolean isLastReceiveRetry()
    {
        return (false);
    }

    @Override
    public int maxRetryCount()
    {
        return (20);
    }

    @Override
    public boolean isIncrementSequenceNumberToRetry()
    {
        return (false);
    }

}
