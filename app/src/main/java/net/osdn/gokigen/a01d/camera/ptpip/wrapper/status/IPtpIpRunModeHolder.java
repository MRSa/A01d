package net.osdn.gokigen.a01d.camera.ptpip.wrapper.status;

public interface IPtpIpRunModeHolder
{
    void transitToRecordingMode(boolean isFinished);
    void transitToPlaybackMode(boolean isFinished);
}
