package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OlympusPenLiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    private final CameraLiveViewListenerImpl liveViewListener;
    private DatagramSocket receiveSocket = null;
    private boolean whileStreamReceive = false;

    private static final int TIMEOUT_MAX = 3;
    private static final int RECEIVE_BUFFER_SIZE = 1024 * 1024 * 4;
    private static final int TIMEOUT_MS = 1500;
    private static final int LIVEVIEW_PORT = 49152;

    private final String COMMUNICATION_URL = "http://192.168.0.10/";
    private final String LIVEVIEW_START_REQUEST = "exec_takemisc.cgi?com=startliveview&port=49152";
    private final String LIVEVIEW_STOP_REQUEST = "exec_takemisc.cgi?com=stopliveview";

    private Map<String, String> headerMap;
    private ByteArrayOutputStream receivedByteStream;
    private byte[] rtpHeader;

    OlympusPenLiveViewControl()
    {
        liveViewListener = new CameraLiveViewListenerImpl();

        headerMap = new HashMap<>();
        headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
        headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

        receivedByteStream = new ByteArrayOutputStream(RECEIVE_BUFFER_SIZE);
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public void startLiveView()
    {
        Log.v(TAG, "startLiveView()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        startReceiveStream();
                        if (!whileStreamReceive)
                        {
                            Log.v(TAG, "CANNOT OPEN : UDP RECEIVE SOCKET");
                            return;
                        }
                        String requestUrl = COMMUNICATION_URL + LIVEVIEW_START_REQUEST;
                        String reply = SimpleHttpClient.httpGetWithHeader(requestUrl, headerMap, null, TIMEOUT_MS);
                        Log.v(TAG, "   ----- START LIVEVIEW ----- : " + requestUrl + " " + reply);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
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

    @Override
    public void stopLiveView()
    {
        Log.v(TAG, "stopLiveView()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String requestUrl = COMMUNICATION_URL + LIVEVIEW_STOP_REQUEST;
                        String reply = SimpleHttpClient.httpGetWithHeader(requestUrl, headerMap, null, TIMEOUT_MS);
                        Log.v(TAG, "stopLiveview() is issued.  " + reply);

                        //  ライブビューウォッチャーを止める
                        whileStreamReceive = false;
                        closeReceiveSocket();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
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
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    private void startReceiveStream()
    {
        if (whileStreamReceive)
        {
            Log.v(TAG, "startReceiveStream() : already starting.");
            return;
        }

        // ソケットをあける (UDP)
        try
        {
            receiveSocket = new DatagramSocket(LIVEVIEW_PORT);
            whileStreamReceive = true;
            receivedByteStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            whileStreamReceive = false;
            receiveSocket = null;
        }

        // 受信スレッドを動かす
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiverThread();
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

    private void checkReceiveImage(@NonNull DatagramPacket packet)
    {
        try
        {
            int dataLength = packet.getLength();
            byte[] receivedData = packet.getData();
            if (receivedData == null)
            {
                // 受信データが取れなかったのでいったん終了する
                Log.v(TAG, "RECEIVED DATA IS NULL...");
                return;
            }

            int position = 12;
            int extensionLength = 0;
            boolean isFinished = false;
            if (receivedData[0] == (byte) 0x90)
            {
                // 先頭パケット (RTPヘッダは 12bytes + 拡張ヘッダ...)
                //extensionLength = (receivedData[14]);
                //extensionLength = (extensionLength * 256) + (receivedData[15]);
                extensionLength = 16;
                extensionLength = checkJpegStartPosition(receivedData, extensionLength) - position;

                rtpHeader = Arrays.copyOf(receivedData, extensionLength);
                System.gc();
            }
            else if (receivedData[1] == (byte) 0xe0)
            {
                // 末尾パケット (RTPヘッダは 12bytes)
                isFinished = true;
            }

            int offset = position + extensionLength;
            receivedByteStream.write(receivedData, (position + extensionLength), (dataLength - offset));
            if (isFinished)
            {
                byte[] dataArray = receivedByteStream.toByteArray();
                receivedByteStream.flush();
                liveViewListener.onUpdateLiveView(Arrays.copyOf(dataArray, dataArray.length), null);
                receivedByteStream.reset();
                //System.gc();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int checkJpegStartPosition(byte[] bytes, int offset)
    {
        try
        {
            int position = offset;
            int maxLength = bytes.length - 1;
            while (position < maxLength)
            {
                if (bytes[position] == (byte) 0xff)
                {
                    if (bytes[position + 1] == (byte) 0xd8)
                    {
                        return (position);
                    }
                }
                position++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (0);
    }

    private void receiverThread()
    {
        int exceptionCount = 0;
        byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
        while (whileStreamReceive)
        {
            try
            {
                DatagramPacket receive_packet = new DatagramPacket(buffer, buffer.length);
                if (receiveSocket != null)
                {
                    receiveSocket.setSoTimeout(TIMEOUT_MS);
                    receiveSocket.receive(receive_packet);
                    checkReceiveImage(receive_packet);
                    exceptionCount = 0;
                }
                else
                {
                    Log.v(TAG, "receiveSocket is NULL...");
                }
            }
            catch (Exception e)
            {
                exceptionCount++;
                e.printStackTrace();
                if (exceptionCount > TIMEOUT_MAX)
                {
                    try
                    {
                        //  ライブビューの送信が来なくなった... それも回数が超えた...
                        Log.v(TAG, "LV : RETRY REQUEST");
                        exceptionCount = 0;
/*
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + LIVEVIEW_START_REQUEST, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "LV : RETRY COMMAND FAIL...");
                        }
*/
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
            }
        }
        closeReceiveSocket();
        Log.v(TAG, "  ----- startReceiveStream() : Finished.");
        System.gc();
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }

    private void closeReceiveSocket()
    {
        Log.v(TAG, "closeReceiveSocket()");
        try
        {
            if (receiveSocket != null)
            {
                Log.v(TAG, "  ----- SOCKET CLOSE -----  ");
                receiveSocket.close();
                receiveSocket = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
