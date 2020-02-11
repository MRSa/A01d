package net.osdn.gokigen.a01d.camera.canon.wrapper.connection;


import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific.CanonSetDevicePropertyValue;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific.CanonRegistrationMessage;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.canon.wrapper.status.CanonStatusChecker;

public class CanonCameraConnectSequence implements Runnable, IPtpIpCommandCallback, IPtpIpMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IPtpIpInterfaceProvider interfaceProvider;
    private final IPtpIpCommandPublisher commandIssuer;
    private final CanonStatusChecker statusChecker;
    private boolean isDumpLog = false;

    CanonCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IPtpIpInterfaceProvider interfaceProvider, @NonNull CanonStatusChecker statusChecker)
    {
        Log.v(TAG, " CanonCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.interfaceProvider = interfaceProvider;
        this.commandIssuer = interfaceProvider.getCommandPublisher();
        this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            // カメラとTCP接続
            IPtpIpCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_canon));
                    return;
                }
            }
            else
            {
                Log.v(TAG, "SOCKET IS ALREADY CONNECTED...");
            }
            // コマンドタスクの実行開始
            issuer.start();

            // 接続シーケンスの開始
            sendRegistrationMessage();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED);
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        switch (id)
        {
            case SEQ_REGISTRATION:
                if (checkRegistrationMessage(rx_body))
                {
                    sendInitEventRequest(rx_body);
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;

            case SEQ_EVENT_INITIALIZE:
                if (checkEventInitialize(rx_body))
                {
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting1), false, false, 0);
                    commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41));
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;

            case SEQ_OPEN_SESSION:
                Log.v(TAG, " SEQ_OPEN_SESSION ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting2), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_INIT_SESSION, isDumpLog, 0, 0x902f));
                break;

            case SEQ_INIT_SESSION:
                Log.v(TAG, " SEQ_INIT_SESSION ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting3), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_CHANGE_REMOTE, isDumpLog, 0, 0x9114, 4, 0x15));
                break;

            case SEQ_CHANGE_REMOTE:
                Log.v(TAG, " SEQ_CHANGE_REMOTE ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting4), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_SET_EVENT_MODE, isDumpLog, 0, 0x9115, 4, 0x02));
                break;

            case SEQ_SET_EVENT_MODE:
                Log.v(TAG, " SEQ_SET_EVENT_MODE ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting5), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_EVENT, isDumpLog, 0, 0x913d, 4, 0x0fff));
                break;

            case SEQ_GET_EVENT:
                Log.v(TAG, " SEQ_GET_EVENT ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting6), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_EVENT1, isDumpLog, 0, 0x9033, 4, 0x00000000));
                //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_EVENT1, isDumpLog, 0, 0x9033, 4, 0x00200000));
                break;

            case SEQ_GET_EVENT1:
                Log.v(TAG, " SEQ_GET_EVENT1 ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting7), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_INFORMATION, isDumpLog, 0, 0x1001));
                break;

            case SEQ_DEVICE_INFORMATION:
                Log.v(TAG, " SEQ_DEVICE_INFORMATION ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting8), false, false, 0);
                //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1a6));
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1a6));
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d169));
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d16a));
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d16b));
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1af));
                break;

            case SEQ_DEVICE_PROPERTY:
                Log.v(TAG, " SEQ_DEVICE_PROPERTY ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting9), false, false, 0);
                if ((rx_body[8] == (byte) 0x01)&&(rx_body[9] == (byte) 0x20))
                {
                    // コマンドが受け付けられたときだけ次に進む！
                    try
                    {
                        // ちょっと(250ms)待つ
                        Thread.sleep(250);

                        // コマンド発行
                        commandIssuer.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_SET_DEVICE_PROPERTY, isDumpLog, 0, 150, 0xd136, 0x00));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                break;

            case SEQ_SET_DEVICE_PROPERTY:
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting10), false, false, 0);
                commandIssuer.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_SET_DEVICE_PROPERTY_2, isDumpLog, 0, 150, 0xd136, 0x01));
                break;

            case SEQ_SET_DEVICE_PROPERTY_2:
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY_2 ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting11), false, false, 0);
                commandIssuer.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_SET_DEVICE_PROPERTY_3, isDumpLog, 0, 150, 0xd136, 0x00));
                break;

            case SEQ_SET_DEVICE_PROPERTY_3:
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY_3 ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.canon_connect_connecting12), false, false, 0);
                commandIssuer.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_DEVICE_PROPERTY_FINISHED, isDumpLog, 0, 300, 0xd1b0, 0x08));
                break;

            case SEQ_DEVICE_PROPERTY_FINISHED:
                Log.v(TAG, " SEQ_DEVICE_PROPERTY_FINISHED ");
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0);
                connectFinished();
                Log.v(TAG, "CHANGED MODE : DONE.");
                break;

            default:
                Log.v(TAG, "RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void sendRegistrationMessage()
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new CanonRegistrationMessage(this));
    }

    private void sendInitEventRequest(byte[] receiveData)
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start_2), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start_2));
        try
        {
            int eventConnectionNumber = (receiveData[8] & 0xff);
            eventConnectionNumber = eventConnectionNumber + ((receiveData[9]  & 0xff) << 8);
            eventConnectionNumber = eventConnectionNumber + ((receiveData[10] & 0xff) << 16);
            eventConnectionNumber = eventConnectionNumber + ((receiveData[11] & 0xff) << 24);
            statusChecker.setEventConnectionNumber(eventConnectionNumber);
            interfaceProvider.getCameraStatusWatcher().startStatusWatch(interfaceProvider.getStatusListener());

            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkRegistrationMessage(byte[] receiveData)
    {
        // データ(Connection Number)がないときにはエラーと判断する
        return (!((receiveData == null)||(receiveData.length < 12)));
    }

    private boolean checkEventInitialize(byte[] receiveData)
    {
        Log.v(TAG, "checkEventInitialize() ");
        return (!(receiveData == null));
    }

    private void connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connected), false, false, 0);

            // ちょっと待つ
            Thread.sleep(1000);

            // 接続成功！のメッセージを出す
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connected), false, false, 0);

            onConnectNotify();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onConnectNotify()
    {
        try
        {
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
                    cameraStatusReceiver.onCameraConnected();
                    Log.v(TAG, " onConnectNotify()");
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
