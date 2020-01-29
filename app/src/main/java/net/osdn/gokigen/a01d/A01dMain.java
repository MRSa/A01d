package net.osdn.gokigen.a01d;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import net.osdn.gokigen.a01d.camera.CameraInterfaceProvider;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;
import net.osdn.gokigen.a01d.camera.fujix.cameraproperty.FujiXCameraCommandSendDialog;
import net.osdn.gokigen.a01d.camera.olympus.cameraproperty.OlyCameraPropertyListFragment;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn;
import net.osdn.gokigen.a01d.camera.ptpip.operation.PtpIpCameraCommandSendDialog;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpSendCommandDialog;
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicSendCommandDialog;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2SendCommandDialog;
import net.osdn.gokigen.a01d.camera.sony.cameraproperty.SonyCameraApiListFragment;
import net.osdn.gokigen.a01d.liveview.IStatusViewDrawer;
import net.osdn.gokigen.a01d.liveview.LiveViewFragment;
import net.osdn.gokigen.a01d.logcat.LogCatFragment;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.a01d.preference.canon.CanonPreferenceFragment;
import net.osdn.gokigen.a01d.preference.fujix.FujiXPreferenceFragment;
import net.osdn.gokigen.a01d.preference.olympus.PreferenceFragment;
import net.osdn.gokigen.a01d.preference.panasonic.PanasonicPreferenceFragment;
import net.osdn.gokigen.a01d.preference.olympuspen.OlympusPreferenceFragment;
import net.osdn.gokigen.a01d.preference.ricohgr2.RicohGr2PreferenceFragment;
import net.osdn.gokigen.a01d.preference.sony.SonyPreferenceFragment;
import net.osdn.gokigen.a01d.preference.theta.ThetaPreferenceFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 *   A01d ;
 *
 */
public class A01dMain extends AppCompatActivity implements ICameraStatusReceiver, IChangeScene, ICameraPowerOn.PowerOnCameraCallback, IInformationReceiver
{
    private final String TAG = toString();
    private IInterfaceProvider interfaceProvider = null;
    private IStatusViewDrawer statusViewDrawer = null;

    private PreferenceFragmentCompat preferenceFragment = null;
    private OlyCameraPropertyListFragment propertyListFragment = null;
    private SonyCameraApiListFragment sonyApiListFragmentSony = null;
    private LogCatFragment logCatFragment = null;
    private LiveViewFragment liveViewFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final int REQUEST_NEED_PERMISSIONS = 1010;

