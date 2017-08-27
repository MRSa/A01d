package net.osdn.gokigen.a01d.liveview;

import android.util.Log;
import android.view.View;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;

/**
 *
 *
 */
class LiveViewClickListener implements View.OnClickListener
{
    final String TAG = toString();
    final ILiveImageStatusNotify statusNotify;
    final IStatusViewDrawer statusViewDrawer;
    final IChangeScene changeScene;


    LiveViewClickListener(ILiveImageStatusNotify imageStatusNotify, IStatusViewDrawer statusView, IChangeScene changeScene)
    {
        this.statusNotify = imageStatusNotify;
        this.statusViewDrawer = statusView;
        this.changeScene = changeScene;
    }


    @Override
    public void onClick(View view)
    {
        int id = view.getId();
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
}
