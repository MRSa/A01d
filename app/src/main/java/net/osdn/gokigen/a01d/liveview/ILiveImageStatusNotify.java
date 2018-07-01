package net.osdn.gokigen.a01d.liveview;

import net.osdn.gokigen.a01d.liveview.message.IMessageDrawer;

/**
 *
 *
 */
interface ILiveImageStatusNotify
{
    void toggleFocusAssist();
    void toggleShowGridFrame();
    void takePicture();
    IMessageDrawer getMessageDrawer();
}
