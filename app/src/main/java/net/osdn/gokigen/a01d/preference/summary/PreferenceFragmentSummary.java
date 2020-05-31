package net.osdn.gokigen.a01d.preference.summary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import net.osdn.gokigen.a01d.ConfirmationDialog;
import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.logcat.LogCatViewer;
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

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.DEBUG_INFO;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.EXIT_APPLICATION;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_CANON_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_FUJI_X_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_NIKON_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_OLYMPUS_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_OPC_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_PANASONIC_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_RICOH_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_SONY_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.PREFERENCE_THETA_SETTINGS;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.WIFI_SETTINGS;


/**
 *
 *
 */
public class PreferenceFragmentSummary extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private SharedPreferences preferences = null;
    private IChangeScene changeScene = null;
    private LogCatViewer logCatViewer = null;

    /**
     *
     *
     */
    public static PreferenceFragmentSummary newInstance(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        PreferenceFragmentSummary instance = new PreferenceFragmentSummary();
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
            logCatViewer = new LogCatViewer(changeScene);
            logCatViewer.prepare();
            this.changeScene = changeScene;
            this.context = context;
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
        if (key != null)
        {
            switch (key)
            {
                case IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, "  " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                default:
                    String strValue = preferences.getString(key, "");
                    setListPreference(key, key, strValue);
                    break;
            }
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
            addPreferencesFromResource(R.xml.preferences_summary);

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

            Preference debug_info = findPreference(DEBUG_INFO);
            if (debug_info != null)
            {
                debug_info.setOnPreferenceClickListener(logCatViewer);
            }

            Preference wifi_settings = findPreference(WIFI_SETTINGS);
            if (wifi_settings != null)
            {
                wifi_settings.setOnPreferenceClickListener(this);
            }

            Preference opc_settings = findPreference(PREFERENCE_OPC_SETTINGS);
            if (opc_settings != null)
            {
                opc_settings.setOnPreferenceClickListener(this);
            }
            Preference olympus_settings = findPreference(PREFERENCE_OLYMPUS_SETTINGS);
            if (olympus_settings != null)
            {
                olympus_settings.setOnPreferenceClickListener(this);
            }
            Preference sony_settings = findPreference(PREFERENCE_SONY_SETTINGS);
            if (sony_settings != null)
            {
                sony_settings.setOnPreferenceClickListener(this);
            }
            Preference ricoh_settings = findPreference(PREFERENCE_RICOH_SETTINGS);
            if (ricoh_settings != null)
            {
                ricoh_settings.setOnPreferenceClickListener(this);
            }
            Preference theta_settings = findPreference(PREFERENCE_THETA_SETTINGS);
            if (theta_settings != null)
            {
                theta_settings.setOnPreferenceClickListener(this);
            }
            Preference fuji_settings = findPreference(PREFERENCE_FUJI_X_SETTINGS);
            if (fuji_settings != null)
            {
                fuji_settings.setOnPreferenceClickListener(this);
            }
            Preference panasonic_settings = findPreference(PREFERENCE_PANASONIC_SETTINGS);
            if (panasonic_settings != null)
            {
                panasonic_settings.setOnPreferenceClickListener(this);
            }
            Preference canon_settings = findPreference(PREFERENCE_CANON_SETTINGS);
            if (canon_settings != null)
            {
                canon_settings.setOnPreferenceClickListener(this);
            }
            Preference nikon_settings = findPreference(PREFERENCE_NIKON_SETTINGS);
            if (nikon_settings != null)
            {
                nikon_settings.setOnPreferenceClickListener(this);
            }

            Preference exitApplication = findPreference(EXIT_APPLICATION);
            if (exitApplication != null)
            {
                exitApplication.setOnPreferenceClickListener(this);
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
            if (pref != null)
            {
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
            else if (preferenceKey.contains(PREFERENCE_OPC_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.OPC);
            }
            else if (preferenceKey.contains(PREFERENCE_OLYMPUS_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.OLYMPUS);
            }
            else if (preferenceKey.contains(PREFERENCE_SONY_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.SONY);
            }
            else if (preferenceKey.contains(PREFERENCE_RICOH_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.RICOH_GR2);
            }
            else if (preferenceKey.contains(PREFERENCE_THETA_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.THETA);
            }
            else if (preferenceKey.contains(PREFERENCE_FUJI_X_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.FUJI_X);
            }
            else if (preferenceKey.contains(PREFERENCE_PANASONIC_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.PANASONIC);
            }
            else if (preferenceKey.contains(PREFERENCE_CANON_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.CANON);
            }
            else if (preferenceKey.contains(PREFERENCE_NIKON_SETTINGS))
            {
                changeScene.changeSceneToConfiguration(ICameraConnection.CameraConnectionMethod.NIKON);
            }
            else if (preferenceKey.contains(EXIT_APPLICATION))
            {
                // 確認ダイアログの生成と表示
                try
                {
                    ConfirmationDialog dialog = ConfirmationDialog.newInstance(context);
                    dialog.show(R.string.dialog_title_confirmation, R.string.dialog_message_exit_application, new ConfirmationDialog.Callback() {
                        @Override
                        public void confirm()
                        {
                            changeScene.exitApplication();
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
