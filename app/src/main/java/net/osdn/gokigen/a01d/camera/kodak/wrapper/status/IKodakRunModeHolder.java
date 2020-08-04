package net.osdn.gokigen.a01d.camera.kodak.wrapper.status;

public interface IKodakRunModeHolder
{
    void transitToRecordingMode(boolean isFinished);
    void transitToPlaybackMode(boolean isFinished);
}
