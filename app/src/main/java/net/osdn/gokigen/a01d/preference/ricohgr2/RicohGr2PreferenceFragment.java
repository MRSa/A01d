package net.osdn.gokigen.a01d.preference.ricohgr2;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.CameraPowerOffRicohGr2;
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

public class RicohGr2PreferenceFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private IChangeScene changeScene = null;
    private SharedPreferences preferences = null;
    private CameraPowerOffRicohGr2 powerOffController = null;

    /**
     *
     *
     */
    public static RicohGr2PreferenceFragment newInstance(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        RicohGr2PreferenceFragment instance = new RicohGr2PreferenceFragment();
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
            powerOffController = new CameraPowerOffRicohGr2(context, changeScene);
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

            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA))
            {
                editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW))
            {
                editor.putBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD))
            {
                editor.putString(IPreferencePropertyAccessor.CONNECTION_METHOD, IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_LCD_SLEEP))
            {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_LCD_SLEEP, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_LIVE_VIEW))
            {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_LIVE_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS))
            {
                editor.putBoolean(IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_DISPLAY_MODE))
            {
                editor.putString(IPreferencePropertyAccessor.GR2_DISPLAY_MODE, IPreferencePropertyAccessor.GR2_DISPLAY_MODE_DEFAULT_VALUE);
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
                        Log.v(TAG, "  " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.GR2_LCD_SLEEP:
                        value = preferences.getBoolean(key, false);
                        Log.v(TAG, "  " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.GR2_LIVE_VIEW:
                        value = preferences.getBoolean(key, true);
                        Log.v(TAG, "    " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS:
                        value = preferences.getBoolean(key, false);
                        Log.v(TAG, " " + key + " , " + value);
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
            addPreferencesFromResource(R.xml.preferences_ricoh_gr2);

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

            ListPreference displayMode = findPreference(IPreferencePropertyAccessor.GR2_DISPLAY_MODE);
            if (displayMode != null)
            {
                displayMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                displayMode.setSummary(displayMode.getValue() + " ");
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
            ListPreference pref;
            pref = findPreference(pref_key);
            String value = preferences.getString(key, defaultValue);
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
        final boolean defaultValue = true;
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        // Preferenceの画面に反映させる
                        setBooleanPreference(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.GR2_LCD_SLEEP, IPreferencePropertyAccessor.GR2_LCD_SLEEP, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.GR2_LIVE_VIEW, IPreferencePropertyAccessor.GR2_LIVE_VIEW, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS, IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS, false);
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
                // HTTP送信ダイアログを表示する
                if (changeScene != null)
                {
                    changeScene.changeSceneToCameraPropertyList(ICameraConnection.CameraConnectionMethod.RICOH_GR2);
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
