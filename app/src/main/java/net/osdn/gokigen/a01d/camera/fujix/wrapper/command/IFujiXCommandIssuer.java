package net.osdn.gokigen.a01d.camera.fujix.wrapper.command;

import androidx.annotation.NonNull;

public interface IFujiXCommandIssuer
{
    boolean isConnected();
    boolean enqueueCommand(@NonNull IFujiXCommand command);

    void start();
    void stop();
}
