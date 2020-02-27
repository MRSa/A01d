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
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.Arrays;
import java.util.Map;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_AFDRIVE;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_DEVICE_READY;
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
    private boolean isDumpLog = true;

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
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_START_LIVEVIEW, 20, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00));
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
                                commandIssuer.enqueueCommand(new NikonLiveViewRequestMessage(imageReceiver, 90, isDumpLog));
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
                int offset = 384;
                if (data.length > 8)
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
        Log.v(TAG, " NikonLiveViewControl::receivedMessage() : ");
        try
        {
            if (rx_body.length < 10)
            {
                retrySendMessage(id);
                return;
            }
            int responseCode = (rx_body[8] & 0xff) + ((rx_body[9] & 0xff) * 256);
            if (responseCode != 0x2001)
            {
                // NG応答を受信...同じコマンドを再送する
                Log.v(TAG, String.format(" RECEIVED NG REPLY ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode));
                retrySendMessage(id);
                return;
            }

            Log.v(TAG, String.format(" OK REPLY (ID : %d) ", id));
            if (id == SEQ_START_LIVEVIEW)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_READY, 20, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_DEVICE_READY)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_AFDRIVE, 20, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else
            {
                // ライブビューの開始。
                startLiveviewImpl();
            }
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
            if (id == SEQ_START_LIVEVIEW)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_START_LIVEVIEW, 20, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else if (id == SEQ_DEVICE_READY)
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_READY, 20, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00));
            }
            else
            {
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_AFDRIVE, 20, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00));
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
