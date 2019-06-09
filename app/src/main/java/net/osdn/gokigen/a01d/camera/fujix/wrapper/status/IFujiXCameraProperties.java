package net.osdn.gokigen.a01d.camera.fujix.wrapper.status;

public interface IFujiXCameraProperties
{
    int BATTERY_LEVEL         = 0x5001;
    int WHITE_BALANCE         = 0x5005;
    int APERTURE               = 0x5007;
    int FOCUS_MODE            = 0x500a;
    int SHOOTING_MODE         = 0x500e;
    int FLASH                 = 0x500c;
    int EXPOSURE_COMPENSATION = 0x5010;
    int SELF_TIMER            = 0x5012;
    int FILM_SIMULATION       = 0xd001;
    int IMAGE_FORMAT          = 0xd018;
    int RECMODE_ENABLE        = 0xd019;
    int F_SS_CONTROL          = 0xd028;
    int ISO                   = 0xd02a;
    int MOVIE_ISO             = 0xd02b;
    int FOCUS_POINT           = 0xd17c;
    int FOCUS_LOCK            = 0xd209;
    int DEVICE_ERROR          = 0xd21b;
    int SDCARD_REMAIN_SIZE    = 0xd229;
    int MOVIE_REMAINING_TIME  = 0xd22a;
    int SHUTTER_SPEED         = 0xd240;
    int IMAGE_ASPECT          = 0xd241;
    int BATTERY_LEVEL_2       = 0xd242;

    String BATTERY_LEVEL_STR         = "Battery";
    String WHITE_BALANCE_STR         = "WhiteBalance";
    String APERTURE_STR               = "Aperture";
    String FOCUS_MODE_STR            = "FocusMode";
    String SHOOTING_MODE_STR         = "ShootingMode";
    String FLASH_STR                 = "FlashMode";
    String EXPOSURE_COMPENSATION_STR = "ExposureCompensation";
    String SELF_TIMER_STR            = "SelfTimer";
    String FILM_SIMULATION_STR       = "FilmSimulation";
    String IMAGE_FORMAT_STR          = "ImageFormat";
    String RECMODE_ENABLE_STR        = "RecModeEnable";
    String F_SS_CONTROL_STR          = "F_SS_Control";
    String ISO_STR                   = "Iso";
    String MOVIE_ISO_STR             = "MovieIso";
    String FOCUS_POINT_STR           = "FocusPoint";
    String FOCUS_LOCK_STR            = "FocusLock";
    String DEVICE_ERROR_STR          = "DeviceError";
    String SDCARD_REMAIN_SIZE_STR    = "ImageRemainCount";
    String MOVIE_REMAINING_TIME_STR  = "MovieRemainTime";
    String SHUTTER_SPEED_STR         = "ShutterSpeed";
    String IMAGE_ASPECT_STR          = "ImageAspect";
    String BATTERY_LEVEL_2_STR       = "BatteryLevel";

    String BATTERY_LEVEL_STR_ID         = "0x5001";
    String WHITE_BALANCE_STR_ID         = "0x5005";
    String APERTURE_STR_ID               = "0x5007";
    String FOCUS_MODE_STR_ID            = "0x500a";
    String SHOOTING_MODE_STR_ID         = "0x500e";
    String FLASH_STR_ID                 = "0x500c";
    String EXPOSURE_COMPENSATION_STR_ID = "0x5010";
    String SELF_TIMER_STR_ID            = "0x5012";
    String FILM_SIMULATION_STR_ID       = "0xd001";
    String IMAGE_FORMAT_STR_ID          = "0xd018";
    String RECMODE_ENABLE_STR_ID        = "0xd019";
    String F_SS_CONTROL_STR_ID          = "0xd028";
    String ISO_STR_ID                   = "0xd02a";
    String MOVIE_ISO_STR_ID             = "0xd02b";
    String FOCUS_POINT_STR_ID           = "0xd17c";
    String FOCUS_LOCK_STR_ID            = "0xd209";
    String DEVICE_ERROR_STR_ID          = "0xd21b";
    String SDCARD_REMAIN_SIZE_STR_ID    = "0xd229";
    String MOVIE_REMAINING_TIME_STR_ID  = "0xd22a";
    String SHUTTER_SPEED_STR_ID         = "0xd240";
    String IMAGE_ASPECT_STR_ID          = "0xd241";
    String BATTERY_LEVEL_2_STR_ID       = "0xd242";
}