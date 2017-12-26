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
    private final int BLE_SCAN_TIMEOUT_MILLIS = 10 * 1000; // 10秒間
    private final Context context;
    private final OLYCamera camera;
    private List<OlyCameraSetArrayItem> myCameraList;

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

            //  10秒間だけBLEのスキャンを実施する
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    {
                        class bleScanCallback implements BluetoothAdapter.LeScanCallback
                        {
                            private List<String> checked = new ArrayList<>();
                            @Override
                            public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes)
                            {
                                try
                                {
                                    final String btDeviceName = bluetoothDevice.getName();
                                    Log.v(TAG, "onLeScan() " + btDeviceName);
                                    for (OlyCameraSetArrayItem device : deviceList)
                                    {
                                        final String btName = device.getBtName();
                                        // Log.v(TAG, "onLeScan() [" + btName + "]");
                                        if ((!checked.contains(btName))&&(btName.equals(btDeviceName)))
                                        {
                                            // 別スレッドで起動する
                                            final String passCode = device.getBtPassCode();
                                            Thread thread = new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    try
                                                    {
                                                        // カメラの起動
                                                        camera.setBluetoothDevice(bluetoothDevice);
                                                        camera.setBluetoothPassword(passCode);
                                                        //camera.wakeup();
                                                        callback.wakeupExecuted(true);
                                                        checked.add(btName);
                                                        Log.v(TAG, "WAKE UP! : " + btName + " [" + btDeviceName + "]");
                                                    }
                                                    catch (Exception e)
                                                    {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            thread.start();
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
                                    checked.clear();
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

                            // BLEのスキャン時間の間待つ
                            Thread.sleep(BLE_SCAN_TIMEOUT_MILLIS);

                            // スキャンを止める
                            adapter.stopLeScan(scanCallback);

                            Log.v(TAG, "BT SCAN STOPPED");
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

            // 今は暫定で返しちゃう
            // callback.wakeupExecuted(true);
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