        super.onCreate(savedInstanceState);
/*
        try {
            // 全画面表示...
            if (Build.VERSION.SDK_INT >= 19)
            {
                View decor = this.getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
        setContentView(R.layout.activity_a01d_main);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            // タイトルバーは表示しない
            bar.hide();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 外部メモリアクセス権のオプトイン
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_MEDIA_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET,
                    },
                    REQUEST_NEED_PERMISSIONS);
        }
        initializeClass();
        initializeFragment();
        onReadyClass();
    }

    /**
     *   なぜか、onReadyClass() が有効ではなさそうなので...
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]  permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onReadyClass();
    }

    /**
     * クラスの初期化
     */
    private void initializeClass()
    {
        try
        {
            interfaceProvider = new CameraInterfaceProvider(this, this, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 初期化終了時の処理
     */
    private void onReadyClass()
    {
        if (isBlePowerOn())
        {
            // BLEでPower ONは、OPCのみ対応
            if (interfaceProvider.getCammeraConnectionMethod() == ICameraConnection.CameraConnectionMethod.OPC)
            {
                // BLEでカメラの電源をONにする設定だった時
                try
                {
                    // カメラの電源ONクラスを呼び出しておく (電源ONができたら、コールバックをもらう）
                    interfaceProvider.getOlympusInterface().getCameraPowerOn().wakeup(this);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (isAutoConnectCamera())
        {
            // 自動接続の指示があったとき
            changeCameraConnection();
        }
    }

    /**
     * フラグメントの初期化
     */
    private void initializeFragment()
    {
        try
        {
            //if (liveViewFragment == null)
            {
                liveViewFragment = LiveViewFragment.newInstance(this, interfaceProvider);
            }
            statusViewDrawer = liveViewFragment;
            liveViewFragment.setRetainInstance(true);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, liveViewFragment);
            transaction.commitAllowingStateLoss();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            ICameraConnection.CameraConnectionMethod method = interfaceProvider.getCammeraConnectionMethod();
            ICameraConnection connection = getCameraConnection(method);
            if (connection != null)
            {
                connection.stopWatchWifiStatus(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * カメラのプロパティ一覧画面を開く
     * （カメラと接続中のときのみ、接続方式が Olympusのときのみ）
     */
    @Override
    public void changeSceneToCameraPropertyList()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod method = interfaceProvider.getCammeraConnectionMethod();
            ICameraConnection connection = getCameraConnection(method);
            if (method == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
            {
                try
                {
                    // Ricohの場合は、コマンド送信ダイアログを表示する
                    RicohGr2SendCommandDialog.newInstance().show(getSupportFragmentManager(), "RicohGr2SendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (method == ICameraConnection.CameraConnectionMethod.SONY)
            {
                // SONYの場合は、API一覧画面へ遷移させる
                changeSceneToApiList();
            }
            else if (method == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                try
                {
                    // Panasonicの場合は、コマンド送信ダイアログを表示する
                    PanasonicSendCommandDialog.newInstance(interfaceProvider.getPanasonicInterface()).show(getSupportFragmentManager(), "panasonicSendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (method == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                try
                {
                    // FUJI X Seriesの場合は、コマンド送信ダイアログを表示する
                    FujiXCameraCommandSendDialog.newInstance(interfaceProvider.getFujiXInterface()).show(getSupportFragmentManager(), "sendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (method == ICameraConnection.CameraConnectionMethod.OLYMPUS)
            {
                try
                {
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
                    headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

                    // Olympus Penの場合は、コマンド送信ダイアログを表示する
                    SimpleHttpSendCommandDialog.newInstance("http://192.168.0.10/", interfaceProvider.getOlympusPenInterface().getLiveViewControl(), headerMap).show(getSupportFragmentManager(), "olympusPenSendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (method == ICameraConnection.CameraConnectionMethod.THETA)
            {
                try
                {
                    // THETA の場合は、HTTPコマンド送信ダイアログを表示する
                    SimpleHttpSendCommandDialog.newInstance("http://192.168.1.1/", null, null).show(getSupportFragmentManager(), "thetaSendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (method == ICameraConnection.CameraConnectionMethod.CANON)
            {
                try
                {
                    // CANON の場合は、PTPIPコマンド送信ダイアログを表示する
                    PtpIpCameraCommandSendDialog.newInstance(interfaceProvider.getCanonInterface(), true).show(getSupportFragmentManager(), "ptpipSendCommandDialog");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // OPC カメラの場合...
                if (connection != null)
                {
                    ICameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
                    if (status == ICameraConnection.CameraConnectionStatus.CONNECTED)
                    {
                        if (propertyListFragment == null)
                        {
                            propertyListFragment = OlyCameraPropertyListFragment.newInstance(this, interfaceProvider.getOlympusInterface().getCameraPropertyProvider());
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment1, propertyListFragment);
                        // backstackに追加
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   設定画面を開く
     *
     */
    @Override
    public void changeSceneToConfiguration()
    {
        try
        {
            if (preferenceFragment == null)
            {
                try
                {
                    ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();
                    if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2) {
                        preferenceFragment = RicohGr2PreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY) {
                        preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC) {
                        preferenceFragment = PanasonicPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.OLYMPUS) {
                        preferenceFragment = OlympusPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X) {
                        preferenceFragment = FujiXPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.THETA) {
                        preferenceFragment = ThetaPreferenceFragment.newInstance(this, this);
                    } else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON) {
                        preferenceFragment = CanonPreferenceFragment.newInstance(this, this);
                    } else //  if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
                    {
                        preferenceFragment = PreferenceFragment.newInstance(this, interfaceProvider, this);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    preferenceFragment = SonyPreferenceFragment.newInstance(this, this);
                }
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, preferenceFragment);
            // backstackに追加
            transaction.addToBackStack(null);
            transaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   デバッグ情報画面を開く
     *
     */
    @Override
    public void changeSceneToDebugInformation()
    {
        if (logCatFragment == null)
        {
            logCatFragment = LogCatFragment.newInstance();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, logCatFragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     *   SonyのAPI List画面を開く
     *
     */
    @Override
    public void changeSceneToApiList()
    {
        if (sonyApiListFragmentSony == null)
        {
            sonyApiListFragmentSony = SonyCameraApiListFragment.newInstance(interfaceProvider);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, sonyApiListFragmentSony);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     *   カメラとの接続・切断のシーケンス
     */
    @Override
    public void changeCameraConnection()
    {
        if (interfaceProvider == null)
        {
            Log.v(TAG, "changeCameraConnection() : interfaceProvider is NULL");
            return;
        }
        try
        {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                ICameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
                if (status == ICameraConnection.CameraConnectionStatus.CONNECTED)
                {
                    // 接続中のときには切断する
                    connection.disconnect(false);
                    return;
                }
                // 接続中でない時は、接続中にする
                connection.startWatchWifiStatus(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * アプリを抜ける
     */
    @Override
    public void exitApplication()
    {
        Log.v(TAG, "exitApplication()");
        try
        {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                connection.disconnect(true);
            }
            finish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onStatusNotify(String message)
    {
        Log.v(TAG, " CONNECTION MESSAGE : " + message);
        try
        {
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateStatusView(message);
                ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
                if (connection != null)
                {
                    statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onCameraConnected()
    {
        Log.v(TAG, "onCameraConnected()");

        try
        {
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                // クラス構造をミスった...のでこんなところで、無理やりステータスを更新する
                connection.forceUpdateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);
            }
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.CONNECTED);

                // ライブビューの開始...
                statusViewDrawer.startLiveView();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onCameraDisconnected()
    {
        Log.v(TAG, "onCameraDisconnected()");
        if (statusViewDrawer != null)
        {
            statusViewDrawer.updateStatusView(getString(R.string.camera_disconnected));
            statusViewDrawer.updateConnectionStatus(ICameraConnection.CameraConnectionStatus.DISCONNECTED);
        }
    }

    /**
     *
     *
     */
    @Override
    public void onCameraOccursException(String message, Exception e)
    {
        Log.v(TAG, "onCameraOccursException() " + message);
        try
        {
            e.printStackTrace();
            ICameraConnection connection = getCameraConnection(interfaceProvider.getCammeraConnectionMethod());
            if (connection != null)
            {
                connection.alertConnectingFailed(message + " " + e.getLocalizedMessage());
            }
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateStatusView(message);
                if (connection != null)
                {
                    statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
                }
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }

    /**
     *   BLE経由でカメラの電源を入れるかどうか
     *
     */
    private boolean isBlePowerOn()
    {
        boolean ret = false;
        try
        {
            if (interfaceProvider.getCammeraConnectionMethod() == ICameraConnection.CameraConnectionMethod.OPC)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                ret = preferences.getBoolean(IPreferencePropertyAccessor.BLE_POWER_ON, false);
                // Log.v(TAG, "isBlePowerOn() : " + ret);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }

    /**
     *    カメラへの自動接続を行うかどうか
     *
     */
    private boolean isAutoConnectCamera()
    {
        boolean ret = true;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            ret = preferences.getBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            // Log.v(TAG, "isAutoConnectCamera() : " + ret);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }

    /**
     *
     *
     *
     */
    private ICameraConnection getCameraConnection(ICameraConnection.CameraConnectionMethod connectionMethod)
    {
        ICameraConnection connection;
        if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
        {
            connection = interfaceProvider.getRicohGr2Infterface().getRicohGr2CameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
        {
            connection = interfaceProvider.getSonyInterface().getSonyCameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
        {
            connection = interfaceProvider.getPanasonicInterface().getPanasonicCameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
        {
            connection = interfaceProvider.getFujiXInterface().getFujiXCameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.OLYMPUS)
        {
            connection = interfaceProvider.getOlympusPenInterface().getOlyCameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.THETA)
        {
            connection = interfaceProvider.getThetaInterface().getCameraConnection();
        }
        else if  (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
        {
            connection = interfaceProvider.getCanonInterface().getCameraConnection();
        }
        else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
        {
            connection = interfaceProvider.getOlympusInterface().getOlyCameraConnection();
        }
        return (connection);
    }

    /**
     *   カメラへのBLE接続指示が完了したとき
     *
     * @param isExecuted  true : BLEで起動した, false : 起動していない、その他
     */
    @Override
    public void wakeupExecuted(boolean isExecuted)
    {
        Log.v(TAG, "wakeupExecuted() : " + isExecuted);
        if (isAutoConnectCamera())
        {
            // カメラへ自動接続する設定だった場合、カメラへWiFi接続する (BLEで起動しなくても)
            changeCameraConnection();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.v(TAG, "onKeyDown()" + " " + keyCode);
        try
        {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)&&
                    ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)||(keyCode == KeyEvent.KEYCODE_CAMERA)))
            {
                if (liveViewFragment != null)
                {
                    return (liveViewFragment.handleKeyDown(keyCode, event));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (super.onKeyDown(keyCode, event));
    }

    @Override
    public void updateMessage(final String message, final boolean isBold, final boolean isColor, final int color)
    {
        Log.v(TAG, " updateMessage() : " + message);
/*
        try {
            final TextView messageArea = findViewById(R.id.message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if ((messageArea != null) && (message != null))
                        {
                            messageArea.setText(message);
                            if (isBold)
                            {
                                messageArea.setTypeface(Typeface.DEFAULT_BOLD);
                            }
                            if (isColor)
                            {
                                messageArea.setTextColor(color);
                            }
                            else
                            {
                                messageArea.setTextColor(Color.DKGRAY);
                            }
                            messageArea.invalidate();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
    }
}
