package net.osdn.gokigen.a01d.camera.ptpip.command;

import androidx.annotation.NonNull;

public interface IPtpIpCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IPtpIpCommand command);

    boolean flushHoldQueue();

    void start();
    void stop();
}
