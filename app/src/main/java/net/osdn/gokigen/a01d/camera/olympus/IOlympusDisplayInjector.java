package net.osdn.gokigen.a01d.camera.olympus;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IFocusingModeNotify;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

/**
 *
 *
 */
public interface IOlympusDisplayInjector
{
    void injectOlympusDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify);
}
