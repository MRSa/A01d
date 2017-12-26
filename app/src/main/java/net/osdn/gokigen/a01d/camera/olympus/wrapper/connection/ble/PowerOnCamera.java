package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.co.olympus.camerakit.OLYCamera;

/**
 *   BLE経由でカメラの電源を入れるクラス
 *
 */
public class PowerOnCamera implements ICameraPowerOn
{
    private final String TAG = toString();
    private final int BLE_SCAN_TIMEOUT_MILLIS = 5 * 1000; // 5秒間
    private final int BLE_WAIT_DURATION  = 100;             // 100ms間隔
    private final Context context;
    private final OLYCamera camera;
    private List<OlyCameraSetArrayItem> myCameraList;
    private BluetoothDevice myBluetoothDevice = null;
    private String myBtDevicePassCode = "";

    /**
     *
     */
    public PowerOnCamera(Context context, OLYCamera camera)
    {
        Log.v(TAG, "PowerOnCamera()");
        this.context = context;
        this.camera = camera;
        setupCameraList();
    }

    public void wakeup(final PowerOnCameraCallback callback)
    {
        Log.v(TAG, "PowerOnCamera::wakeup()");

        final BluetoothManager btMgr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // BLE のサービスを取得
            btMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (btMgr == null)
            {
                // Bluetooth LEのサポートがない場合は、何もしない
                Log.v(TAG, "PowerOnCamera::wakeup() NOT SUPPORT BLE...");

                // BLEの起動はしなかった...
                callback.wakeupExecuted(false);
                return;
            }
            final  List<OlyCameraSetArrayItem> deviceList = myCameraList;

            //  BLE_SCAN_TIMEOUT_MILLIS の間だけBLEのスキャンを実施する
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    {
                        class bleScanCallback implements BluetoothAdapter.LeScanCallback
                        {
                            @Override
                            public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes)
                            {
                                try
                                {
                                    final String btDeviceName = bluetoothDevice.getName();
                                    // Log.v(TAG, "onLeScan() " + btDeviceName);   // BluetoothDevice::getName() でログ出力してくれるので
                                    for (OlyCameraSetArrayItem device : deviceList)
                                    {
                                        final String btName = device.getBtName();
                                        // Log.v(TAG, "onLeScan() [" + btName + "]");
                                        if (btName.equals(btDeviceName))
                                        {
                                            // マイカメラ発見！
                                            // 別スレッドで起動する
                                            myBluetoothDevice = bluetoothDevice;
                                            myBtDevicePassCode = device.getBtPassCode();
                                            break;
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            private void reset()
                            {
                                try
                                {
                                    myBluetoothDevice = null;
                                    myBtDevicePassCode = "";
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        bleScanCallback scanCallback = new bleScanCallback();
                        try
                        {
                            // スキャン開始
                            scanCallback.reset();
                            BluetoothAdapter adapter = btMgr.getAdapter();
                            if (!adapter.startLeScan(scanCallback))
                            {
                                // Bluetooth LEのスキャンが開始できなかった場合...
                                Log.v(TAG, "Bluetooth LE SCAN START fail...");
                                callback.wakeupExecuted(false);
                                return;
                            }
                            Log.v(TAG, "BT SCAN STARTED");
                            int passed = 0;
                            while (passed < BLE_SCAN_TIMEOUT_MILLIS)
                            {
                                // BLEデバイスが見つかったときは抜ける...
                                if (myBluetoothDevice != null)
                                {
                                    break;
                                }

                                // BLEのスキャンが終わるまで待つ
                                Thread.sleep(BLE_WAIT_DURATION);
                                passed = passed + BLE_WAIT_DURATION;
                            }
                            // スキャンを止める
                            adapter.stopLeScan(scanCallback);
                            Log.v(TAG, "BT SCAN STOPPED");

                            if (myBluetoothDevice != null)
                            {
                                // カメラの起動
                                Log.v(TAG, "WAKE UP CAMERA : " + myBluetoothDevice.getName() + " [" + myBluetoothDevice.getAddress() + "]");
                                camera.setBluetoothDevice(myBluetoothDevice);
                                camera.setBluetoothPassword(myBtDevicePassCode);
                                camera.wakeup();
                                callback.wakeupExecuted(true);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...");
                            callback.wakeupExecuted(false);
                        }
                        Log.v(TAG, "Bluetooth LE SCAN STOPPED");
                    }
                }
            });
            thread.start();
        }
    }

    /**
     *
     *
     */
    private void setupCameraList()
    {
        myCameraList = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        for (int index = 1; index <= IOlyCameraEntryList.MAX_STORE_PROPERTIES; index++)
        {
            String idHeader = String.format(Locale.ENGLISH, "%03d", index);
            String prefDate = preferences.getString(idHeader + IOlyCameraEntryList.DATE_KEY, "");
            if (prefDate.length() <= 0)
            {
                // 登録が途中までだったとき
                break;
            }
            String btName = preferences.getString(idHeader + IOlyCameraEntryList.NAME_KEY, "");
            String btCode = preferences.getString(idHeader + IOlyCameraEntryList.CODE_KEY, "");
            myCameraList.add(new OlyCameraSetArrayItem(idHeader, btName, btCode, prefDate));
        }
        Log.v(TAG, "setupCameraList() : " + myCameraList.size());
    }

}
