package net.osdn.gokigen.a01d.camera.sony.cameraproperty;

import android.support.v7.preference.Preference;
import android.util.Log;

import net.osdn.gokigen.a01d.IChangeScene;

/**
 *
 *
 */
public class SonyCameraApiListViewer implements android.support.v7.preference.Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private final IChangeScene changeScene;

    /**
     *
     *
     */    public SonyCameraApiListViewer(IChangeScene changeScene)
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
        if ((preferenceKey.contains("sony_api_list"))&&(changeScene != null))
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
