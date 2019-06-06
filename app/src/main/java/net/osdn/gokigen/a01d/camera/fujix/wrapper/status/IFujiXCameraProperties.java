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
}
