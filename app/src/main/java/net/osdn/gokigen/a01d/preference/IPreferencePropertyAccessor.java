package net.osdn.gokigen.a01d.preference;

/**
 *
 *
 *
 */
public interface IPreferencePropertyAccessor
{
    String EXIT_APPLICATION = "exit_application";

    String AUTO_CONNECT_TO_CAMERA = "auto_connect_to_camera";

    String TAKE_MODE =  "take_mode";
    String TAKE_MODE_DEFAULT_VALUE =  "P";

    String SOUND_VOLUME_LEVEL = "sound_volume_level";
    String SOUND_VOLUME_LEVEL_DEFAULT_VALUE = "OFF";

    String RAW = "raw";

    String LIVE_VIEW_QUALITY = "live_view_quality";
    String LIVE_VIEW_QUALITY_DEFAULT_VALUE = "QVGA";

    String CAMERAKIT_VERSION = "camerakit_version";

    String SHOW_GRID_STATUS = "show_grid";
/*****
    int CHOICE_SPLASH_SCREEN = 10;

    int SELECT_SAMPLE_IMAGE_CODE = 110;
    int SELECT_SPLASH_IMAGE_CODE = 120;

    String getLiveViewSize();
    void restoreCameraSettings(Callback callback);
    void storeCameraSettings(Callback callback);

    interface Callback
    {
        void stored(boolean result);
        void restored(boolean result);
    }
*****/

}