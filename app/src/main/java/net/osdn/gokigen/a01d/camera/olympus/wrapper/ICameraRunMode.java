package net.osdn.gokigen.a01d.camera.olympus.wrapper;

public interface ICameraRunMode
{
    /** カメラの動作モード変更 **/
    void changeRunMode(boolean isRecording);
    boolean isRecordingMode();
}
