package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.Arrays;
import java.util.Map;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_VIEWFRAME;

public class CanonLiveViewControl implements ILiveViewControl, ILiveViewListener, IPtpIpCommunication, ICanonLiveViewImageCallback
{
    private final String TAG = this.toString();
    private final IPtpIpCommandPublisher commandIssuer;
    private final int delayMs;
    private CanonLiveViewImageReceiver imageReceiver;
    private IImageDataReceiver dataReceiver = null;
    private boolean liveViewIsReceiving = false;
    private boolean commandIssued = false;

    public CanonLiveViewControl(@NonNull Activity context, @NonNull IPtpIpInterfaceProvider interfaceProvider, int delayMs)
    {
        this.commandIssuer = interfaceProvider.getCommandPublisher();
        this.delayMs = delayMs;
        this.imageReceiver = new CanonLiveViewImageReceiver(this);
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
                                commandIssued = true;
                                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(imageReceiver, SEQ_GET_VIEWFRAME, 10, false, 0, 0x9153, 12, 0x00200000, 0x01, 0x00, 0x00));
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
        // Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- ");
        try
        {
            if ((dataReceiver != null)&&(data != null))
            {
                //dataReceiver.setImageData(data, metadata);
                dataReceiver.setImageData(Arrays.copyOfRange(data, 8, data.length), metadata);
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
}
