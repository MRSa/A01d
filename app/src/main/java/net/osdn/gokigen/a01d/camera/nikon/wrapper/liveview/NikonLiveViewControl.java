package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.Arrays;
import java.util.Map;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_AFDRIVE;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_DEVICE_READY;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_VIEWFRAME;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_START_LIVEVIEW;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_STOP_LIVEVIEW;

public class NikonLiveViewControl  implements ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback
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
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(new PtpIpResponseReceiver(), SEQ_START_LIVEVIEW, 20, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00));
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(new PtpIpResponseReceiver(), SEQ_DEVICE_READY, 20, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00));
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(new PtpIpResponseReceiver(), SEQ_AFDRIVE, 20, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00));
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
        try
        {
            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(new PtpIpResponseReceiver(), SEQ_STOP_LIVEVIEW, 20, isDumpLog, 0, 0x9202, 0, 0x00, 0x00, 0x00, 0x00));
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
                                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(imageReceiver, SEQ_GET_VIEWFRAME, 20, false, 0, 0x9203, 0, 0x00, 0x00, 0x00, 0x00));
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
            if ((dataReceiver != null)&&(data != null))
            {
                //Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- : " + data.length + " bytes.");
                //dataReceiver.setImageData(data, metadata);
                if (data.length > 8)
                {
                    dataReceiver.setImageData(Arrays.copyOfRange(data, 8, data.length), metadata);  // ヘッダ部分を切り取って送る
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
}
