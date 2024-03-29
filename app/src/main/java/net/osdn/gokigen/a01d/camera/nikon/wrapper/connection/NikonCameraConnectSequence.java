package net.osdn.gokigen.a01d.camera.nikon.wrapper.connection;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.command.messages.specific.NikonRegistrationMessage;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.status.NikonStatusChecker;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;

public class NikonCameraConnectSequence implements Runnable, IPtpIpCommandCallback, IPtpIpMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IPtpIpInterfaceProvider interfaceProvider;
    private final IPtpIpCommandPublisher commandIssuer;
    private final NikonStatusChecker statusChecker;
    private boolean isDumpLog = false;

    NikonCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IPtpIpInterfaceProvider interfaceProvider, @NonNull NikonStatusChecker statusChecker)
    {
        Log.v(TAG, " NikonCameraConnectSequence");
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
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_nikon), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_nikon));
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
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_nikon), false, true, Color.RED);
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
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.nikon_connect_connecting1), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_INIT_SESSION, 50, isDumpLog, 0, 0x1001, 0, 0, 0, 0, 0));  // GetDeviceInfo
                break;

            case SEQ_INIT_SESSION:
                if (checkEventInitialize(rx_body))
                {
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.nikon_connect_connecting2), false, false, 0);
                    commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION, 50, isDumpLog, 0, 0x1002, 4, 0x41, 0, 0, 0));  // OpenSession
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message));
                }
                break;
            case SEQ_OPEN_SESSION:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.nikon_connect_connecting3), false, false, 0);
                commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_CHANGE_REMOTE, 50, isDumpLog, 0, 0x902c, 4, 0x01, 0, 0, 0));  //
                break;

            case SEQ_CHANGE_REMOTE:
            case SEQ_SET_EVENT_MODE:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0);
                connectFinished();
                Log.v(TAG, "  CONNECT TO CAMERA : DONE.");
                break;

            default:
                Log.v(TAG, " RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void sendRegistrationMessage()
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new NikonRegistrationMessage(this));
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

            commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_OPEN_SESSION, 50, isDumpLog, 0, 0x1002, 4, 0x41, 0, 0, 0));
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

            //interfaceProvider.getAsyncEventCommunication().connect();
            //interfaceProvider.getCameraStatusWatcher().startStatusWatch(interfaceProvider.getStatusListener());  ステータスの定期確認は実施しない

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
