package net.osdn.gokigen.a01d.camera.fujix.wrapper.status;

import android.util.Log;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.ArrayList;
import java.util.List;

class FujiXStatusHolder
{
    private final String TAG = toString();
    private SparseIntArray statusHolder;

    FujiXStatusHolder()
    {
        statusHolder = new SparseIntArray();
        statusHolder.clear();
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
                updateDetected(notifier, id, currentValue, value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void updateDetected(ICameraStatusUpdateNotify notifier, int id, int previous, int current)
    {
        Log.v(TAG, "updateDetected(" + id + " " + previous + " -> " + current + " )");
    }

    List<String> getAvailableItemList(String listKey)
    {
        return (new ArrayList<>());
    }

    String getItemStatus(String key)
    {
        return ("");
    }

}
