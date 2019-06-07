package net.osdn.gokigen.a01d.camera.fujix.wrapper.status;

import android.util.Log;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class FujiXStatusHolder implements IFujiXCameraProperties
{
    private final String TAG = toString();
    private SparseIntArray statusHolder;
    private SparseArrayCompat<String> statusNameArray;

    FujiXStatusHolder()
    {
        statusHolder = new SparseIntArray();
        statusHolder.clear();

        statusNameArray = new SparseArrayCompat<>();
        prepareStatusNameArray();
    }

    private void prepareStatusNameArray()
    {
        statusNameArray.clear();
        statusNameArray.append(BATTERY_LEVEL, "Battery");
        statusNameArray.append(WHITE_BALANCE, "WhiteBalance");
        statusNameArray.append(APERTURE, "Aperture");
        statusNameArray.append(FOCUS_MODE, "FocusMode");
        statusNameArray.append(SHOOTING_MODE, "ShootingMode");
        statusNameArray.append(FLASH, "FlashMode");
        statusNameArray.append(EXPOSURE_COMPENSATION, "ExposureCompensation");
        statusNameArray.append(SELF_TIMER, "SelfTimer");
        statusNameArray.append(FILM_SIMULATION, "FilmSimulation");
        statusNameArray.append(IMAGE_FORMAT, "ImageFormat");
        statusNameArray.append(RECMODE_ENABLE, "RecModeEnable");
        statusNameArray.append(F_SS_CONTROL, "F_SS_Control");
        statusNameArray.append(ISO, "Iso");
        statusNameArray.append(MOVIE_ISO, "MovieIso");
        statusNameArray.append(FOCUS_POINT, "FocusPoint");
        statusNameArray.append(DEVICE_ERROR, "DeviceError");
        statusNameArray.append(SDCARD_REMAIN_SIZE, "ImageRemainCount");
        statusNameArray.append(FOCUS_LOCK, "FocusLock");
        statusNameArray.append(MOVIE_REMAINING_TIME, "MovieRemainTime");
        statusNameArray.append(SHUTTER_SPEED, "ShutterSpeed");
        statusNameArray.append(IMAGE_ASPECT, "ImageAspect");
        statusNameArray.append(BATTERY_LEVEL_2, "BattLevel");
    }


    void updateValue(ICameraStatusUpdateNotify notifier, int id, byte data0, byte data1, byte data2, byte data3)
    {
        try
        {
            int value = ((((int) data3) & 0xff) << 24) + ((((int) data2) & 0xff) << 16) + ((((int) data1) & 0xff) << 8) + (((int) data0) & 0xff);
            int currentValue = statusHolder.get(id, -1);
            //Log.v(TAG, "STATUS  ID: " + id + "  value : " + value + " (" + currentValue + ")");
            statusHolder.put(id, value);
            if (currentValue != value)
            {
                Log.v(TAG, "STATUS  ID: " + id + " value : " + currentValue + " -> " + value);
                if (notifier != null)
                {
                    updateDetected(notifier, id, currentValue, value);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateDetected(@NonNull ICameraStatusUpdateNotify notifier, int id, int previous, int current)
    {
        try
        {
            String idName = statusNameArray.get(id, "Unknown");
            Log.v(TAG, "updateDetected(" + id + " [" + idName + "] " + previous + " -> " + current + " )");

            if (id == FOCUS_LOCK)
            {
                if (current == 1)
                {
                    // focus Lock
                    notifier.updateFocusedStatus(true, true);
                }
                else
                {
                    // focus unlock
                    notifier.updateFocusedStatus(false, false);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   認識したカメラのステータス名称のリストを応答する
     *
     */
    private List<String> getAvailableStatusNameList()
    {
        ArrayList<String> selection = new ArrayList<>();
        try
        {
            for (int index = 0; index < statusHolder.size(); index++)
            {
                int key = statusHolder.keyAt(index);
                selection.add(statusNameArray.get(key));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (selection);

    }



    List<String> getAvailableItemList(String listKey)
    {
        if (listKey == null)
        {
            // アイテム名の一覧を応答する
            return (getAvailableStatusNameList());
        }

        ArrayList<String> selection = new ArrayList<>();
        try
        {
            for (int index = 0; index < statusHolder.size(); index++)
            {
                int key = statusHolder.keyAt(index);
                selection.add(statusNameArray.get(key));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (selection);
    }

    String getItemStatus(String key)
    {
        try
        {
            for (int index = 0; index < statusNameArray.size(); index++)
            {
                int id = statusNameArray.keyAt(index);
                String strKey = statusNameArray.valueAt(index);
                if (key.contentEquals(strKey))
                {
                    int value = statusHolder.get(id);
                    return (String.format(Locale.US,"0x%08x (%d)", value, value));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("?");
    }
}
