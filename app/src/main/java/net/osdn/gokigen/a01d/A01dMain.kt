package net.osdn.gokigen.a01d

import android.Manifest.permission
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
import androidx.fragment.app.Fragment

class A01dMain : AppCompatActivity(),
    ICameraStatusReceiver,
    IChangeScene,
    PowerOnCameraCallback,
    IInformationReceiver,
    ICardSlotSelector {

    private lateinit var interfaceProvider: IInterfaceProvider
    private var statusViewDrawer: IStatusViewDrawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_a01d_main)

        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupWindowInset(findViewById(R.id.base_layout))

        if (allPermissionsGranted()) {
            Log.v(TAG, "allPermissionsGranted() : true")
            initializeClass()
            initializeFragment()
            onReadyClass()
        } else {
            Log.v(TAG, "====== REQUEST PERMISSIONS ======")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_NEED_PERMISSIONS)
        }
    }

    private fun setupWindowInset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (param in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(baseContext, param) != PackageManager.PERMISSION_GRANTED) {
                when (param) {
                    permission.ACCESS_MEDIA_LOCATION if Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> continue
                    permission.READ_EXTERNAL_STORAGE if Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> continue
                    permission.WRITE_EXTERNAL_STORAGE if Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> continue
                    permission.BLUETOOTH_SCAN if Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> continue
                    permission.BLUETOOTH_CONNECT if Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> continue
                    permission.NEARBY_WIFI_DEVICES -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    permission.ACCESS_LOCAL_NETWORK -> Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN
                    permission.BLUETOOTH -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    permission.BLUETOOTH_ADMIN -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    permission.ACCESS_FINE_LOCATION -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    permission.ACCESS_COARSE_LOCATION -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    else -> {
                        Log.v(TAG, " Permission denied: $param (SDK: ${Build.VERSION.SDK_INT})")
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "------------------------- onRequestPermissionsResult()")
        if (requestCode == REQUEST_NEED_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.v(TAG, "onRequestPermissionsResult(): granted")
                initializeClass()
                initializeFragment()
                onReadyClass()
            } else {
                Log.v(TAG, "----- onRequestPermissionsResult() : false")
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initializeClass() {
        interfaceProvider = CameraInterfaceProvider(this, this, this, this)
    }

    private fun onReadyClass() {
        if (!::interfaceProvider.isInitialized) return

        if (isBlePowerOn) {
            if (interfaceProvider.cammeraConnectionMethod == CameraConnectionMethod.OPC) {
                try {
                    interfaceProvider.olympusInterface.cameraPowerOn.wakeup(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (isAutoConnectCamera) {
            changeCameraConnection()
        }
    }

    private fun initializeFragment() {
        val liveViewFragment = LiveViewFragment.newInstance(this, interfaceProvider)
        statusViewDrawer = liveViewFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment1, liveViewFragment)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        try
        {
            if (::interfaceProvider.isInitialized) {
                val method = interfaceProvider.cammeraConnectionMethod
                val connection = getCameraConnection(method)
                connection.stopWatchWifiStatus(this)
            }
        }
        catch (e: Exception)
        {
            Log.e(TAG, "onPause() Exception: ${e.localizedMessage}")
        }
    }

    override fun changeSceneToCameraPropertyList()
    {
        if (!::interfaceProvider.isInitialized) return
        val method = interfaceProvider.cammeraConnectionMethod
        if (method == CameraConnectionMethod.OPC) {
            changeSceneToCameraPropertyList(method)
        }
    }

    override fun changeSceneToCameraPropertyList(connectionMethod: CameraConnectionMethod) {
        if (!::interfaceProvider.isInitialized) return

        when (connectionMethod) {
            CameraConnectionMethod.RICOH_GR2 -> {
                RicohGr2SendCommandDialog.newInstance()
                    .show(supportFragmentManager, "RicohGr2SendCommandDialog")
            }
            CameraConnectionMethod.SONY -> {
                changeSceneToApiList()
            }
            CameraConnectionMethod.PANASONIC -> {
                PanasonicSendCommandDialog.newInstance(interfaceProvider.panasonicInterface)
                    .show(supportFragmentManager, "panasonicSendCommandDialog")
            }
            CameraConnectionMethod.FUJI_X -> {
                FujiXCameraCommandSendDialog.newInstance(interfaceProvider.fujiXInterface)
                    .show(supportFragmentManager, "sendCommandDialog")
            }
            CameraConnectionMethod.OLYMPUS -> {
                val headerMap = mapOf(
                    "User-Agent" to "OlympusCameraKit",
                    "X-Protocol" to "OlympusCameraKit"
                )
                SimpleHttpSendCommandDialog.newInstance(
                    URL_OLYMPUS_PEN,
                    interfaceProvider.olympusPenInterface.liveViewControl,
                    headerMap
                ).show(supportFragmentManager, "olympusPenSendCommandDialog")
            }
            CameraConnectionMethod.THETA -> {
                SimpleHttpSendCommandDialog.newInstance(URL_THETA, null, null)
                    .show(supportFragmentManager, "thetaSendCommandDialog")
            }
            CameraConnectionMethod.CANON -> {
                PtpIpCameraCommandSendDialog.newInstance(interfaceProvider.canonInterface, true)
                    .show(supportFragmentManager, "ptpipSendCommandDialog")
            }
            CameraConnectionMethod.NIKON -> {
                // NIKON用のインターフェース指定に修正
                PtpIpCameraCommandSendDialog.newInstance(interfaceProvider.nikonInterface, true)
                    .show(supportFragmentManager, "ptpipSendCommandDialog")
            }
            else -> {
                Log.v(TAG, " Change Scene to propertyList :")
                val connection = getCameraConnection(connectionMethod)
                if (connection.connectionStatus == CameraConnectionStatus.CONNECTED) {
                    val fragment = OlyCameraPropertyListFragment.newInstance(
                        this,
                        interfaceProvider.olympusInterface.cameraPropertyProvider
                    )
                    replaceFragment(fragment)
                }
            }
        }
    }

    override fun changeSceneToConfiguration() {
        val fragment = PreferenceFragmentSummary.newInstance(this, this)
        replaceFragment(fragment)
    }

    override fun changeSceneToConfiguration(connectionMethod: CameraConnectionMethod) {
        if (!::interfaceProvider.isInitialized) return

        val targetFragment: Fragment? = when (connectionMethod) {
            CameraConnectionMethod.RICOH_GR2 -> RicohGr2PreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.SONY -> SonyPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.PANASONIC -> PanasonicPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.OLYMPUS -> OlympusPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.FUJI_X -> FujiXPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.THETA -> ThetaPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.CANON -> CanonPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.NIKON -> NikonPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.KODAK -> KodakPreferenceFragment.newInstance(this, this)
            CameraConnectionMethod.OPC -> PreferenceFragment.newInstance(this, interfaceProvider, this)
        }

        targetFragment?.let { replaceFragment(it) }
    }

    override fun changeSceneToDebugInformation() {
        replaceFragment(LogCatFragment.newInstance())
    }

    override fun changeSceneToApiList() {
        if (!::interfaceProvider.isInitialized) return
        replaceFragment(SonyCameraApiListFragment.newInstance(interfaceProvider))
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment1, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun changeCameraConnection() {
        if (!::interfaceProvider.isInitialized) return
        val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
        if (connection.connectionStatus == CameraConnectionStatus.CONNECTED) {
            connection.disconnect(false)
        } else {
            connection.startWatchWifiStatus(this)
        }
    }

    override fun exitApplication() {
        Log.v(TAG, "exitApplication()")
        if (::interfaceProvider.isInitialized) {
            val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
            connection.disconnect(true)
        }
        finish()
    }

    override fun onStatusNotify(message: String) {
        Log.v(TAG, " CONNECTION MESSAGE : $message")
        if (!::interfaceProvider.isInitialized) return
        val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
        statusViewDrawer?.updateStatusView(message)
        statusViewDrawer?.updateConnectionStatus(connection.connectionStatus)
    }

    override fun onCameraConnected() {
        Log.v(TAG, "onCameraConnected()")
        if (!::interfaceProvider.isInitialized) return
        val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
        connection.forceUpdateConnectionStatus(CameraConnectionStatus.CONNECTED)
        statusViewDrawer?.updateConnectionStatus(CameraConnectionStatus.CONNECTED)
        statusViewDrawer?.startLiveView()
    }

    override fun onCameraDisconnected() {
        Log.v(TAG, "onCameraDisconnected()")
        statusViewDrawer?.updateStatusView(getString(R.string.camera_disconnected))
        statusViewDrawer?.updateConnectionStatus(CameraConnectionStatus.DISCONNECTED)
    }

    override fun onCameraOccursException(message: String, e: Exception) {
        Log.v(TAG, "onCameraOccursException() $message")
        e.printStackTrace()
        if (!::interfaceProvider.isInitialized) return
        val connection = getCameraConnection(interfaceProvider.cammeraConnectionMethod)
        connection.alertConnectingFailed("$message ${e.localizedMessage}")
        statusViewDrawer?.updateStatusView(message)
        statusViewDrawer?.updateConnectionStatus(connection.connectionStatus)
    }

    private val isBlePowerOn: Boolean
        get() {
            if (!::interfaceProvider.isInitialized) return false
            return if (interfaceProvider.cammeraConnectionMethod == CameraConnectionMethod.OPC) {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.getBoolean(IPreferencePropertyAccessor.BLE_POWER_ON, false)
            } else {
                false
            }
        }

    private val isAutoConnectCamera: Boolean
        get() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            return preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
        }

    private fun getCameraConnection(connectionMethod: CameraConnectionMethod): ICameraConnection {
        return when (connectionMethod) {
            CameraConnectionMethod.RICOH_GR2 -> interfaceProvider.ricohGr2Infterface.ricohGr2CameraConnection
            CameraConnectionMethod.SONY -> interfaceProvider.sonyInterface.sonyCameraConnection
            CameraConnectionMethod.PANASONIC -> interfaceProvider.panasonicInterface.getPanasonicCameraConnection()
            CameraConnectionMethod.FUJI_X -> interfaceProvider.fujiXInterface.fujiXCameraConnection
            CameraConnectionMethod.OLYMPUS -> interfaceProvider.olympusPenInterface.olyCameraConnection
            CameraConnectionMethod.THETA -> interfaceProvider.thetaInterface.cameraConnection
            CameraConnectionMethod.CANON -> interfaceProvider.canonInterface.cameraConnection
            CameraConnectionMethod.NIKON -> interfaceProvider.nikonInterface.cameraConnection
            CameraConnectionMethod.KODAK -> interfaceProvider.kodakInterface.cameraConnection
            else -> interfaceProvider.olympusInterface.olyCameraConnection
        }
    }

    override fun wakeupExecuted(isExecuted: Boolean) {
        Log.v(TAG, "wakeupExecuted() : $isExecuted")
        if (isAutoConnectCamera) {
            changeCameraConnection()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.v(TAG, "onKeyDown() $keyCode")
        if (event.action == KeyEvent.ACTION_DOWN &&
            (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_CAMERA)
        ) {
            (supportFragmentManager.findFragmentById(R.id.fragment1) as? LiveViewFragment)?.let {
                return it.handleKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun updateMessage(message: String, isBold: Boolean, isColor: Boolean, color: Int) {
        Log.v(TAG, " updateMessage() : $message")
        runOnUiThread {
            val messageArea = findViewById<TextView>(R.id.message) ?: return@runOnUiThread
            messageArea.text = message
            messageArea.typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            messageArea.setTextColor(if (isColor) color else Color.DKGRAY)
            messageArea.invalidate()
        }
    }

    override fun setupSlotSelector(isEnable: Boolean, slotSelectionReceiver: ICardSlotSelectionReceiver?) {}
    override fun selectSlot(slotId: String) {}
    override fun changedCardSlot(slotId: String) {}

    companion object {
        private val TAG = A01dMain::class.java.simpleName
        private const val REQUEST_NEED_PERMISSIONS = 1010

        private const val URL_OLYMPUS_PEN = "http://192.168.0.10/"
        private const val URL_THETA = "http://192.168.1.1/"

        private val REQUIRED_PERMISSIONS = arrayOf(
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_EXTERNAL_STORAGE,
            permission.ACCESS_MEDIA_LOCATION,
            permission.ACCESS_NETWORK_STATE,
            permission.ACCESS_WIFI_STATE,
            permission.BLUETOOTH,
            permission.BLUETOOTH_SCAN,
            permission.BLUETOOTH_CONNECT,
            permission.BLUETOOTH_ADMIN,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.INTERNET,
            permission.ACCESS_LOCAL_NETWORK,
            permission.NEARBY_WIFI_DEVICES
        )
    }
}
