package net.osdn.gokigen.a01d.camera.fujix.wrapper.command;

public interface IFujiXCommandCallback
{
    void receivedMessage(int id, byte[] rx_body);
}
