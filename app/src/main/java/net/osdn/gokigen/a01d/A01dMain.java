package net.osdn.gokigen.a01d;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

import net.osdn.gokigen.a01d.camera.olympus.IOlympusDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.cameraproperty.OlyCameraPropertyListFragment;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;
import net.osdn.gokigen.a01d.liveview.IStatusViewDrawer;
import net.osdn.gokigen.a01d.liveview.LiveViewFragment;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.a01d.preference.PreferenceFragment;

/**
 *   A01d ;
 *
 */
public class A01dMain extends AppCompatActivity implements ICameraStatusReceiver, IChangeScene
{
    private final String TAG = toString();
    private IOlympusInterfaceProvider interfaceProvider = null;
    private IOlympusDisplayInjector interfaceInjector = null;
    private IStatusViewDrawer statusViewDrawer = null;

    private PreferenceFragment preferenceFragment = null;
    private OlyCameraPropertyListFragment propertyListFragment = null;

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
                (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE,
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
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
            OlympusInterfaceProvider provider = new OlympusInterfaceProvider(this, this);
            interfaceProvider = provider;
            interfaceInjector = provider;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 初期化終了時の処理
     */
    private void onReadyClass()
    {
        // 自動接続の指示があったとき
        if (isAutoConnectCamera())
        {
            changeCameraConnection();
        }
    }

    /**
     * フラグメントの初期化
     */
    private void initializeFragment()
    {
        LiveViewFragment fragment = new LiveViewFragment();
        statusViewDrawer = fragment;
        fragment.prepare(this, interfaceProvider, interfaceInjector);
        fragment.setRetainInstance(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, fragment);
        transaction.commitAllowingStateLoss();
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
            if (connection != null) {
                connection.stopWatchWifiStatus(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * カメラのプロパティ一覧画面を開く
     * （カメラと接続中のときのみ）
     */
    @Override
    public void changeSceneToCameraPropertyList()
    {
        IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
        if (connection != null) {
            IOlyCameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
            if (status == IOlyCameraConnection.CameraConnectionStatus.CONNECTED) {
                if (propertyListFragment == null) {
                    propertyListFragment = new OlyCameraPropertyListFragment();
                }
                propertyListFragment.setInterface(this, interfaceProvider.getCameraPropertyProvider());
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment1, propertyListFragment);
                // backstackに追加
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

    /**
     * 設定画面を開く
     */
    @Override
    public void changeSceneToConfiguration()
    {
        if (preferenceFragment == null)
        {
            preferenceFragment = new PreferenceFragment();
        }
        preferenceFragment.setInterface(this, interfaceProvider, this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, preferenceFragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * カメラとの接続・切断のシーケンス
     */
    @Override
    public void changeCameraConnection()
    {
        if (interfaceProvider == null)
        {
            Log.v(TAG, "changeCameraConnection() : interfaceProvider is NULL");
            return;
        }

        IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
        if (connection != null) {
            IOlyCameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
            if (status == IOlyCameraConnection.CameraConnectionStatus.CONNECTED)
            {
                // 接続中のときには切断する
                connection.disconnect(false);
                return;
            }
            // 接続中でない時は、接続中にする
            connection.startWatchWifiStatus(this);
        }
    }

    /**
     * アプリを抜ける
     */
    @Override
    public void exitApplication() {
        Log.v(TAG, "exitApplication()");
        try {
            IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
            if (connection != null) {
                connection.disconnect(true);
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onStatusNotify(String message) {
        Log.v(TAG, " CONNECTION MESSAGE : " + message);
        if (statusViewDrawer != null) {
            statusViewDrawer.updateStatusView(message);
            IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
            if (connection != null) {
                statusViewDrawer.updateConnectionStatus(connection.getConnectionStatus());
            }
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

        try {
            IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
            if (connection != null)
            {
                // クラス構造をミスった...のでこんなところで、無理やりステータスを更新する
                connection.forceUpdateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus.CONNECTED);
            }
            if (statusViewDrawer != null)
            {
                statusViewDrawer.updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus.CONNECTED);

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
            statusViewDrawer.updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus.DISCONNECTED);
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
        IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
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
}
