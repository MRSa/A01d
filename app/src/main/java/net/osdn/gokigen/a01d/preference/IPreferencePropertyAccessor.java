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
    String BLE_POWER_ON = "ble_power_on";

    String WIFI_SETTINGS = "wifi_settings";

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
    String CAPTURE_ONLY_LIVE_VIEW = "capture_only_live_view";


    String OLYCAMERA_BLUETOOTH_SETTINGS = "olympus_air_bt";

    String CONNECTION_METHOD = "connection_method";
    String CONNECTION_METHOD_DEFAULT_VALUE = "OPC";

    String GR2_DISPLAY_MODE = "gr2_display_mode";
    String GR2_DISPLAY_MODE_DEFAULT_VALUE = "0";

    String GR2_LCD_SLEEP = "gr2_lcd_sleep";
    String GR2_LIVE_VIEW = "gr2_display_camera_view";
    String USE_PENTAX_AUTOFOCUS = "use_pentax_autofocus_mode";

    String FUJIX_DISPLAY_CAMERA_VIEW = "fujix_display_camera_view";

    String FUJIX_FOCUS_XY = "fujix_focus_xy";
    String FUJIX_FOCUS_XY_DEFAULT_VALUE = "7,7";

    String FUJIX_LIVEVIEW_WAIT = "fujix_liveview_wait";
    String FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE = "80";

    String FUJIX_COMMAND_POLLING_WAIT = "fujix_command_polling_wait";
    String FUJIX_COMMAND_POLLING_WAIT_DEFAULT_VALUE = "500";

    String FUJIX_CONNECTION_FOR_READ = "fujix_connection_for_read";

    String USE_OSC_THETA_V21 = "use_osc_theta_v21";

    String CANON_FOCUS_XY = "canon_focus_xy";
    String CANON_FOCUS_XY_DEFAULT_VALUE = "6000,4000";

    String CANON_ZOOM_MAGNIFICATION = "canon_zoom_magnification";
    String CANON_ZOOM_MAGNIFICATION_DEFAULT_VALUE = "0";

    String CANON_ZOOM_RESOLUTION = "canon_zoom_resolution";
    String CANON_ZOOM_RESOLUTION_DEFAULT_VALUE = "25";

    String NIKON_FOCUS_XY = "nikon_focus_xy";
    String NIKON_FOCUS_XY_DEFAULT_VALUE = "6000,4000";

    String NIKON_NOT_SUPPORT_FOCUS_LOCK = "nikon_not_support_focus_lock";

    String DEBUG_INFO = "debug_info";

    String PREFERENCE_OPC_SETTINGS = "opc_settings";
    String PREFERENCE_OLYMPUS_SETTINGS = "olympus_settings";
    String PREFERENCE_SONY_SETTINGS = "sony_settings";
    String PREFERENCE_RICOH_SETTINGS = "ricoh_settings";
    String PREFERENCE_THETA_SETTINGS = "theta_settings";
    String PREFERENCE_FUJI_X_SETTINGS = "fuji_x_settings";
    String PREFERENCE_PANASONIC_SETTINGS = "panasonic_settings";
    String PREFERENCE_CANON_SETTINGS = "canon_settings";
    String PREFERENCE_NIKON_SETTINGS = "nikon_settings";
    String PREFERENCE_KODAK_SETTINGS = "kodak_settings";

    String CACHE_LIVEVIEW_PICTURES = "cache_liveview_pictures";
    String NUMBER_OF_CACHE_PICTURES = "number_of_cache_pictures";
    String NUMBER_OF_CACHE_PICTURES_DEFAULT_VALUE = "500";

    String SEND_MESSAGE_DIALOG = "dialog_message_send";

    String KODAK_FLASH_MODE = "kodak_flash_mode";
    String KODAK_FLASH_MODE_DEFAULT_VALUE = "OFF";

    String KODAK_HOST_IP = "kodak_host_ip";
    String KODAK_HOST_IP_DEFAULT_VALUE = "172.16.0.254";

    String KODAK_COMMAND_PORT = "kodak_command_port";
    String KODAK_COMMAND_PORT_DEFAULT_VALUE = "9175";

    String KODAK_LIVEVIEW_PORT = "kodak_liveview_port";
    String KODAK_LIVEVIEW_PORT_DEFAULT_VALUE = "9176";

    String SAVE_LOCAL_LOCATION = "save_local_location";
    boolean SAVE_LOCAL_LOCATION_DEFAULT_VALUE = false;
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
*/

}
