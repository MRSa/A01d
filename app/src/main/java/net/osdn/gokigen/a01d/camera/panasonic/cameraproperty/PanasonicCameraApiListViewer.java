package net.osdn.gokigen.a01d.camera.panasonic.cameraproperty;

import android.util.Log;

import net.osdn.gokigen.a01d.IChangeScene;

import androidx.preference.Preference;

/**
 *
 *
 */
public class PanasonicCameraApiListViewer implements Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private final IChangeScene changeScene;

    /**
     *
     *
     */
    public PanasonicCameraApiListViewer(IChangeScene changeScene)
    {
        this.changeScene = changeScene;
    }

    /**
     *
     *
     */    public void prepare()
    {
        Log.v(TAG, "prepare() ");
    }

    /**
     *
     *
     */
    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (!preference.hasKey())
        {
            return (false);
        }

        String preferenceKey = preference.getKey();
        if ((preferenceKey.contains("panasonic_api_list"))&&(changeScene != null))
        {
            try
            {
                // API Listを表示する
                changeScene.changeSceneToApiList();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return (true);
        }
        return (false);
    }
}
