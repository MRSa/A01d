package net.osdn.gokigen.a01d.camera.fujix.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXMessages;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.QueryCameraCapabilities;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.StatusRequestMessage;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.changemode.ChangeToPlayback1st;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.changemode.ChangeToPlayback2nd;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.changemode.ChangeToPlayback3rd;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.changemode.ChangeToPlayback4th;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.CameraRemoteMessage;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.RegistrationMessage;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.StartMessage;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.StartMessage2ndRead;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.StartMessage3rd;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.StartMessage4th;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.messages.start.StartMessage5th;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.status.IFujiXRunModeHolder;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class FujiXCameraConnectSequenceForPlayback implements Runnable, IFujiXCommandCallback, IFujiXMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IFujiXInterfaceProvider interfaceProvider;
    private final IFujiXCommandPublisher commandIssuer;
    private boolean isBothLiveView = false;

    FujiXCameraConnectSequenceForPlayback(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IFujiXInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, " FujiXCameraConnectSequenceForPlayback");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.interfaceProvider = interfaceProvider;
        this.commandIssuer = interfaceProvider.getCommandPublisher();
    }

    @Override
    public void run()
    {
        try
        {
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                isBothLiveView = preferences.getBoolean(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, false);
            }
            catch (Exception e)
            {
                //isBothLiveView = false;
                e.printStackTrace();
            }

            // カメラとTCP接続
            IFujiXCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    if (cameraStatusReceiver != null)
                    {
                        cameraStatusReceiver.onStatusNotify(context.getString(R.string.dialog_title_connect_failed_fuji));
                    }
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_fuji));
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
            if (cameraStatusReceiver != null)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.dialog_title_connect_failed_fuji));
            }
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
        //Log.v(TAG, "receivedMessage : " + id + "[" + rx_body.length + " bytes]");
        //int bodyLength = 0;
        IFujiXRunModeHolder runModeHolder;
        switch (id)
        {
            case SEQ_REGISTRATION:
                if (checkRegistrationMessage(rx_body))
                {
                    commandIssuer.enqueueCommand(new StartMessage(this));
                }
                else
                {
                    onConnectError(context.getString(R.string.connect_error_message_fuji));
                }
                break;

            case SEQ_START:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting1));
                }
                commandIssuer.enqueueCommand(new StartMessage2ndRead(this));
                break;

            case SEQ_START_2ND_READ:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting2));
                }
                if (rx_body.length == (int)rx_body[0])
                {
                    // なぜかもうちょっとデータが飛んでくるので待つ
                    //commandIssuer.enqueueCommand(new ReceiveOnly(this));

                    commandIssuer.enqueueCommand(new StartMessage3rd(this));
                }
                else
                {
                    commandIssuer.enqueueCommand(new StartMessage3rd(this));
                }
                break;

            case SEQ_START_2ND_RECEIVE:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting3));
                }
                commandIssuer.enqueueCommand(new StartMessage3rd(this));
                break;

            case SEQ_START_3RD:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting4));
                }
                commandIssuer.enqueueCommand(new StartMessage4th(this));
                break;

            case SEQ_START_4TH:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting5));
                }
                if (isBothLiveView)
                {
                    // カメラのLCDと遠隔のライブビューを同時に表示する場合...
                    commandIssuer.enqueueCommand(new CameraRemoteMessage(this));
                }
                else
                {
                    commandIssuer.enqueueCommand(new StartMessage5th(this));
                }
                break;

            case SEQ_START_5TH:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting6));
                }
                commandIssuer.enqueueCommand(new QueryCameraCapabilities(this));
                //commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_QUERY_CAMERA_CAPABILITIES:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting7));
                }
                commandIssuer.enqueueCommand(new CameraRemoteMessage(this));
                break;

            case SEQ_CAMERA_REMOTE:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting8));
                }
                commandIssuer.enqueueCommand(new ChangeToPlayback1st(this));
                runModeHolder = interfaceProvider.getRunModeHolder();
                if (runModeHolder != null)
                {
                    runModeHolder.transitToPlaybackMode(false);
                }
                //connectFinished();
                break;

            case SEQ_CHANGE_TO_PLAYBACK_1ST:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting9));
                }
                //commandIssuer.enqueueCommand(new ChangeToPlayback2nd(this));
                commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_2ND:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting10));
                }
                commandIssuer.enqueueCommand(new ChangeToPlayback3rd(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_3RD:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting11));
                }
                commandIssuer.enqueueCommand(new ChangeToPlayback4th(this));
                break;

            case SEQ_CHANGE_TO_PLAYBACK_4TH:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connecting12));
                }
                commandIssuer.enqueueCommand(new StatusRequestMessage(this));
                break;

            case SEQ_STATUS_REQUEST:
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connect_finished));
                }
                IFujiXCommandCallback callback = interfaceProvider.getStatusHolder();
                if (callback != null)
                {
                    callback.receivedMessage(id, rx_body);
                }
                runModeHolder = interfaceProvider.getRunModeHolder();
                if (runModeHolder != null)
                {
                    runModeHolder.transitToPlaybackMode(true);
                }
                connectFinished();
                Log.v(TAG, "CHANGED PLAYBACK MODE : DONE.");
                break;

            default:
                Log.v(TAG, "RECEIVED UNKNOWN ID : " + id);
                onConnectError(context.getString(R.string.connect_receive_unknown_message));
                break;
        }
    }

    private void sendRegistrationMessage()
    {
        if (cameraStatusReceiver != null)
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        }
        commandIssuer.enqueueCommand(new RegistrationMessage(this));
    }

    private boolean checkRegistrationMessage(byte[] receiveData)
    {
        // データがないときにはエラー
        if ((receiveData == null)||(receiveData.length < 8))
        {
            return (false);
        }

        // 応答エラーかどうかをチェックする
        if (receiveData.length == 8)
        {
            if ((receiveData[0] == 0x05) && (receiveData[1] == 0x00) && (receiveData[2] == 0x00) && (receiveData[3] == 0x00) &&
                    (receiveData[4] == 0x19) && (receiveData[5] == 0x20) && (receiveData[6] == 0x00) && (receiveData[7] == 0x00)) {
                // 応答エラー...
                if (cameraStatusReceiver != null)
                {
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.error_reply_from_camera));
                }
                return (false);
            }
            if (cameraStatusReceiver != null)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.other_error_reply_from_camera));
            }
            return (false);
        }
        if (cameraStatusReceiver != null)
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.registration_reply_from_camera));
        }
        return (true);
    }


    private void connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            if (cameraStatusReceiver != null)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
            }

            // ちょっと待つ
            Thread.sleep(1000);

            interfaceProvider.getAsyncEventCommunication().connect();
            //interfaceProvider.getCameraStatusWatcher().startStatusWatch(interfaceProvider.getStatusListener());  ステータスの定期確認は実施しない

            // 接続成功！のメッセージを出す
            if (cameraStatusReceiver != null)
            {
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
            }

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
                    Log.v(TAG, "onConnectNotify()");
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
