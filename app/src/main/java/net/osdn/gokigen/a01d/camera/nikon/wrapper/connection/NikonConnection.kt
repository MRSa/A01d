package net.osdn.gokigen.a01d.camera.nikon.wrapper.connection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import net.osdn.gokigen.a01d.R
import net.osdn.gokigen.a01d.camera.ICameraConnection
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver
import net.osdn.gokigen.a01d.camera.nikon.wrapper.status.NikonStatusChecker
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NikonConnection(
    activity: Activity,
    private val statusReceiver: ICameraStatusReceiver,
    private val interfaceProvider: IPtpIpInterfaceProvider,
    statusChecker: NikonStatusChecker
) : ICameraConnection {

    // メモリリーク防止のため ApplicationContext を保持
    private val appContext: Context = activity.applicationContext

    // UI (AlertDialog) 表示用に Activity を WeakReference で保持
    private val activityRef = WeakReference(activity)

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @Volatile
    private var connectionStatus: ICameraConnection.CameraConnectionStatus? = ICameraConnection.CameraConnectionStatus.UNKNOWN

    // 監視状態フラグ・オブジェクト
    private var isReceiverRegistered = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val connectSequence = NikonCameraConnectSequence(
        appContext,
        statusReceiver,
        this,
        interfaceProvider,
        statusChecker
    )
    private val disconnectSequence = NikonCameraDisconnectSequence(interfaceProvider, statusChecker)

    // API 20 以下用の BroadcastReceiver
    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onReceiveBroadcastOfConnection(context, intent)
        }
    }

    /**
     * Wi-Fi ネットワーク状態の監視を開始
     * API 21 以上: NetworkCallback
     * API 20 以下: BroadcastReceiver
     */
    override fun startWatchWifiStatus(context: Context) {
        Log.v(TAG, "startWatchWifiStatus()")

        interfaceProvider.getInformationReceiver()
            .updateMessage(appContext.getString(R.string.connect_prepare), false, false, 0)
        statusReceiver.onStatusNotify("prepare")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startWatchWifiStatusApi21()
        } else {
            startWatchWifiStatusLegacy()
        }
    }

    /**
     * API 21 以上向けの NetworkCallback 登録
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startWatchWifiStatusApi21() {
        if (networkCallback != null) {
            Log.w(TAG, "NetworkCallback is already registered.")
            return
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.v(TAG, "NetworkCallback: Wi-Fi Available")
                notifyWifiChecking()
                connectToCamera()
            }

            override fun onLost(network: Network) {
                Log.v(TAG, "NetworkCallback: Wi-Fi Lost")
                disconnect(false)
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    /**
     * API 20 以下（minSdk 14〜20）向けの BroadcastReceiver 登録
     */
    private fun startWatchWifiStatusLegacy() {
        if (isReceiverRegistered) {
            Log.w(TAG, "connectionReceiver is already registered.")
            return
        }

        val filter = IntentFilter().apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            @Suppress("DEPRECATION")
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }

        try {
            appContext.registerReceiver(connectionReceiver, filter)
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register connectionReceiver", e)
        }
    }

    /**
     * 監視を停止（両 API レベルに対応）
     */
    override fun stopWatchWifiStatus(context: Context) {
        Log.v(TAG, "stopWatchWifiStatus()")

        // API 21+ NetworkCallback の解除
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback?.let {
                try {
                    connectivityManager.unregisterNetworkCallback(it)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to unregister network callback", e)
                }
                networkCallback = null
            }
        }

        // API 20- BroadcastReceiver の解除
        if (isReceiverRegistered) {
            try {
                appContext.unregisterReceiver(connectionReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister connectionReceiver", e)
            } finally {
                isReceiverRegistered = false
            }
        }

        disconnect(false)
    }

    /**
     * API 20 以下用の Broadcast 受信ハンドラー
     */
    private fun onReceiveBroadcastOfConnection(context: Context, intent: Intent) {
        notifyWifiChecking()

        val action = intent.action ?: run {
            Log.v(TAG, "intent.action is null")
            return
        }

        try {
            @Suppress("DEPRECATION")
            if (action == ConnectivityManager.CONNECTIVITY_ACTION || action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                @Suppress("DEPRECATION")
                val activeNetwork: NetworkInfo? = cm?.activeNetworkInfo

                @Suppress("DEPRECATION")
                if (activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnected) {
                    Log.v(TAG, "Legacy Receiver: Wi-Fi connected.")
                    connectToCamera()
                } else {
                    Log.v(TAG, "Legacy Receiver: Wi-Fi is not connected.")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "onReceiveBroadcastOfConnection EXCEPTION: ${e.message}", e)
        }
    }

    private fun notifyWifiChecking() {
        interfaceProvider.getInformationReceiver()
            .updateMessage(appContext.getString(R.string.connect_check_wifi), false, false, 0)
        statusReceiver.onStatusNotify(appContext.getString(R.string.connect_check_wifi))
    }

    override fun disconnect(powerOff: Boolean) {
        Log.v(TAG, "disconnect(): powerOff=$powerOff")
        disconnectFromCamera(powerOff)
        connectionStatus = ICameraConnection.CameraConnectionStatus.DISCONNECTED
        statusReceiver.onCameraDisconnected()
    }

    override fun connect() {
        Log.v(TAG, "connect()")
        connectToCamera()
    }

    override fun alertConnectingFailed(message: String?) {
        Log.v(TAG, "alertConnectingFailed(): $message")

        val currentActivity = activityRef.get() ?: run {
            Log.w(TAG, "Activity context is lost. Cannot show alert dialog.")
            return
        }

        // API 17 未満への安全対策（isDestroyed は API 17〜）
        val isDestroyed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            currentActivity.isDestroyed
        } else {
            false
        }

        if (currentActivity.isFinishing || isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed. Cannot show alert dialog.")
            return
        }

        currentActivity.runOnUiThread {
            try {
                AlertDialog.Builder(currentActivity)
                    .setTitle(R.string.dialog_title_connect_failed_nikon)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_title_button_retry) { _, _ ->
                        disconnect(false)
                        connect()
                    }
                    .setNeutralButton(R.string.dialog_title_button_network_settings) { _, _ ->
                        try {
                            currentActivity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        } catch (ex: ActivityNotFoundException) {
                            Log.v(TAG, "ActivityNotFoundException for Wi-Fi settings : ${ex.localizedMessage}")
                            connect()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to open Wi-Fi settings", e)
                        }
                    }
                    .show()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing dialog", e)
            }
        }
    }

    override fun getConnectionStatus(): ICameraConnection.CameraConnectionStatus? {
        Log.v(TAG, "getConnectionStatus()")
        return connectionStatus
    }

    override fun forceUpdateConnectionStatus(status: ICameraConnection.CameraConnectionStatus?) {
        Log.v(TAG, "forceUpdateConnectionStatus(): $status")
        connectionStatus = status
    }

    private fun disconnectFromCamera(powerOff: Boolean) {
        Log.v(TAG, "disconnectFromCamera(): $powerOff")
        try {
            cameraExecutor.execute(disconnectSequence)
        } catch (e: Exception) {
            Log.e(TAG, "Execution failed in disconnectFromCamera", e)
        }
    }

    private fun connectToCamera() {
        Log.v(TAG, "connectToCamera()")
        connectionStatus = ICameraConnection.CameraConnectionStatus.CONNECTING
        try {
            cameraExecutor.execute(connectSequence)
        } catch (e: Exception) {
            Log.e(TAG, "Execution failed in connectToCamera", e)
        }
    }

    // オブジェクト破棄時や終了時に呼び出して Executor を解放する
    fun release() {
        cameraExecutor.shutdown()
    }

    companion object {
        private val TAG: String = NikonConnection::class.java.simpleName
    }
}
/*
class NikonConnection(
    context: Activity,
    statusReceiver: ICameraStatusReceiver,
    interfaceProvider: IPtpIpInterfaceProvider,
    statusChecker: NikonStatusChecker
) : ICameraConnection {
    private val TAG = toString()
    private val context: Activity
    private val statusReceiver: ICameraStatusReceiver
    private val interfaceProvider: IPtpIpInterfaceProvider
    private val connectionReceiver: BroadcastReceiver
    private val cameraExecutor: Executor = Executors.newFixedThreadPool(1)

    private var connectionStatus: CameraConnectionStatus? = CameraConnectionStatus.UNKNOWN

    private val connectSequence: NikonCameraConnectSequence
    private val disconnectSequence: NikonCameraDisconnectSequence

    init {
        Log.v(TAG, "NikonConnection()")
        this.context = context
        this.statusReceiver = statusReceiver
        this.interfaceProvider = interfaceProvider
        connectionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                onReceiveBroadcastOfConnection(context, intent)
            }
        }
        connectSequence = NikonCameraConnectSequence(
            context,
            statusReceiver,
            this,
            interfaceProvider,
            statusChecker
        )
        disconnectSequence = NikonCameraDisconnectSequence(interfaceProvider, statusChecker)
    }

    /**
     * 
     * 
     */
    private fun onReceiveBroadcastOfConnection(context: Context, intent: Intent) {
        interfaceProvider.getInformationReceiver()
            .updateMessage(context.getString(R.string.connect_check_wifi), false, false, 0)
        statusReceiver.onStatusNotify(context.getString(R.string.connect_check_wifi))

        Log.v(TAG, context.getString(R.string.connect_check_wifi))

        val action = intent.getAction()
        if (action == null) {
            Log.v(TAG, "intent.getAction() : null")
            return
        }

        try {
            if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                Log.v(TAG, "onReceiveBroadcastOfConnection() : CONNECTIVITY_ACTION")

                val wifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager?
                if (wifiManager != null) {
                    val info = wifiManager.getConnectionInfo()
                    if (wifiManager.isWifiEnabled() && info != null) {
                        if (info.getNetworkId() != -1) {
                            Log.v(TAG, "Network ID is -1, there is no currently connected network.")
                        }
                        // 自動接続が指示されていた場合は、カメラとの接続処理を行う
                        connectToCamera()
                    } else {
                        if (info == null) {
                            Log.v(TAG, "NETWORK INFO IS NULL.")
                        } else {
                            Log.v(
                                TAG,
                                "isWifiEnabled : " + wifiManager.isWifiEnabled + " NetworkId : " + info.networkId
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "onReceiveBroadcastOfConnection() EXCEPTION" + e.message)
            e.printStackTrace()
        }
    }

    override fun startWatchWifiStatus(context: Context) {
        Log.v(TAG, "startWatchWifiStatus()")
        interfaceProvider.getInformationReceiver()
            .updateMessage(context.getString(R.string.connect_prepare), false, false, 0)
        statusReceiver.onStatusNotify("prepare")

        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(connectionReceiver, filter)
    }

    override fun stopWatchWifiStatus(context: Context) {
        Log.v(TAG, "stopWatchWifiStatus()")
        context.unregisterReceiver(connectionReceiver)
        disconnect(false)
    }

    override fun disconnect(powerOff: Boolean) {
        Log.v(TAG, "disconnect()")
        disconnectFromCamera(powerOff)
        connectionStatus = CameraConnectionStatus.DISCONNECTED
        statusReceiver.onCameraDisconnected()
    }

    override fun connect() {
        Log.v(TAG, "connect()")
        connectToCamera()
    }

    override fun alertConnectingFailed(message: String?) {
        Log.v(TAG, "alertConnectingFailed() : " + message)
        val builder = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialog_title_connect_failed_nikon))
            .setMessage(message)
            .setPositiveButton(
                context.getString(R.string.dialog_title_button_retry),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        disconnect(false)
                        connect()
                    }
                })
            .setNeutralButton(
                R.string.dialog_title_button_network_settings,
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        try {
                            // Wifi 設定画面を表示する
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        } catch (ex: ActivityNotFoundException) {
                            // Activity が存在しなかった...設定画面が起動できなかった
                            Log.v(TAG, "android.content.ActivityNotFoundException...")

                            // この場合は、再試行と等価な動きとする
                            connect()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        context.runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    builder.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun getConnectionStatus(): CameraConnectionStatus? {
        Log.v(TAG, " getConnectionStatus()")
        return (connectionStatus)
    }

    override fun forceUpdateConnectionStatus(status: CameraConnectionStatus?) {
        Log.v(TAG, " forceUpdateConnectionStatus()")
        connectionStatus = status
    }

    /**
     * カメラとの切断処理
     */
    private fun disconnectFromCamera(powerOff: Boolean) {
        Log.v(TAG, " disconnectFromCamera() : " + powerOff)
        try {
            cameraExecutor.execute(disconnectSequence)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * カメラとの接続処理
     */
    private fun connectToCamera() {
        Log.v(TAG, " connectToCamera()")
        connectionStatus = CameraConnectionStatus.CONNECTING
        try {
            cameraExecutor.execute(connectSequence)
        } catch (e: Exception) {
            Log.v(TAG, " connectToCamera() EXCEPTION : " + e.message)
            e.printStackTrace()
        }
    }
}
*/