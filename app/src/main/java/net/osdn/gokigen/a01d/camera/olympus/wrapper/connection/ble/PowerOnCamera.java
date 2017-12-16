package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import jp.co.olympus.camerakit.OACentralConfiguration;
import jp.co.olympus.camerakit.OLYCamera;

/**
 *   BLE経由でカメラの電源を入れるクラス
 *
 */
public class PowerOnCamera implements ICameraPowerOn
{
    private final String TAG = toString();
    private final OLYCamera camera;
    private String btName;
    private String btCode;

    /**
     *
     */
    public PowerOnCamera(OLYCamera camera)
    {
        Log.v(TAG, "PowerOnCamera()");
        this.camera = camera;
    }

    public void wakeup(PowerOnCameraCallback callback)
    {
        boolean isWakeup = true;
        Log.v(TAG, "PowerOnCamera::wakeup()");

        btName = "";
        btCode = "";
        try
        {
            OACentralConfiguration oa_central = OACentralConfiguration.load();
            btName = oa_central.getBleName();
            btCode = oa_central.getBleCode();
            Log.v(TAG, "Bluetooth BtName : '" + btName + "' btCode : '" + btCode + "' ");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
/*
        try
        {
            BluetoothDevice btDevice = null;
            camera.setBluetoothDevice(btDevice);
            camera.setBluetoothPassword(btCode);
            camera.wakeup();
            isWakeup = true;
        }
        catch (Exception e)
        {
            // 例外受信
            e.printStackTrace();
            isWakeup = false;
        }
*/
        // 今は暫定で返しちゃう
        callback.wakeupExecuted(isWakeup);
    }



/*
private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord){
        if (btName.equals(device.getName())){
            btAdapter.stopLeScan(this);
            btDevice = device;
            try {
                camera.setBluetoothDevice(btDevice);
                camera.setBluetoothPassword(btCode);
                camera.wakeup();
                camera.connect(OLYCamera.ConnectionType.BluetoothLE);
            } catch (OLYCameraKitException e) {
                Log.w(TAG, "To connect to the camera is failed: " + e.getMessage());
            }
        }
    }
};
*/


}
