package net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE;


public class FujiXLiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    private final String ipAddress;
    private final int portNumber;
    private final CameraLiveViewListenerImpl liveViewListener;
    private int waitMs = 80;
    private static final int DATA_HEADER_OFFSET = 18;
    private static final int BUFFER_SIZE = 1280 * 1024 + 8;
    private static final int ERROR_LIMIT = 30;
    private boolean isStart = false;

    public FujiXLiveViewControl(@NonNull Activity activity, String ip, int portNumber)
    {
        this.ipAddress = ip;
        this.portNumber = portNumber;
        liveViewListener = new CameraLiveViewListenerImpl();

        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            String waitMsStr = preferences.getString(FUJIX_LIVEVIEW_WAIT, FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE);
            int wait = Integer.parseInt(waitMsStr);
            if ((wait >= 10)&&(wait <= 800))
            {
                waitMs = wait;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            waitMs = 80;
        }
        Log.v(TAG, "LOOP WAIT : " + waitMs + " ms");
    }

    @Override
    public void startLiveView()
    {
        if (isStart)
        {
            // すでに受信スレッド動作中なので抜ける
            return;
        }
        isStart = true;
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket socket = new Socket(ipAddress, portNumber);
                    startReceive(socket);
                }
                catch (Exception e)
                {
                    Log.v(TAG, " IP : " + ipAddress + " port : " + portNumber);
                    e.printStackTrace();
                }
            }
        });
        try
        {
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopLiveView()
    {
        isStart = false;
    }

    private void startReceive(Socket socket)
    {
        int errorCount = 0;
        InputStream isr;
        byte[] byteArray;
        try
        {
            isr = socket.getInputStream();
            byteArray = new byte[BUFFER_SIZE];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "===== startReceive() aborted.");
            return;
        }
        while (isStart)
        {
            try
            {
                int read_bytes = isr.read(byteArray, 0, BUFFER_SIZE);
                liveViewListener.onUpdateLiveView(Arrays.copyOfRange(byteArray, DATA_HEADER_OFFSET, read_bytes - DATA_HEADER_OFFSET), null);
                Thread.sleep(waitMs);
                errorCount = 0;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                errorCount++;
            }
            if (errorCount > ERROR_LIMIT)
            {
                // エラーが連続でたくさん出たらループをストップさせる
                isStart = false;
            }
        }
        try
        {
            isr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void updateDigitalZoom()
    {

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {

    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (1.0f);
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }
}
