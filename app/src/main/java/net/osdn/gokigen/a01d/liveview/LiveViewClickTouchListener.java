package net.osdn.gokigen.a01d.liveview;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraProperty;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
class LiveViewClickTouchListener implements View.OnClickListener, View.OnTouchListener, View.OnKeyListener
{
    private final String TAG = toString();
    private final Context context;
    private final ILiveImageStatusNotify statusNotify;
    private final IStatusViewDrawer statusViewDrawer;
    private final IChangeScene changeScene;
    private final IInterfaceProvider interfaceProvider;
    private final IFocusingControl focusingControl;
    private final ICaptureControl captureControl;
    private final IOlyCameraPropertyProvider propertyProvider;
    private final ICameraInformation cameraInformation;
    private final ICameraConnection cameraConnection;
    private final IDialogKicker dialogKicker;
    private final IZoomLensControl zoomLensControl;

    LiveViewClickTouchListener(Context context, ILiveImageStatusNotify imageStatusNotify, IStatusViewDrawer statusView, IChangeScene changeScene, IInterfaceProvider interfaceProvider, IDialogKicker dialogKicker)
    {
        this.context = context;
        this.statusNotify = imageStatusNotify;
        this.statusViewDrawer = statusView;
        this.changeScene = changeScene;
        this.interfaceProvider = interfaceProvider;

        ICameraConnection.CameraConnectionMethod connectionMethod = interfaceProvider.getCammeraConnectionMethod();
        if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH_GR2)
        {
            this.focusingControl = interfaceProvider.getRicohGr2Infterface().getFocusingControl();
            this.captureControl = interfaceProvider.getRicohGr2Infterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getRicohGr2Infterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getRicohGr2Infterface().getRicohGr2CameraConnection();
            this.zoomLensControl = interfaceProvider.getRicohGr2Infterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
        {
            this.focusingControl = interfaceProvider.getSonyInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getSonyInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getSonyInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getSonyInterface().getSonyCameraConnection();
            this.zoomLensControl = interfaceProvider.getSonyInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
        {
            this.focusingControl = interfaceProvider.getFujiXInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getFujiXInterface().getCaptureControl();
            this.cameraInformation = interfaceProvider.getFujiXInterface().getCameraInformation();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraConnection = interfaceProvider.getFujiXInterface().getFujiXCameraConnection();
            this.zoomLensControl = interfaceProvider.getFujiXInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
        {
            this.focusingControl = interfaceProvider.getPanasonicInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getPanasonicInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getPanasonicInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getPanasonicInterface().getPanasonicCameraConnection();
            this.zoomLensControl = interfaceProvider.getPanasonicInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.OLYMPUS)
        {
            this.focusingControl = interfaceProvider.getOlympusPenInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getOlympusPenInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusPenInterface().getCameraPropertyProvider();
            this.cameraInformation = interfaceProvider.getOlympusPenInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getOlympusPenInterface().getOlyCameraConnection();
            this.zoomLensControl = interfaceProvider.getOlympusPenInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.THETA)
        {
            this.focusingControl = interfaceProvider.getThetaInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getThetaInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getThetaInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getThetaInterface().getCameraConnection();
            this.zoomLensControl = interfaceProvider.getThetaInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
        {
            this.focusingControl = interfaceProvider.getCanonInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getCanonInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getCanonInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getCanonInterface().getCameraConnection();
            this.zoomLensControl = interfaceProvider.getCanonInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
        {
            this.focusingControl = interfaceProvider.getNikonInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getNikonInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getNikonInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getNikonInterface().getCameraConnection();
            this.zoomLensControl = interfaceProvider.getNikonInterface().getZoomLensControl();
        }
        else if (connectionMethod == ICameraConnection.CameraConnectionMethod.KODAK)
        {
            this.focusingControl = interfaceProvider.getKodakInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getKodakInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();  // 要変更
            this.cameraInformation = interfaceProvider.getKodakInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getKodakInterface().getCameraConnection();
            this.zoomLensControl = interfaceProvider.getKodakInterface().getZoomLensControl();
        }
        else  // if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
        {
            this.focusingControl = interfaceProvider.getOlympusInterface().getFocusingControl();
            this.captureControl = interfaceProvider.getOlympusInterface().getCaptureControl();
            this.propertyProvider = interfaceProvider.getOlympusInterface().getCameraPropertyProvider();
            this.cameraInformation = interfaceProvider.getOlympusInterface().getCameraInformation();
            this.cameraConnection = interfaceProvider.getOlympusInterface().getOlyCameraConnection();
            this.zoomLensControl = interfaceProvider.getOlympusInterface().getZoomLensControl();
        }

        this.dialogKicker = dialogKicker;
    }

    /**
     *   オブジェクトをクリックする処理
     *
     */
    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        //Log.v(TAG, "onClick() : " + id);
        try
        {
            switch (id)
            {
                case R.id.show_hide_grid_button:
                    // グリッドの ON/OFF
                    statusNotify.toggleShowGridFrame();
                    statusViewDrawer.updateGridIcon();
                    break;

                case R.id.show_preference_button:
                    // カメラの設定
                    changeScene.changeSceneToConfiguration();
                    break;

                case R.id.camera_property_settings_button:
                    // カメラのプロパティ設定
                    changeScene.changeSceneToCameraPropertyList();
                    break;

                case R.id.connect_disconnect_button:
                    // カメラと接続・切断のボタンが押された
                    changeScene.changeCameraConnection();
                    break;

                case R.id.shutter_button:
                    // シャッターボタンが押された (撮影)
                    pushedShutterButton();
                    break;

                case R.id.focusing_button:
                    // AF と MFの切り替えボタンが押された
                    changeFocusingMode();
                    break;

                case R.id.live_view_scale_button:
                    //  ライブビューの倍率を更新する
                    statusViewDrawer.updateLiveViewScale(true);
                    break;

                case R.id.show_favorite_settings_button:
                    // お気に入り設定のダイアログを表示する
                    showFavoriteDialog();
                    break;

                case R.id.btn_zoomin:
                    // ズームインのボタンが押された
                    actionZoomin();
                    break;
                case R.id.btn_zoomout:
                    // ズームアウトのボタンが押された
                    actionZoomout();
                    break;

                case R.id.focus_indicator:
                    // フォーカスインジケータをクリックした
                    actionFocusButton();
                    break;

                default:
                    Log.v(TAG, "onClick() : " + id);
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void actionZoomin()
    {
        Log.v(TAG, "actionZoomin()");
        try
        {
            // ズーム可能な場合、ズームインする
            if (zoomLensControl.canZoom())
            {
                zoomLensControl.driveZoomLens(true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void actionZoomout()
    {
        Log.v(TAG, "actionZoomout()");
        try
        {
            // ズーム可能な場合、ズームアウトする
            if (zoomLensControl.canZoom())
            {
                zoomLensControl.driveZoomLens(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }




    /**
     *   シャッターボタンが押された時の処理
     *
     *
     */
    private void pushedShutterButton()
    {
        Log.v(TAG, "pushedShutterButton()");
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (!preferences.getBoolean(IPreferencePropertyAccessor.CAPTURE_ONLY_LIVE_VIEW, false))
            {
                // カメラで撮影する
                captureControl.doCapture(0);
            }
            if (preferences.getBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true))
            {
                // ライブビュー画像を保管する
                statusNotify.takePicture();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   AF/MFの切り替えを行う
     *
     */
    private void changeFocusingMode()
    {
        if ((propertyProvider == null)||(cameraInformation == null))
        {
            Log.v(TAG, "changeFocusingMode() : OBJECT IS NULL.");
            return;
        }
        try
        {
            boolean isManualFocus = cameraInformation.isManualFocus();
            if (!isManualFocus)
            {
                // AF ⇒ MF時には、オートフォーカスのロックを解除する
                focusingControl.unlockAutoFocus();
            }
            String value = (isManualFocus) ? IOlyCameraProperty.STILL_AF :  IOlyCameraProperty.STILL_MF;
            propertyProvider.setCameraPropertyValue(IOlyCameraProperty.FOCUS_STILL, value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   フォーカスボタンが押されたとき...
     *
     */
    private void actionFocusButton()
    {
        try
        {
            //　シャッターを半押しする
            if (focusingControl != null)
            {
                boolean isHalfPress = !statusViewDrawer.isFocusLocked();
                focusingControl.halfPressShutter(isHalfPress);
                Log.v(TAG, " actionFocusButton() : isHalfPress " + isHalfPress);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   お気に入り設定ダイアログの表示
     *
     */
    private void showFavoriteDialog()
    {
        Log.v(TAG, "showFavoriteDialog()");
        try
        {
            if (interfaceProvider.getCammeraConnectionMethod() == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                // FUJI X Seriesの場合は、カメラ状態を表示するダイアログを表示する
                if (cameraConnection.getConnectionStatus() == ICameraConnection.CameraConnectionStatus.CONNECTED)
                {
                    dialogKicker.showCameraStatusDialog();
                }
                return;
            }
            else if (interfaceProvider.getCammeraConnectionMethod() != ICameraConnection.CameraConnectionMethod.OPC)
            {
                // OPCカメラでない場合には、「OPCカメラのみ有効です」表示をして画面遷移させない
                Toast.makeText(context, context.getText(R.string.only_opc_feature), Toast.LENGTH_SHORT).show();
                return;
            }

            if (cameraConnection.getConnectionStatus() == ICameraConnection.CameraConnectionStatus.CONNECTED)
            {
                //  お気に入り設定のダイアログを表示する
                dialogKicker.showFavoriteSettingDialog();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   オブジェクトをタッチする処理
     *
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        int id = view.getId();
        if (focusingControl == null)
        {
            Log.v(TAG, "focusingControl is NULL.");
            return (false);
        }
        //Log.v(TAG, "onTouch() : " + id + " (" + motionEvent.getX() + "," + motionEvent.getY() + ")");
        return ((id == R.id.cameraLiveImageView)&&(focusingControl.driveAutoFocus(motionEvent)));
    }

    /**
     *   ボタンを押したときの対応
     *
     */
    @Override
    public boolean onKey(View view, int keyCode, @NonNull KeyEvent keyEvent)
    {
        Log.v(TAG, "onKey() : " + keyCode);
        try
        {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)&&
                    ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)||(keyCode == KeyEvent.KEYCODE_CAMERA)))
            {
                pushedShutterButton();
                return (true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
