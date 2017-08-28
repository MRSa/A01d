package net.osdn.gokigen.a01d.liveview;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;

/**
 *
 *
 */
class LiveViewClickTouchListener implements View.OnClickListener, View.OnTouchListener
{
    private final String TAG = toString();
    private final ILiveImageStatusNotify statusNotify;
    private final IStatusViewDrawer statusViewDrawer;
    private final IChangeScene changeScene;
    private final IFocusingControl focusingControl;

    LiveViewClickTouchListener(ILiveImageStatusNotify imageStatusNotify, IStatusViewDrawer statusView, IChangeScene changeScene, IFocusingControl focusingControl)
    {
        this.statusNotify = imageStatusNotify;
        this.statusViewDrawer = statusView;
        this.changeScene = changeScene;
        this.focusingControl = focusingControl;
    }

    /**
     *   オブジェクトをクリックする処理
     *
     */
    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        Log.v(TAG, "onClick() " + id);
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
     *   オブジェクトをタッチする処理
     *
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        int id = view.getId();
        Log.v(TAG, "onTouch() : " + id);
        return ((id == R.id.cameraLiveImageView)&&(focusingControl.driveAutoFocus(motionEvent)));
    }
}
