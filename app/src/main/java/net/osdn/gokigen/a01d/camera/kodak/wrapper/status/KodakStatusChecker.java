package net.osdn.gokigen.a01d.camera.kodak.wrapper.status;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.ArrayList;
import java.util.List;

public class KodakStatusChecker implements IKodakCommandCallback, ICameraStatusWatcher, ICameraStatus
{
    private final String TAG = toString();
    private KodakStatusHolder statusHolder;
    private boolean whileFetching = false;
    private ICameraStatusUpdateNotify notifier = null;

    public KodakStatusChecker()
    {
        this.statusHolder = new KodakStatusHolder();
    }


    @Override
    public List<String> getStatusList(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return (new ArrayList<>());
            }
            return (statusHolder.getAvailableItemList(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new ArrayList<>());
    }

    @Override
    public String getStatus(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return ("");
            }
            return (statusHolder.getItemStatus(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public void setStatus(String key, String value)
    {
        Log.v(TAG, "setStatus(" + key + ", " + value + ")");
    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier)
    {
        if (whileFetching)
        {
            Log.v(TAG, "startStatusWatch() already starting.");
            return;
        }
        try
        {
            this.notifier = notifier;
            whileFetching = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopStatusWatch()
    {
        Log.v(TAG, "stoptStatusWatch()");
        whileFetching = false;
        this.notifier = null;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {

    }
}
