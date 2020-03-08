package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.command.messages.specific.NikonLiveViewRequestMessage;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.Arrays;
import java.util.Map;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_AFDRIVE;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_CHECK_EVENT;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_DEVICE_READY;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_DEVICE_PROP1;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_DEVICE_PROP2;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_START_LIVEVIEW;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_STOP_LIVEVIEW;

public class NikonLiveViewControl  implements ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback, IPtpIpCommandCallback
{
    private final String TAG = this.toString();
    private final IPtpIpCommandPublisher commandIssuer;
    private final int delayMs;
    private NikonLiveViewImageReceiver imageReceiver;
    private IImageDataReceiver dataReceiver = null;
    private boolean liveViewIsReceiving = false;
    private boolean commandIssued = false;
    private boolean isDumpLog = false;

    public NikonLiveViewControl(@NonNull Activity context, @NonNull IPtpIpInterfaceProvider interfaceProvider, int delayMs)
    {
        this.commandIssuer = interfaceProvider.getCommandPublisher();
        this.delayMs = delayMs;
        this.imageReceiver = new NikonLiveViewImageReceiver(this);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (this);
    }

    @Override
    public void changeLiveViewSize(String size)
    {

    }

    @Override
    public void startLiveView()
    {
        Log.v(TAG, " startLiveView() ");
        try
        {
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_START_LIVEVIEW, 30, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopLiveView()
    {
        Log.v(TAG, " stopLiveView() ");
        try
        {
            if (liveViewIsReceiving)
            {
                liveViewIsReceiving = false;
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(new PtpIpResponseReceiver(null), SEQ_STOP_LIVEVIEW, 30, isDumpLog, 0, 0x9202, 0, 0x00, 0x00, 0x00, 0x00));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void startLiveviewImpl()
    {
        liveViewIsReceiving = true;
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        while (liveViewIsReceiving)
                        {
                            if (!commandIssued)
                            {
                                commandIssued = true;
                                commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 65, isDumpLog));
                            }
                            try
                            {
                                Thread.sleep(delayMs);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
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
        Log.v(TAG, " updateDigitalZoom() ");

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {
        Log.v(TAG, " updateMagnifyingLiveViewScale() ");
    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return 0;
    }

    @Override
    public float getDigitalZoomScale()
    {
        return 0;
    }

    @Override
    public void setCameraLiveImageView(IImageDataReceiver target)
    {
        Log.v(TAG, " setCameraLiveImageView() ");
        this.dataReceiver = target;
    }

    @Override
    public boolean connect()
    {
        Log.v(TAG, " connect() ");
        return (true);
    }

    @Override
    public void disconnect()
    {
        Log.v(TAG, " disconnect() ");
    }

    @Override
    public void onCompleted(byte[] data, Map<String, Object> metadata)
    {
        //Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- ");
        try
        {
            if ((dataReceiver != null)&&(data != null)&&(data.length > 0))
            {
                //Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- : " + data.length + " bytes.");
                //SimpleLogDumper.dump_bytes(" [LVLV] " + ": ", Arrays.copyOfRange(data, 0, (0 + 512)));
                //dataReceiver.setImageData(data, metadata);
                int offset = searchJpegHeader(data);
                if ((data.length > 8)&&(offset < data.length))
                {
                    dataReceiver.setImageData(Arrays.copyOfRange(data, offset, data.length), metadata);  // ヘッダ部分を切り取って送る
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        commandIssued = false;
        sendNextMessage();
    }

    private void sendNextMessage()
    {
        try
        {
            Thread.sleep(delayMs);
            //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_DEVICE_PROP1, 30, isDumpLog, 0, 0x1015, 4, 0x5007, 0x00, 0x00, 0x00));
            //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_CHECK_EVENT, 30, isDumpLog, 0, 0x90c7, 0, 0x00, 0x00, 0x00, 0x00));
            commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 35, isDumpLog));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private int searchJpegHeader(byte[] data)
    {
        try
        {
            int pos = 0;

            // 先頭の 1024bytesまで
            int limit = (data.length < 1024) ? (data.length - 1): 1024;
            while (pos < limit)
            {
                if ((data[pos] == (byte) 0xff)&&(data[pos + 1] == (byte) 0xd8))
                {

                    return (pos);
                }
                pos++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (384);
    }


    @Override
    public void onErrorOccurred(Exception e)
    {
        Log.v(TAG, " onErrorOccurred () : " + e.getLocalizedMessage());
        commandIssued = false;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " NikonLiveViewControl::receivedMessage() [id:" + id + "]");
        try
        {
            if (rx_body.length < 10)
            {
                Log.v(TAG, " NikonLiveViewControl::receivedMessage() : BODY LENGTH IS TOO SHORT. SEND RETRY MESSAGE");
                retrySendMessage(id);
                return;
            }
/**/
            int responseCode = (rx_body[8] & 0xff) + ((rx_body[9] & 0xff) * 256);
            if (id == SEQ_CHECK_EVENT)
            {
                // 応答にはデータが含まれているので....受信データの末尾を拾う
                //SimpleLogDumper.dump_bytes("CHECK EVENT", Arrays.copyOfRange(rx_body, 0, Math.min(rx_body.length, 128)));
                responseCode = (rx_body[rx_body.length - 6] & 0xff) + ((rx_body[rx_body.length - 5] & 0xff) * 256);
            }
/**/
            //int responseCode = (rx_body[rx_body.length - 6] & 0xff) + ((rx_body[rx_body.length - 5] & 0xff) * 256);
            if (responseCode != 0x2001)
            {
                // NG応答を受信...同じコマンドを再送する
                Log.v(TAG, String.format(" RECEIVED NG REPLY ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode));
                retrySendMessage(id);
                return;
            }

            Log.v(TAG, String.format(" ----- OK REPLY (ID : %d) ----- ", id));
            if (id == SEQ_START_LIVEVIEW)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_READY, 30, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_DEVICE_READY)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_AFDRIVE, 30, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_CHECK_EVENT)
            {
                commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 35, isDumpLog));
            }
            else if (id == SEQ_GET_DEVICE_PROP1)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_DEVICE_PROP2, 30, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_GET_DEVICE_PROP2)
            {
                commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 35, isDumpLog));
            }
            else  // SEQ_AFDRIVE
            {
                // ライブビューの開始。
                //startLiveviewImpl();
                commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 80, isDumpLog));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void waitSleep()
    {
        try
        {
            Thread.sleep(delayMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void retrySendMessage(int id)
    {
        try
        {
            waitSleep();
            if (id == SEQ_START_LIVEVIEW)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_START_LIVEVIEW, 30, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_DEVICE_READY)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_READY, 30, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_CHECK_EVENT)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_CHECK_EVENT, 30, isDumpLog, 0, 0x90c7, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_GET_DEVICE_PROP1)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_DEVICE_PROP1, 30, isDumpLog, 0, 0x5007, 4, 0x5007, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_GET_DEVICE_PROP2)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_DEVICE_PROP2, 30, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00));
            }
            else
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_AFDRIVE, 30, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        //
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
