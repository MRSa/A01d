package net.osdn.gokigen.a01d.camera.kodak.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.kodak.IKodakInterfaceProvider;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence01;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence02;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence03;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence04;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence05;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence06;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence07;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence08;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence09;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence10;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.connection.KodakConnectSequence11;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakFlashAuto;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakFlashOff;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakFlashOn;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.KodakStatusChecker;

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_FLASH_MODE;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_FLASH_MODE_DEFAULT_VALUE;

public class KodakCameraConnectSequence implements Runnable, IKodakCommandCallback, IKodakMessages
{
    private final String TAG = this.toString();

    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IKodakInterfaceProvider interfaceProvider;
    private final IKodakCommandPublisher commandIssuer;
    private final KodakStatusChecker statusChecker;

    private String flashMode = "OFF";

    KodakCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IKodakInterfaceProvider interfaceProvider, @NonNull KodakStatusChecker statusChecker)
    {
        Log.v(TAG, " KodakCameraConnectSequence");
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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (preferences != null)
            {
                flashMode = preferences.getString(KODAK_FLASH_MODE, KODAK_FLASH_MODE_DEFAULT_VALUE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            flashMode = "OFF";
        }

        try
        {
            // カメラとTCP接続
            IKodakCommandPublisher issuer = interfaceProvider.getCommandPublisher();
            if (!issuer.isConnected())
            {
                if (!interfaceProvider.getCommandCommunication().connect())
                {
                    // 接続失敗...
                    interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_kodak), false, true, Color.RED);
                    onConnectError(context.getString(R.string.dialog_title_connect_failed_kodak));
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
            startConnectSequence();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.dialog_title_connect_failed_kodak), false, true, Color.RED);
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        switch (id)
        {
            case SEQ_CONNECT_01:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting1), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence02(this));
                break;

            case SEQ_CONNECT_02:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting2), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence03(this));
                break;

            case SEQ_CONNECT_03:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting3), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence04(this));
                break;
            case SEQ_CONNECT_04:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting4), false, false, 0);
                // ここで、パスワードの Base64情報を切り出す(FC 03 の応答、 0x0058 ～ 64バイトの文字列を切り出して、Base64エンコードする)
                commandIssuer.enqueueCommand(new KodakConnectSequence05(this));
                break;
            case SEQ_CONNECT_05:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting5), false, false, 0);
                // ここで、パスワードの情報を切り出す (FE 03 の応答、 0x0078 ～ 文字列を切り出す。)
                commandIssuer.enqueueCommand(new KodakConnectSequence06(this));
                break;
            case SEQ_CONNECT_06:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting6), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence07(this));
                break;
            case SEQ_CONNECT_07:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting7), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence08(this));
                break;
            case SEQ_CONNECT_08:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting8), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence09(this));
                break;
            case SEQ_CONNECT_09:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting9), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence10(this));
                break;
            case SEQ_CONNECT_10:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting10), false, false, 0);
                commandIssuer.enqueueCommand(new KodakConnectSequence11(this));
                break;
            case SEQ_CONNECT_11:
                interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.kodak_connect_connecting11), false, false, 0);
                if (flashMode.contains("AUTO"))
                {
                    commandIssuer.enqueueCommand(new KodakFlashAuto(this));
                }
                else if (flashMode.contains("ON"))
                {
                    commandIssuer.enqueueCommand(new KodakFlashOn(this));
                }
                else
                {
                    commandIssuer.enqueueCommand(new KodakFlashOff(this));
                }
                break;
            case SEQ_FLASH_AUTO:
            case SEQ_FLASH_OFF:
            case SEQ_FLASH_ON:
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

    private void startConnectSequence()
    {
        interfaceProvider.getInformationReceiver().updateMessage(context.getString(R.string.connect_start), false, false, 0);
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
        commandIssuer.enqueueCommand(new KodakConnectSequence01(this));
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
