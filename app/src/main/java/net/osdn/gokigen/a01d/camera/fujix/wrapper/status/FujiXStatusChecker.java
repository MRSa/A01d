package net.osdn.gokigen.a01d.camera.fujix.wrapper.status;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommand;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.StatusRequestReceive;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.ArrayList;
import java.util.List;

public class FujiXStatusChecker implements IFujiXCommandCallback, ICameraStatusWatcher, ICameraStatus
{
    private final String TAG = toString();
    private static final int STATUS_MESSAGE_HEADER_SIZE = 14;
    private final int sleepMs;
    private final IFujiXCommandIssuer issuer;
    private ICameraStatusUpdateNotify notifier = null;
    private FujiXStatusHolder statusHolder;
    private boolean whileFetching = false;

    public FujiXStatusChecker(@NonNull IFujiXCommandIssuer issuer, int sleepMs)
    {
        this.issuer = issuer;
        this.statusHolder = new FujiXStatusHolder();
        this.sleepMs = sleepMs;
    }

    @Override
    public void receivedMessage(int id, byte[] data)
    {
        try
        {
            if (data.length < STATUS_MESSAGE_HEADER_SIZE)
            {
                Log.v(TAG, "received status length is short. (" + data.length + " bytes.)");
                return;
            }

            int nofStatus = (data[13] * 256) + data[12];
            int statusCount = 0;
            int index = STATUS_MESSAGE_HEADER_SIZE;
            while ((statusCount < nofStatus)&&(index < data.length))
            {
                int dataId = ((((int)data[index + 1]) & 0xff) * 256) + (((int) data[index]) & 0xff);
                statusHolder.updateValue(notifier, dataId, data[index + 2], data[index + 3], data[index +4], data[index + 5]);
                index = index + 6;
                statusCount++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
            String listKey = key + "List";
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
        try
        {
            Log.v(TAG, "setStatus(" + key + ", " + value + ")");

            // ここで設定を行う。
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier)
    {
        Log.v(TAG, "startStatusWatch()");
        if (whileFetching)
        {
            Log.v(TAG, "startStatusWatch() already starting.");
            return;
        }
        try
        {
            this.notifier = notifier;
            whileFetching = true;
            final IFujiXCommand command = new StatusRequestReceive(this);
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d(TAG, "Start status watch.");
                    while (whileFetching)
                    {
                        try
                        {
                            issuer.enqueueCommand(command);
                            Thread.sleep(sleepMs);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    Log.v(TAG, "STATUS WATCH STOPPED.");
                }
            });
            thread.start();
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
}
