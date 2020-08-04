package net.osdn.gokigen.a01d.camera.kodak.wrapper.command;

public interface IKodakCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
}
