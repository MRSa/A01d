package net.osdn.gokigen.a01d.camera.fujix.wrapper.status;

public interface IFujiXRunModeHolder
{
    void transitToRecordingMode(boolean isFinished);
    void transitToPlaybackMode(boolean isFinished);
}
