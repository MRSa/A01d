package net.osdn.gokigen.a01d.preference.olympus;

/**
 *
 *
 *
 */
public interface IPreferencePropertyAccessor
{
    String EXIT_APPLICATION = "exit_application";

    String AUTO_CONNECT_TO_CAMERA = "auto_connect_to_camera";
    String BLE_POWER_ON = "ble_power_on";

    String TAKE_MODE =  "take_mode";
    String TAKE_MODE_DEFAULT_VALUE =  "P";

    String SOUND_VOLUME_LEVEL = "sound_volume_level";
    String SOUND_VOLUME_LEVEL_DEFAULT_VALUE = "OFF";

    String RAW = "raw";

    String LIVE_VIEW_QUALITY = "live_view_quality";
    String LIVE_VIEW_QUALITY_DEFAULT_VALUE = "QVGA";

    String CAMERAKIT_VERSION = "camerakit_version";

    String SHOW_GRID_STATUS = "show_grid";

    String DIGITAL_ZOOM_LEVEL = "digital_zoom_level";
    String DIGITAL_ZOOM_LEVEL_DEFAULT_VALUE = "1.0";

    String POWER_ZOOM_LEVEL = "power_zoom_level";
    String POWER_ZOOM_LEVEL_DEFAULT_VALUE = "1.0";

    String MAGNIFYING_LIVE_VIEW_SCALE = "magnifying_live_view_scale";
    String MAGNIFYING_LIVE_VIEW_SCALE_DEFAULT_VALUE = "10.0";

    String CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW = "capture_both_camera_and_live_view";

    String OLYCAMERA_BLUETOOTH_SETTINGS = "olympus_air_bt";

    String CONNECTION_METHOD = "connection_method";
    String CONNECTION_METHOD_DEFAULT_VALUE = "OPC";


/*
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