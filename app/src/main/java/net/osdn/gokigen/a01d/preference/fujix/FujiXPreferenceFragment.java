package net.osdn.gokigen.a01d.preference.fujix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.fujix.operation.CameraPowerOffFujiX;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.EXIT_APPLICATION;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.SEND_MESSAGE_DIALOG;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.WIFI_SETTINGS;

/**
 *
 *
 */
public class FujiXPreferenceFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private IChangeScene changeScene = null;
    private SharedPreferences preferences = null;
    private CameraPowerOffFujiX powerOffController = null;

    /**
     *
     *
     */
    public static FujiXPreferenceFragment newInstance(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        FujiXPreferenceFragment instance = new FujiXPreferenceFragment();
        instance.prepare(context, changeScene);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        try
        {
            powerOffController = new CameraPowerOffFujiX(context, changeScene);
            powerOffController.prepare();

            this.context = context;
            this.changeScene = changeScene;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onAttach(@NonNull Context activity)
    {
        super.onAttach(activity);
        Log.v(TAG, "onAttach()");

        try
        {
            // Preference をつかまえる
            preferences = PreferenceManager.getDefaultSharedPreferences(activity);

            // Preference を初期設定する
            initializePreferences();

            preferences.registerOnSharedPreferenceChangeListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Preferenceの初期化...
     *
     */
    private void initializePreferences()
    {
        try
        {
            Map<String, ?> items = preferences.getAll();
            SharedPreferences.Editor editor = preferences.edit();

            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA)) {
                editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW)) {
                editor.putBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW)) {
                editor.putBoolean(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_FOCUS_XY)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_FOCUS_XY, IPreferencePropertyAccessor.FUJIX_FOCUS_XY_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT, IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT, IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD)) {
                editor.putString(IPreferencePropertyAccessor.CONNECTION_METHOD, IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ)) {
                editor.putBoolean(IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ, false);
            }
            editor.apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.v(TAG, "onSharedPreferenceChanged() : " + key);
        boolean value;
        try
        {
            if (key != null)
            {
                switch (key)
                {
                    case IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA:
                        value = preferences.getBoolean(key, true);
                        Log.v(TAG, " " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW:
                        value = preferences.getBoolean(key, true);
                        Log.v(TAG, " " + key + " ,  " + value);
                        break;

                    case IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW:
                        value = preferences.getBoolean(key, false);
                        Log.v(TAG, "   " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ:
                        value = preferences.getBoolean(key, false);
                        Log.v(TAG, "  " + key + " , " + value);
                        break;

                    default:
                        String strValue = preferences.getString(key, "");
                        setListPreference(key, key, strValue);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        Log.v(TAG, "onCreatePreferences()");
        try
        {
            //super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_fuji_x);

            ListPreference connectionMethod = findPreference(IPreferencePropertyAccessor.CONNECTION_METHOD);
            if (connectionMethod != null)
            {
                connectionMethod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                connectionMethod.setSummary(connectionMethod.getValue() + " ");
            }

            Preference exitApplication = findPreference(EXIT_APPLICATION);
            if (exitApplication != null)
            {
                exitApplication.setOnPreferenceClickListener(powerOffController);
            }

            Preference httpDialog = findPreference(SEND_MESSAGE_DIALOG);
            if (httpDialog != null)
            {
                httpDialog.setOnPreferenceClickListener(this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume() Start");

        try
        {
            synchronizedProperty();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "onResume() End");

    }

    /**
     *
     *
     */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "onPause() Start");

        try
        {
            // Preference変更のリスナを解除
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "onPause() End");
    }

    /**
     * ListPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private void setListPreference(String pref_key, String key, String defaultValue)
    {
        try
        {
            String value = preferences.getString(key, defaultValue);
            ListPreference pref = findPreference(pref_key);
            if (pref != null)
            {
                pref.setValue(value);
                pref.setSummary(value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * BooleanPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private void setBooleanPreference(String pref_key, String key, boolean defaultValue)
    {
        try
        {
            CheckBoxPreference pref = findPreference(pref_key);
            if (pref != null) {
                boolean value = preferences.getBoolean(key, defaultValue);
                pref.setChecked(value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    private void synchronizedProperty()
    {
        final FragmentActivity activity = getActivity();
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        // Preferenceの画面に反映させる
                        setBooleanPreference(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
                        setBooleanPreference(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
                        setBooleanPreference(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, false);
                        setBooleanPreference(IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ, IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ, false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        try
        {
            String preferenceKey = preference.getKey();
            if (preferenceKey.contains(WIFI_SETTINGS))
            {
                // Wifi 設定画面を表示する
                Log.v(TAG, " onPreferenceClick : " + preferenceKey);
                if (context != null)
                {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
            else if (preferenceKey.contains(SEND_MESSAGE_DIALOG))
            {
                // コマンド送信ダイアログを表示する
                if (changeScene != null)
                {
                    changeScene.changeSceneToCameraPropertyList(ICameraConnection.CameraConnectionMethod.FUJI_X);
                }
            }
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
