package net.osdn.gokigen.a01d

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import net.osdn.gokigen.a01d.camera.CameraInterfaceProvider
import net.osdn.gokigen.a01d.camera.ICameraConnection
import net.osdn.gokigen.a01d.camera.ICameraConnection.CameraConnectionMethod
import net.osdn.gokigen.a01d.camera.ICameraConnection.CameraConnectionStatus
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver
import net.osdn.gokigen.a01d.camera.IInterfaceProvider
import net.osdn.gokigen.a01d.camera.fujix.cameraproperty.FujiXCameraCommandSendDialog
import net.osdn.gokigen.a01d.camera.olympus.cameraproperty.OlyCameraPropertyListFragment
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn.PowerOnCameraCallback
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicSendCommandDialog
import net.osdn.gokigen.a01d.camera.ptpip.operation.PtpIpCameraCommandSendDialog
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2SendCommandDialog
import net.osdn.gokigen.a01d.camera.sony.cameraproperty.SonyCameraApiListFragment
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpSendCommandDialog
import net.osdn.gokigen.a01d.liveview.IStatusViewDrawer
import net.osdn.gokigen.a01d.liveview.LiveViewFragment
import net.osdn.gokigen.a01d.logcat.LogCatFragment
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor
import net.osdn.gokigen.a01d.preference.canon.CanonPreferenceFragment
import net.osdn.gokigen.a01d.preference.fujix.FujiXPreferenceFragment
import net.osdn.gokigen.a01d.preference.kodak.KodakPreferenceFragment
import net.osdn.gokigen.a01d.preference.nikon.NikonPreferenceFragment
import net.osdn.gokigen.a01d.preference.olympus.PreferenceFragment
import net.osdn.gokigen.a01d.preference.olympuspen.OlympusPreferenceFragment
import net.osdn.gokigen.a01d.preference.panasonic.PanasonicPreferenceFragment
import net.osdn.gokigen.a01d.preference.ricohgr2.RicohGr2PreferenceFragment
import net.osdn.gokigen.a01d.preference.sony.SonyPreferenceFragment
import net.osdn.gokigen.a01d.preference.summary.PreferenceFragmentSummary
import net.osdn.gokigen.a01d.preference.theta.ThetaPreferenceFragment

/**
 * A01d
 *
 */
