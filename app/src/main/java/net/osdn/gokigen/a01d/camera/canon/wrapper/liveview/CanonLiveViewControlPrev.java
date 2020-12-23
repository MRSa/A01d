package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGenericWithRetry;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.Arrays;
import java.util.Map;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_VIEWFRAME;

public class CanonLiveViewControlPrev implements ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback
{
    private final String TAG = this.toString();
    private final IPtpIpCommandPublisher commandIssuer;
    private final int delayMs;
    private final boolean isDumpLog = false;
    private final boolean isSearchJpegHeader;
    private final int retryCount = 1200;
    private final CanonLiveViewImageReceiver imageReceiver;
    private IImageDataReceiver dataReceiver = null;
    private boolean liveViewIsReceiving = false;
    private boolean commandIssued = false;

    public CanonLiveViewControlPrev(@NonNull Activity context, @NonNull IPtpIpInterfaceProvider interfaceProvider, int delayMs, boolean isSearchJpegHeader)
    {
        this.commandIssuer = interfaceProvider.getCommandPublisher();
        this.isSearchJpegHeader = isSearchJpegHeader;
        this.delayMs = delayMs;
        this.imageReceiver = new CanonLiveViewImageReceiver(context,this);
        Log.v(TAG, " -=-=-=-=-=- CanonLiveViewControl : delay " + delayMs + " ms");
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
                                if (isDumpLog)
                                {
                                    Log.v(TAG, " enqueueCommand() ");
                                }
                                if (commandIssuer.getCurrentQueueSize() < 3)
                                {
                                    commandIssued = true;
                                    commandIssuer.enqueueCommand(new PtpIpCommandGenericWithRetry(imageReceiver, SEQ_GET_VIEWFRAME, delayMs, retryCount, false, false, 0, 0x9153, 12, 0x00200000, 0x01, 0x00, 0x00));
                                }
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
    public void stopLiveView()
    {
        Log.v(TAG, " stopLiveView() ");
        liveViewIsReceiving = false;

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
            if ((dataReceiver != null)&&(data != null))
            {
                if (isDumpLog)
                {
                    Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- : " + data.length + " bytes.");
                }
                int headerSize = searchJpegHeader(data);
                if (headerSize >= 0)
                {
                    dataReceiver.setImageData(Arrays.copyOfRange(data, headerSize, data.length), metadata);  // ヘッダ部分を切り取って送る
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        commandIssued = false;
    }

    private int searchJpegHeader(byte[] data)
    {
        if (data.length <= 8)
        {
            return (-1);
        }
        if (!isSearchJpegHeader)
        {
            // JPEG ヘッダを探さない場合は、8バイト固定とする
            return (8);
        }
        try
        {
            int size = data.length - 1;
            int index = 0;
            while (index < size)
            {
                if ((data[index] == (byte) 0xff)&&(data[index + 1] == (byte) 0xd8))
                {
                    return (index);
                }
                index++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // 見つからなかったときは 8 を返す
        return (8);
    }

    @Override
    public void onErrorOccurred(Exception e)
    {
        Log.v(TAG, " onErrorOccurred () : " + e.getLocalizedMessage());
        commandIssued = false;
    }
}
