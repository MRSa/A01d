package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection;

/**
 *
 *
 */
public interface ICameraStatusReceiver
{
    void onStatusNotify(String message);
    void onCameraConnected();
    void onCameraDisconnected();
    void onCameraOccursException(String message, Exception e);

    boolean isAutoConnectCamera();
}
