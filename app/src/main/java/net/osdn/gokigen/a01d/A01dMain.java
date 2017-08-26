package net.osdn.gokigen.a01d;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;
import net.osdn.gokigen.a01d.liveview.LiveViewFragment;
import net.osdn.gokigen.a01d.preference.PreferenceFragment;

/**
 *
 *
 */
public class A01dMain extends AppCompatActivity implements ICameraStatusReceiver, IChangeScene
{
    private final String TAG = toString();
    private IOlympusInterfaceProvider interfaceProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final int REQUEST_NEED_PERMISSIONS = 1010;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a01d_main);

        ActionBar bar = getSupportActionBar();
        if (bar != null)
        {
            // タイトルバーは表示しない
            bar.hide();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 外部メモリアクセス権のオプトイン
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED))
        {
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
     *   クラスの初期化
     *
     */
    private void initializeClass()
    {
        try
        {
            interfaceProvider = new OlympusInterfaceProvider(this, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   初期化終了時の処理
     *
     */
    private void onReadyClass()
    {


    }

    /**
     *   フラグメントの初期化
     *
     */
    private void initializeFragment()
    {
        LiveViewFragment fragment = new LiveViewFragment();
        fragment.prepare(this);
        fragment.setRetainInstance(true);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, fragment);
        transaction.commitAllowingStateLoss();
    }

    /**
     *   カメラのプロパティ一覧画面を開く
     *   （カメラと接続中のときのみ）
     *
     */
    @Override
    public void changeSceneToCameraPropertyList()
    {

    }

    /**
     *   設定画面を開く
     *
     */
    @Override
    public void changeSceneToConfiguration()
    {
        PreferenceFragment fragment = new PreferenceFragment();
        fragment.setInterface(this, interfaceProvider, this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment1, fragment);
        // backstackに追加
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /**
     *   カメラとの接続・切断のシーケンス
     *
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
        if (connection != null)
        {
            IOlyCameraConnection.CameraConnectionStatus status = connection.getConnectionStatus();
            if (status == IOlyCameraConnection.CameraConnectionStatus.CONNECTED)
            {
                //
                connection.disconnect(false);
                return;
            }
            connection.startWatchWifiStatus(this);
        }
    }

    /**
     *   アプリを抜ける
     *
     */
    @Override
    public void exitApplication()
    {
        Log.v(TAG, "exitApplication()");
        try
        {
            IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
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

    @Override
    public void onStatusNotify(String message)
    {
        Log.v(TAG, " CONNECTION MESSAGE : " + message);
    }


    @Override
    public void onCameraConnected()
    {
        Log.v(TAG, "onCameraConnected()");

        IOlyCameraConnection connection = interfaceProvider.getOlyCameraConnection();
        if (connection != null)
        {
            // クラス構造をミスった...のでこんなところで、無理やりステータスを更新する
            connection.forceUpdateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus.CONNECTED);
        }

    }

    @Override
    public void onCameraDisconnected()
    {
        Log.v(TAG, "onCameraDisconnected()");

    }

    @Override
    public void onCameraOccursException(String message, Exception e)
    {
        Log.v(TAG, "onCameraOccursException() " + message);

    }

    @Override
    public boolean isAutoConnectCamera()
    {
        return  (true);
    }
}
