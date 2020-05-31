package net.osdn.gokigen.a01d;

import net.osdn.gokigen.a01d.camera.ICameraConnection;

/**
 *
 */
public interface IChangeScene
{
    void changeSceneToCameraPropertyList();
    void changeSceneToConfiguration();
    void changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod connectionMethod);
    void changeCameraConnection();
    void changeSceneToDebugInformation();
    void changeSceneToApiList();
    void exitApplication();
}
