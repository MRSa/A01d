package net.osdn.gokigen.a01d.camera.sony.wrapper;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IFocusingModeNotify;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

/**
 *
 *
 */
public interface IDisplayInjector
{
    void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify);
}
