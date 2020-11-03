package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command;

import androidx.annotation.NonNull;

public interface IPtpIpCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IPtpIpCommand command);

    boolean flushHoldQueue();

    int isExistCommandMessageQueue(int id);

    int getCurrentQueueSize();

    void start();
    void stop();
}
