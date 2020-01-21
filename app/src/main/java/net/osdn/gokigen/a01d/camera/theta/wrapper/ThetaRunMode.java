package net.osdn.gokigen.a01d.camera.theta.wrapper;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;

public class ThetaRunMode implements ICameraRunMode
{
    private boolean runMode = false;

    ThetaRunMode()
    {
        //
    }

    @Override
    public void changeRunMode(boolean isRecording)
    {
        this.runMode = isRecording;
    }

    @Override
    public boolean isRecordingMode()
    {
        return (runMode);
    }
}
