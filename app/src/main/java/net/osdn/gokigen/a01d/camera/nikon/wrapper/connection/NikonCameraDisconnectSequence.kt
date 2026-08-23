package net.osdn.gokigen.a01d.camera.nikon.wrapper.connection

import android.util.Log
import net.osdn.gokigen.a01d.camera.nikon.wrapper.status.NikonStatusChecker
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication

// ニコンカメラとの接続を安全に終了させるシーケンスクラス
class NikonCameraDisconnectSequence(
    interfaceProvider: IPtpIpInterfaceProvider,
    private val statusChecker: NikonStatusChecker
) : Runnable
{
    private val command: IPtpIpCommunication? = interfaceProvider.getCommandCommunication()
    private val async: IPtpIpCommunication? = interfaceProvider.getAsyncEventCommunication()
    private val liveview: IPtpIpCommunication? = interfaceProvider.getLiveviewCommunication()

    override fun run() {
        Log.d(TAG, "Nikon camera disconnect sequence started.")

        // --- ステータス監視の停止
        try
        {
            statusChecker.stopStatusWatch()
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Failed to stop status watch", e)
        }

        // --- LiveView 通信の切断
        closeQuietly(liveview, "Liveview communication")

        // --- Async イベント通信の切断
        closeQuietly(async, "Async communication")

        // --- Command 通信の切断
        closeQuietly(command, "Command communication")

        Log.d(TAG, "Nikon camera disconnect sequence completed.")
    }

    // 切断処理中に例外が発生しても後続処理に影響させないためのユーティリティメソッド
    private fun closeQuietly(communication: IPtpIpCommunication?, label: String?) {
        if (communication == null) {
            return
        }

        try {
            communication.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect $label", e)
        }
    }

    companion object {
        private val TAG: String = NikonCameraDisconnectSequence::class.java.getSimpleName()
    }
}

/*
class NikonCameraDisconnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity activity;
    private final IPtpIpCommunication command;
    private final IPtpIpCommunication async;
    private final IPtpIpCommunication liveview;
    private final NikonStatusChecker statusChecker;

    NikonCameraDisconnectSequence(Activity activity, @NonNull IPtpIpInterfaceProvider interfaceProvider, @NonNull NikonStatusChecker statusChecker)
    {
        this.activity = activity;
        this.command = interfaceProvider.getCommandCommunication();
        this.async = interfaceProvider.getAsyncEventCommunication();
        this.liveview = interfaceProvider.getLiveviewCommunication();
        this.statusChecker = statusChecker;
    }

    @Override
    public void run()
    {
        try
        {
            statusChecker.stopStatusWatch();
            liveview.disconnect();
            async.disconnect();
            command.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
*/