class A01dMain : AppCompatActivity(), ICameraStatusReceiver, IChangeScene, PowerOnCameraCallback,
    IInformationReceiver, ICardSlotSelector
{
    private lateinit var interfaceProvider: IInterfaceProvider
    private var statusViewDrawer: IStatusViewDrawer? = null

    private var preferenceFragment: PreferenceFragmentCompat? = null
    private var preferenceFragmentOPC: PreferenceFragmentCompat? = null
    private var preferenceFragmentOlympus: PreferenceFragmentCompat? = null
    private var preferenceFragmentSony: PreferenceFragmentCompat? = null
    private var preferenceFragmentRicoh: PreferenceFragmentCompat? = null
    private var preferenceFragmentTheta: PreferenceFragmentCompat? = null
    private var preferenceFragmentFuji: PreferenceFragmentCompat? = null
    private var preferenceFragmentPanasonic: PreferenceFragmentCompat? = null
    private var preferenceFragmentCanon: PreferenceFragmentCompat? = null
    private var preferenceFragmentNikon: PreferenceFragmentCompat? = null
    private var preferenceFragmentKodak: PreferenceFragmentCompat? = null

    private var propertyListFragment: OlyCameraPropertyListFragment? = null
    private var sonyApiListFragmentSony: SonyCameraApiListFragment? = null
    private lateinit var logCatFragment: LogCatFragment
    private lateinit var liveViewFragment: LiveViewFragment

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a01d_main)
        try
        {
            val bar = supportActionBar
            bar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            try
            {
                setupWindowInset(findViewById(R.id.base_layout))
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            if (allPermissionsGranted())
            {
                Log.v(TAG, "allPermissionsGranted() : true")
                initializeClass()
                initializeFragment()
                onReadyClass()
            }
            else
            {
                Log.v(TAG, "====== REQUEST PERMISSIONS ======")
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_NEED_PERMISSIONS)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupWindowInset(view: View)
    {
        try
        {
            // Display cutout insets
            //   https://developer.android.com/develop/ui/views/layout/edge-to-edge
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    left = bars.left,
                    top = bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                WindowInsetsCompat.CONSUMED
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    param
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                // ----- Permission Denied...
                if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合）
                }
                else if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 33以上はエラーになる...)
                }
                else if ((param == Manifest.permission.WRITE_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 33以上はエラーになる...)
                }
                else if ((param == Manifest.permission.BLUETOOTH_SCAN)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 31よりも下はエラーになるはず...)
                    Log.v(TAG, "BLUETOOTH_SCAN")
                }
                else if ((param == Manifest.permission.BLUETOOTH_CONNECT)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 31よりも下はエラーになるはず...)
                    Log.v(TAG, "BLUETOOTH_CONNECT")
                }
                else
                {
                    // ----- 権限が得られなかった場合...
                    Log.v(TAG, " Permission: $param : ${Build.VERSION.SDK_INT}")
                    result = false
                }
            }
        }
        return (result)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try
        {
            Log.v(TAG, "------------------------- onRequestPermissionsResult() ")
            if (requestCode == REQUEST_NEED_PERMISSIONS)
            {
                if (allPermissionsGranted())
                {
                    // ----- 権限が有効だった、最初の画面を開く
                    Log.v(TAG, "onRequestPermissionsResult()")
                    initializeClass()
                    initializeFragment()
                    onReadyClass()
                }
                else
                {
                    Log.v(TAG, "----- onRequestPermissionsResult() : false")
                    Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * クラスの初期化
     */
    private fun initializeClass()
    {
        try
        {
            interfaceProvider = CameraInterfaceProvider(this, this, this, this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 初期化終了時の処理
     */
    private fun onReadyClass()
    {
        try
        {
            if ((isBlePowerOn)&&(::interfaceProvider.isInitialized))
            {
                // BLEでPower ONは、OPCのみ対応
                if (interfaceProvider.cammeraConnectionMethod == CameraConnectionMethod.OPC)
                {
                    // BLEでカメラの電源をONにする設定だった時
                    try
                    {
                        // カメラの電源ONクラスを呼び出しておく (電源ONができたら、コールバックをもらう）
                        interfaceProvider.olympusInterface.cameraPowerOn.wakeup(this)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            else if (isAutoConnectCamera)
            {
                // 自動接続の指示があったとき
                changeCameraConnection()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * フラグメントの初期化
     */
    private fun initializeFragment()
    {
        try
        {
            run {
                liveViewFragment = LiveViewFragment.newInstance(
                    this,
                    interfaceProvider
                )
            }
            statusViewDrawer = liveViewFragment
            @Suppress("DEPRECATION")
            liveViewFragment.retainInstance = true
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, liveViewFragment)
            transaction.commitAllowingStateLoss()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    override fun onPause()
    {
        super.onPause()
        try
        {
            val method = interfaceProvider.cammeraConnectionMethod
            val connection = getCameraConnection(method)
            connection.stopWatchWifiStatus(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラのプロパティ一覧画面を開く
     * （カメラと接続中のときのみ、接続方式が Olympusのときのみ）
     */
    override fun changeSceneToCameraPropertyList()
    {
        try
        {
            val method = interfaceProvider.cammeraConnectionMethod
            if (method == CameraConnectionMethod.OPC)
            {
                changeSceneToCameraPropertyList(method)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラのプロパティ一覧画面を開く
     * （カメラと接続中のときのみ、接続方式が Olympusのときのみ）
     */
    override fun changeSceneToCameraPropertyList(connectionMethod: CameraConnectionMethod)
    {
        try
        {
            if (connectionMethod == CameraConnectionMethod.RICOH_GR2)
            {
                try
                {
                    // Ricohの場合は、コマンド送信ダイアログを表示する
                    RicohGr2SendCommandDialog.newInstance()
                        .show(supportFragmentManager, "RicohGr2SendCommandDialog")
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.SONY)
            {
                // SONYの場合は、API一覧画面へ遷移させる
                changeSceneToApiList()
            }
            else if (connectionMethod == CameraConnectionMethod.PANASONIC)
            {
                try
                {
                    // Panasonicの場合は、コマンド送信ダイアログを表示する
                    PanasonicSendCommandDialog.newInstance(interfaceProvider.panasonicInterface)
                        .show(
                            supportFragmentManager, "panasonicSendCommandDialog"
                        )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.FUJI_X)
            {
                try
                {
                    // FUJI X Seriesの場合は、コマンド送信ダイアログを表示する
                    FujiXCameraCommandSendDialog.newInstance(interfaceProvider.fujiXInterface)
                        .show(
                            supportFragmentManager, "sendCommandDialog"
                        )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.OLYMPUS)
            {
                try
                {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["User-Agent"] = "OlympusCameraKit" // "OI.Share"
                    headerMap["X-Protocol"] = "OlympusCameraKit" // "OI.Share"

                    // Olympus Penの場合は、コマンド送信ダイアログを表示する
                    SimpleHttpSendCommandDialog.newInstance(
                        "http://192.168.0.10/",
                        interfaceProvider.olympusPenInterface.liveViewControl,
                        headerMap
                    ).show(
                        supportFragmentManager, "olympusPenSendCommandDialog"
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.THETA)
            {
                try
                {
                    // THETA の場合は、HTTPコマンド送信ダイアログを表示する
                    SimpleHttpSendCommandDialog.newInstance("http://192.168.1.1/", null, null).show(
                        supportFragmentManager, "thetaSendCommandDialog"
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.CANON)
            {
                try
                {
                    // CANON の場合は、PTPIPコマンド送信ダイアログを表示する
                    PtpIpCameraCommandSendDialog.newInstance(
                        interfaceProvider.canonInterface,
                        true
                    ).show(
                        supportFragmentManager, "ptpipSendCommandDialog"
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (connectionMethod == CameraConnectionMethod.NIKON)
            {
                try
                {
                    // NIKON の場合は、PTPIPコマンド送信ダイアログを表示する
                    PtpIpCameraCommandSendDialog.newInstance(
                        interfaceProvider.canonInterface,
                        true
                    ).show(
                        supportFragmentManager, "ptpipSendCommandDialog"
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else
            {
                // OPC カメラの場合...;
                Log.v(TAG, " Change Scene to propertyList :")
                val connection = getCameraConnection(connectionMethod)
                val status = connection.connectionStatus
                if (status == CameraConnectionStatus.CONNECTED)
                {
                    if (propertyListFragment == null) {
                        propertyListFragment = OlyCameraPropertyListFragment.newInstance(
                            this,
                            interfaceProvider.olympusInterface.cameraPropertyProvider
                        )
                    }
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment1, propertyListFragment!!)
                    // backstackに追加
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 設定画面を開く
     *
     */
    override fun changeSceneToConfiguration()
    {
        try
        {
            if (preferenceFragment == null)
            {
                try
                {
                    preferenceFragment = PreferenceFragmentSummary.newInstance(this, this)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, preferenceFragment!!)
            transaction.addToBackStack(null) // backstackに追加
            transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changeSceneToConfiguration(connectionMethod: CameraConnectionMethod)
    {
        try
        {
            var targetFragment: PreferenceFragmentCompat? = null
            when (connectionMethod) {
                CameraConnectionMethod.RICOH_GR2 -> {
                    if (preferenceFragmentRicoh == null) {
                        preferenceFragmentRicoh = RicohGr2PreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentRicoh
                }
                CameraConnectionMethod.SONY -> {
                    if (preferenceFragmentSony == null) {
                        preferenceFragmentSony = SonyPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentSony
                }
                CameraConnectionMethod.PANASONIC -> {
                    if (preferenceFragmentPanasonic == null) {
                        preferenceFragmentPanasonic =
                            PanasonicPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentPanasonic
                }
                CameraConnectionMethod.OLYMPUS -> {
                    if (preferenceFragmentOlympus == null) {
                        preferenceFragmentOlympus = OlympusPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentOlympus
                }
                CameraConnectionMethod.FUJI_X -> {
                    if (preferenceFragmentFuji == null) {
                        preferenceFragmentFuji = FujiXPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentFuji
                }
                CameraConnectionMethod.THETA -> {
                    if (preferenceFragmentTheta == null) {
                        preferenceFragmentTheta = ThetaPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentTheta
                }
                CameraConnectionMethod.CANON -> {
                    if (preferenceFragmentCanon == null) {
                        preferenceFragmentCanon = CanonPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentCanon
                }
                CameraConnectionMethod.NIKON -> {
                    if (preferenceFragmentNikon == null) {
                        preferenceFragmentNikon = NikonPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentNikon
                }
                CameraConnectionMethod.KODAK -> {
                    if (preferenceFragmentKodak == null) {
                        preferenceFragmentKodak = KodakPreferenceFragment.newInstance(this, this)
                    }
                    targetFragment = preferenceFragmentKodak
                }
                CameraConnectionMethod.OPC -> {
                    if (preferenceFragmentOPC == null) {
                        preferenceFragmentOPC = PreferenceFragment.newInstance(
                            this,
                            interfaceProvider, this
                        )
                    }
                    targetFragment = preferenceFragmentOPC
                }
            }
            if (targetFragment != null) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment1, targetFragment)
                transaction.addToBackStack(null) // backstackに追加
                transaction.commit()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * デバッグ情報画面を開く
     *
     */
    override fun changeSceneToDebugInformation()
    {
        try
        {
            if (!::logCatFragment.isInitialized)
            {
                logCatFragment = LogCatFragment.newInstance()
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, logCatFragment)
            // backstackに追加
            transaction.addToBackStack(null)
            transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * SonyのAPI List画面を開く
     *
     */
    override fun changeSceneToApiList()
    {
        try
        {
            if (sonyApiListFragmentSony == null)
            {
                sonyApiListFragmentSony = SonyCameraApiListFragment.newInstance(interfaceProvider)
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, sonyApiListFragmentSony!!)
            // backstackに追加
            transaction.addToBackStack(null)
            transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラとの接続・切断のシーケンス
     */
    override fun changeCameraConnection()
    {
        try
        {
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            val status = connection.connectionStatus
            if (status == CameraConnectionStatus.CONNECTED)
            {
                // 接続中のときには切断する
                connection.disconnect(false)
                return
            }
            // 接続中でない時は、接続中にする
            connection.startWatchWifiStatus(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * アプリを抜ける
     */
    override fun exitApplication()
    {
        Log.v(TAG, "exitApplication()")
        try
        {
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            connection.disconnect(true)
            finish()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onStatusNotify(message: String)
    {
        Log.v(TAG, " CONNECTION MESSAGE : $message")
        try
        {
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            statusViewDrawer?.updateStatusView(message)
            statusViewDrawer?.updateConnectionStatus(connection.connectionStatus)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onCameraConnected()
    {
        Log.v(TAG, "onCameraConnected()")
        try
        {
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            // クラス構造をミスった...のでこんなところで、無理やりステータスを更新する
            connection.forceUpdateConnectionStatus(CameraConnectionStatus.CONNECTED)

            statusViewDrawer?.updateConnectionStatus(CameraConnectionStatus.CONNECTED)
            // ライブビューの開始...
            statusViewDrawer?.startLiveView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onCameraDisconnected()
    {
        Log.v(TAG, "onCameraDisconnected()")
        try
        {
            statusViewDrawer?.updateStatusView(getString(R.string.camera_disconnected))
            statusViewDrawer?.updateConnectionStatus(CameraConnectionStatus.DISCONNECTED)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onCameraOccursException(message: String, e: Exception)
    {
        Log.v(TAG, "onCameraOccursException() $message")
        try
        {
            e.printStackTrace()
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            connection.alertConnectingFailed(message + " " + e.localizedMessage)
            if (statusViewDrawer != null)
            {
                statusViewDrawer?.updateStatusView(message)
                statusViewDrawer?.updateConnectionStatus(connection.connectionStatus)
            }
        }
        catch (ee: Exception)
        {
            ee.printStackTrace()
        }
    }

    private val isBlePowerOn: Boolean
        /**
         * BLE経由でカメラの電源を入れるかどうか
         *
         */
        get() {
            var ret = false
            try {
                if (interfaceProvider.cammeraConnectionMethod == CameraConnectionMethod.OPC) {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(
                        this
                    )
                    ret = preferences.getBoolean(IPreferencePropertyAccessor.BLE_POWER_ON, false)
                    // Log.v(TAG, "isBlePowerOn() : " + ret);
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return (ret)
        }

    private val isAutoConnectCamera: Boolean
        /**
         * カメラへの自動接続を行うかどうか
         *
         */
        get() {
            var ret = true
            try {
                val preferences = PreferenceManager.getDefaultSharedPreferences(
                    this
                )
                ret =
                    preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
                // Log.v(TAG, "isAutoConnectCamera() : " + ret);
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return (ret)
        }

    /**
     *
     *
     *
     */
    private fun getCameraConnection(connectionMethod: CameraConnectionMethod): ICameraConnection
    {
        val connection = when (connectionMethod) {
            CameraConnectionMethod.RICOH_GR2 -> {
                interfaceProvider.ricohGr2Infterface.ricohGr2CameraConnection
            }
            CameraConnectionMethod.SONY -> {
                interfaceProvider.sonyInterface.sonyCameraConnection
            }
            CameraConnectionMethod.PANASONIC -> {
                interfaceProvider.panasonicInterface.getPanasonicCameraConnection()
            }
            CameraConnectionMethod.FUJI_X -> {
                interfaceProvider.fujiXInterface.fujiXCameraConnection
            }
            CameraConnectionMethod.OLYMPUS -> {
                interfaceProvider.olympusPenInterface.olyCameraConnection
            }
            CameraConnectionMethod.THETA -> {
                interfaceProvider.thetaInterface.cameraConnection
            }
            CameraConnectionMethod.CANON -> {
                interfaceProvider.canonInterface.cameraConnection
            }
            CameraConnectionMethod.NIKON -> {
                interfaceProvider.nikonInterface.cameraConnection
            }
            CameraConnectionMethod.KODAK -> {
                interfaceProvider.kodakInterface.cameraConnection
            }
            else  // if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            -> {
                interfaceProvider.olympusInterface.olyCameraConnection
            }
        }
        return (connection)
    }

    /**
     * カメラへのBLE接続指示が完了したとき
     *
     * @param isExecuted  true : BLEで起動した, false : 起動していない、その他
     */
    override fun wakeupExecuted(isExecuted: Boolean)
    {
        Log.v(TAG, "wakeupExecuted() : $isExecuted")
        if (isAutoConnectCamera)
        {
            // カメラへ自動接続する設定だった場合、カメラへWiFi接続する (BLEで起動しなくても)
            changeCameraConnection()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
    {
        Log.v(TAG, "onKeyDown() $keyCode")
        try
        {
            if ((event.action == KeyEvent.ACTION_DOWN) &&
                ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_CAMERA))
            ) {
                return (liveViewFragment.handleKeyDown(keyCode, event))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (super.onKeyDown(keyCode, event))
    }

    override fun updateMessage(message: String, isBold: Boolean, isColor: Boolean, color: Int)
    {
        Log.v(TAG, " updateMessage() : $message")
        try
        {
            val messageArea = findViewById<TextView>(R.id.message)
            runOnUiThread {
                try
                {
                    if (messageArea != null)
                    {
                        messageArea.text = message
                        if (isBold)
                        {
                            messageArea.typeface = Typeface.DEFAULT_BOLD
                        }
                        if (isColor)
                        {
                            messageArea.setTextColor(color)
                        }
                        else
                        {
                            messageArea.setTextColor(Color.DKGRAY)
                        }
                        messageArea.invalidate()
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun setupSlotSelector(isEnable: Boolean, slotSelectionReceiver: ICardSlotSelectionReceiver?) { }
    override fun selectSlot(slotId: String) { }
    override fun changedCardSlot(slotId: String) { }

    companion object
    {
        private val TAG = A01dMain::class.java.simpleName
        private const val REQUEST_NEED_PERMISSIONS = 1010

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
        )
    }
}
