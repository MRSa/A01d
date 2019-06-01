package net.osdn.gokigen.a01d.camera.fujix.wrapper.command;

import android.util.Log;

import java.io.InputStreamReader;
import java.net.Socket;

public class FujiAsyncResponseReceiver
{
    private final String TAG = toString();
    private final String ipAddress;
    private final int portNumber;
    private static final int BUFFER_SIZE = 1280 + 8;
    private static final int WAIT_MS = 250;   // 250ms
    private static final int ERROR_LIMIT = 30;
    private boolean isStart = false;

    public FujiAsyncResponseReceiver(String ip, int portNumber)
    {
        this.ipAddress = ip;
        this.portNumber = portNumber;
    }

    public void start()
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

    public void stop()
    {
        isStart = false;
    }

    private void startReceive(Socket socket)
    {
        int errorCount = 0;
        InputStreamReader isr;
        char[] char_array;
        try
        {
            isr = new InputStreamReader(socket.getInputStream());
            char_array = new char[BUFFER_SIZE];

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "===== startReceive() aborted.");
            return;
        }
        Log.v(TAG, "startReceive() start.");
        while (isStart)
        {
            try
            {
                int read_bytes = isr.read(char_array, 0, BUFFER_SIZE);
                Log.v(TAG, "RECEIVE ASYNC  : " + read_bytes + " bytes.");
                //return (new ReceivedDataHolder(char_array, read_bytes));

                Thread.sleep(WAIT_MS);
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
        Log.v(TAG, "startReceive() end.");
    }
}
