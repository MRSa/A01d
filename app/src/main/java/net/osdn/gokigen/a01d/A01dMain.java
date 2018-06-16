package net.osdn.gokigen.a01d;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import net.osdn.gokigen.a01d.camera.CameraInterfaceProvider;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.cameraproperty.OlyCameraPropertyListFragment;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn;
import net.osdn.gokigen.a01d.camera.sony.cameraproperty.SonyCameraApiListFragment;
import net.osdn.gokigen.a01d.liveview.IStatusViewDrawer;
import net.osdn.gokigen.a01d.liveview.LiveViewFragment;
import net.osdn.gokigen.a01d.logcat.LogCatFragment;
import net.osdn.gokigen.a01d.preference.olympus.IPreferencePropertyAccessor;
import net.osdn.gokigen.a01d.preference.olympus.PreferenceFragment;
import net.osdn.gokigen.a01d.preference.sony.SonyPreferenceFragment;

/**
 *   A01d ;
 *
 */
public class A01dMain extends AppCompatActivity implements ICameraStatusReceiver, IChangeScene, ICameraPowerOn.PowerOnCameraCallback
{
    private final String TAG = toString();
    private IInterfaceProvider interfaceProvider = null;
    private IStatusViewDrawer statusViewDrawer = null;

    private PreferenceFragmentCompat preferenceFragment = null;
    private OlyCameraPropertyListFragment propertyListFragment = null;
    private SonyCameraApiListFragment sonyApiListFragmentSony = null;
    private LogCatFragment logCatFragment = null;

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
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String  permissions[], @NonNull int[] grantResults)
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
            interfaceProvider = new CameraInterfaceProvider(this, this);
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
            if (interfaceProvider.useOlympusCamera())
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
            LiveViewFragment fragment = LiveViewFragment.newInstance(this, interfaceProvider);
            statusViewDrawer = fragment;
            fragment.setRetainInstance(true);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, fragment);
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
            ICameraConnection connection = getCameraConnection();
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
            ICameraConnection connection = getCameraConnection();
            if (!useOlympusCamera())
            {
                // OPCカメラでない場合には、「OPCカメラのみ有効です」表示をして画面遷移させない
                Toast.makeText(getApplicationContext(), getText(R.string.only_opc_feature), Toast.LENGTH_SHORT).show();

                /*   // フラグメントができたらつなぐ
                if (connection != null)
                {
                    ICameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
                    if (status == ICameraConnection.CameraConnectionStatus.CONNECTED)
                    {
                        if (propertyListFragmentSony == null)
                        {
                            propertyListFragmentSony = SonyCameraApiListFragment.newInstance(this, interfaceProvider);
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment1, propertyListFragmentSony);
                        // backstackに追加
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
                */
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
            if (preferenceFragment == null) {
                if (useOlympusCamera())
                {
                    preferenceFragment = PreferenceFragment.newInstance(this, interfaceProvider, this);
                }
                else
                {
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
            ICameraConnection connection = getCameraConnection();
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
            ICameraConnection connection = getCameraConnection();
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
                ICameraConnection connection = getCameraConnection();
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
            ICameraConnection connection = getCameraConnection();
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
            ICameraConnection connection = getCameraConnection();
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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (useOlympusCamera())
            {
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
     *   OPCカメラを使用するかどうか
     *
     * @return  true : OPCカメラ /  false : OPCカメラではない
     */
    private boolean useOlympusCamera()
    {
        boolean ret = true;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String connectionMethod = preferences.getString(IPreferencePropertyAccessor.CONNECTION_METHOD, "OPC");
            ret = connectionMethod.contains("OPC");
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
    private ICameraConnection getCameraConnection()
    {
        ICameraConnection connection;
        if (interfaceProvider.useOlympusCamera())
        {
            connection = interfaceProvider.getOlympusInterface().getOlyCameraConnection();
        } else
        {
            connection = interfaceProvider.getSonyInterface().getSonyCameraConnection();
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
}
