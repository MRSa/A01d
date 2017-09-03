package net.osdn.gokigen.a01d.liveview;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.operation.ICaptureControl;
import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraInformation;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraProperty;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

/**
 *
 *
 */
class LiveViewClickTouchListener implements View.OnClickListener, View.OnTouchListener
{
    private final String TAG = toString();
    private final Context context;
    private final ILiveImageStatusNotify statusNotify;
    private final IStatusViewDrawer statusViewDrawer;
    private final IChangeScene changeScene;
    private final IFocusingControl focusingControl;
    private final ICaptureControl captureControl;
    private final IOlyCameraPropertyProvider propertyProvider;
    private final ICameraInformation cameraInformation;

    LiveViewClickTouchListener(Context context, ILiveImageStatusNotify imageStatusNotify, IStatusViewDrawer statusView, IChangeScene changeScene, IOlympusInterfaceProvider interfaceProvider)
    {
        this.context = context;
        this.statusNotify = imageStatusNotify;
        this.statusViewDrawer = statusView;
        this.changeScene = changeScene;
        this.focusingControl = interfaceProvider.getFocusingControl();
        this.captureControl = interfaceProvider.getCaptureControl();
        this.propertyProvider = interfaceProvider.getCameraPropertyProvider();
        this.cameraInformation = interfaceProvider.getCameraInformation();
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
            // カメラで撮影する
            captureControl.doCapture(0);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (preferences.getBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true))
            {
                // ライブビュー画像も保管する
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
     *   オブジェクトをタッチする処理
     *
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        int id = view.getId();
        //Log.v(TAG, "onTouch() : " + id + " (" + motionEvent.getX() + "," + motionEvent.getY() + ")");
        return ((id == R.id.cameraLiveImageView)&&(focusingControl.driveAutoFocus(motionEvent)));
    }
}
