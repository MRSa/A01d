package net.osdn.gokigen.a01d.camera.olympuspen.wrapper.status;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.util.List;

import static net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper.dump_bytes;

public class OlympusPenCameraStatusWatcher implements ICameraStatusWatcher, ICameraStatus
{
    private final String TAG = toString();

    private byte[] buffer = null;
    private boolean isWatching = false;
    private boolean statusReceived = false;
    private final int SLEEP_TIME_MS = 250;
    private ICameraStatusUpdateNotify notifier = null;
    private int focusingStatus = 0;
    private static final int ID_FRAME_SIZE = 1;
    private static final int ID_AF_FRAME_INFO = 2;


    public OlympusPenCameraStatusWatcher()
    {

    }

    public void setRtpHeader(byte[] byteBuffer)
    {
        try
        {
            buffer = byteBuffer;
            statusReceived = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            statusReceived = false;
        }
    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier)
    {
        try
        {
            this.notifier = notifier;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    int waitMs = SLEEP_TIME_MS;
                    isWatching = true;
                    while (isWatching)
                    {
                        if (statusReceived)
                        {
                            // データを解析する
                            parseRtpHeader();
                            statusReceived = false;
                        }
                        sleep(waitMs);
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sleep(int waitMs)
    {
        try
        {
            Thread.sleep(waitMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void parseRtpHeader()
    {
        try
        {
            if (buffer == null)
            {
                Log.v(TAG, " parseRtpHeader() : null");
                return;
            }
            int position = 16;
            int maxLength = buffer.length;
            while ((position + 4) < maxLength)
            {
                int id = ((buffer[position] & 0xff) * 256) + (buffer[position + 1] & 0xff);
                int length = ((buffer[position + 2] & 0xff) * 256) + (buffer[position + 3] & 0xff);
                switch (id)
                {
                    case ID_AF_FRAME_INFO:
                        // 合焦状況の把握
                        checkFocused(buffer, position, length);
                        break;

                    case ID_FRAME_SIZE:
                    default:
                        // Log.v(TAG, " ID : " + id + "  LENGTH : " + length);
                        break;
                }
                position = position + 4 + (length * 4);  // header : 4bytes , data : length * 4 bytes
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void checkFocused(byte[] buffer, int position, int length)
    {
        if (length != 5)
        {
            // データがそろっていないので何もしない
            return;
        }
        int status = (buffer[position + 7] & 0xff);
        if (status != focusingStatus)
        {
            boolean focus = (status == 1);
            boolean isError = (status == 2);
            notifier.updateFocusedStatus(focus, isError);
            focusingStatus = status;
        }
    }

    @Override
    public void stopStatusWatch()
    {
        isWatching = false;
    }

    @Override
    public List<String> getStatusList(String key)
    {
        return null;
    }

    @Override
    public String getStatus(String key)
    {
        return null;
    }

    @Override
    public void setStatus(String key, String value)
    {

    }
}
