package net.osdn.gokigen.a01d.camera.kodak.wrapper.command;

import androidx.annotation.NonNull;

public interface IKodakCommandPublisher
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IKodakCommand command);

    void start();
    void stop();
}
