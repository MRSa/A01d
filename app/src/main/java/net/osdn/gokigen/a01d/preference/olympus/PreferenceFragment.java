package net.osdn.gokigen.a01d.preference.olympus;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import net.osdn.gokigen.a01d.IChangeScene;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.IInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.OlyCameraPowerOnSelector;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraProperty;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.operation.CameraPowerOff;
import net.osdn.gokigen.a01d.logcat.LogCatViewer;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import jp.co.olympus.camerakit.OLYCamera;

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.WIFI_SETTINGS;


/**
 *   SettingFragment
 *
 */
public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, PreferenceSynchronizer.IPropertySynchronizeCallback, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private IOlyCameraPropertyProvider propertyInterface = null;
    private ICameraHardwareStatus hardwareStatusInterface = null;
    private ICameraRunMode changeRunModeExecutor = null;
    private CameraPowerOff powerOffController = null;
    private OlyCameraPowerOnSelector powerOnSelector = null;
    private LogCatViewer logCatViewer = null;
    private SharedPreferences preferences = null;
    private ProgressDialog busyDialog = null;
    private PreferenceSynchronizer preferenceSynchronizer = null;


    public static PreferenceFragment newInstance(@NonNull AppCompatActivity context, @NonNull IInterfaceProvider factory, @NonNull IChangeScene changeScene)
    {
        PreferenceFragment instance = new PreferenceFragment();
        instance.setInterface(context, factory, changeScene);

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
    private void setInterface(@NonNull AppCompatActivity context, @NonNull IInterfaceProvider factory, @NonNull IChangeScene changeScene)
    {
        Log.v(TAG, "setInterface()");
        this.propertyInterface = factory.getOlympusInterface().getCameraPropertyProvider();
        this.changeRunModeExecutor = factory.getOlympusInterface().getCameraRunMode();
        hardwareStatusInterface = factory.getOlympusInterface().getHardwareStatus();
        powerOffController = new CameraPowerOff(context, changeScene);
        powerOffController.prepare();
        powerOnSelector = new OlyCameraPowerOnSelector(context);
        powerOnSelector.prepare();
        logCatViewer = new LogCatViewer(changeScene);
        logCatViewer.prepare();

        this.context = context;
    }

    /**
     *
     *
     */
    @Override
    public void onAttach(Context activity)
    {
        super.onAttach(activity);
        Log.v(TAG, "onAttach()");

        // Preference をつかまえる
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (preferenceSynchronizer == null)
        {
            preferenceSynchronizer = new PreferenceSynchronizer(this.propertyInterface, preferences, this);
        }

        // Preference を初期設定する
        initializePreferences();

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Preferenceの初期化...
     */
    private void initializePreferences()
    {
        Map<String, ?> items = preferences.getAll();
        SharedPreferences.Editor editor = preferences.edit();

        if (!items.containsKey(IPreferencePropertyAccessor.TAKE_MODE))
        {
            editor.putString(IPreferencePropertyAccessor.TAKE_MODE, IPreferencePropertyAccessor.TAKE_MODE_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY))
        {
            editor.putString(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY, IPreferencePropertyAccessor.LIVE_VIEW_QUALITY_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL))
        {
            editor.putString(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.RAW))
        {
            editor.putBoolean(IPreferencePropertyAccessor.RAW, true);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.BLE_POWER_ON))
        {
            editor.putBoolean(IPreferencePropertyAccessor.BLE_POWER_ON, false);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA))
        {
            editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW))
        {
            editor.putBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL))
        {
            editor.putString(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE))
        {
            editor.putString(IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE, IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL))
        {
            editor.putString(IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL, IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.POWER_ZOOM_LEVEL))
        {
            editor.putString(IPreferencePropertyAccessor.POWER_ZOOM_LEVEL, IPreferencePropertyAccessor.POWER_ZOOM_LEVEL_DEFAULT_VALUE);
        }
        if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD))
        {
            editor.putString(IPreferencePropertyAccessor.CONNECTION_METHOD, IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE);
        }
        editor.apply();
    }

    /**
     *
     *
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        Log.v(TAG, "onCreatePreferences()");

        //super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        try
        {
            final HashMap<String, String> sizeTable = new HashMap<>();
            sizeTable.put("QVGA", "(320x240)");
            sizeTable.put("VGA", "(640x480)");
            sizeTable.put("SVGA", "(800x600)");
            sizeTable.put("XGA", "(1024x768)");
            sizeTable.put("QUAD_VGA", "(1280x960)");

            ListPreference liveViewQuality = (ListPreference) findPreference(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY);
            if (liveViewQuality != null)
            {
                liveViewQuality.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String key = (String) newValue;
                        preference.setSummary(newValue + " " + sizeTable.get(key));
                        return (true);
                    }
                });
                liveViewQuality.setSummary(liveViewQuality.getValue() + " " + sizeTable.get(liveViewQuality.getValue()));
            }

            ListPreference liveViewScale = (ListPreference) findPreference(IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE);
            if (liveViewScale != null)
            {
                liveViewScale.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                liveViewScale.setSummary(liveViewScale.getValue() + " ");
            }

            ListPreference digitalZoom = (ListPreference) findPreference(IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL);
            if (digitalZoom != null)
            {
                digitalZoom.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                digitalZoom.setSummary(digitalZoom.getValue() + " ");
            }

            ListPreference powerZoom = (ListPreference) findPreference(IPreferencePropertyAccessor.POWER_ZOOM_LEVEL);
            if (powerZoom != null)
            {
                powerZoom.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                powerZoom.setSummary(powerZoom.getValue() + " ");
            }

            ListPreference connectionMethod = (ListPreference) findPreference(IPreferencePropertyAccessor.CONNECTION_METHOD);
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
            findPreference("exit_application").setOnPreferenceClickListener(powerOffController);
            findPreference("olympus_air_bt").setOnPreferenceClickListener(powerOnSelector);
            //findPreference("debug_info").setOnPreferenceClickListener(logCatViewer);
            //findPreference(WIFI_SETTINGS).setOnPreferenceClickListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * ハードウェアのサマリ情報を取得し設定する
     */
    private void setHardwareSummary()
    {
        // レンズ状態
        findPreference("lens_status").setSummary(hardwareStatusInterface.getLensMountStatus());

        // メディア状態
        findPreference("media_status").setSummary(hardwareStatusInterface.getMediaMountStatus());

        // 焦点距離
        String focalLength;
        float minLength = hardwareStatusInterface.getMinimumFocalLength();
        float maxLength = hardwareStatusInterface.getMaximumFocalLength();
        float actualLength = hardwareStatusInterface.getActualFocalLength();
        if (minLength == maxLength)
        {
            focalLength = String.format(Locale.ENGLISH, "%3.0fmm", actualLength);
        }
        else
        {
            focalLength = String.format(Locale.ENGLISH, "%3.0fmm - %3.0fmm (%3.0fmm)", minLength, maxLength, actualLength);
        }
        findPreference("focal_length").setSummary(focalLength);

        // カメラのバージョン
        try
        {
            Map<String, Object> hardwareInformation = hardwareStatusInterface.inquireHardwareInformation();
            findPreference("camera_version").setSummary((String) hardwareInformation.get(OLYCamera.HARDWARE_INFORMATION_CAMERA_FIRMWARE_VERSION_KEY));

            // 取得した一覧はログに出力する。)
            Log.v(TAG, "- - - - - - - - - -");
            for (Map.Entry<String, Object> entry : hardwareInformation.entrySet())
            {
                String value = (String) entry.getValue();
                Log.v(TAG, entry.getKey() + " : " + value);
            }
            Log.v(TAG, "- - - - - - - - - -");
        }
        catch (Exception e)
        {
            findPreference("camera_version").setSummary("Unknown");
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    private void setCameraProperty(String name, String value)
    {
        try
        {
            String propertyValue = "<" + name + "/" + value + ">";
            Log.v(TAG, "setCameraProperty() : " + propertyValue);
            propertyInterface.setCameraPropertyValue(name, propertyValue);
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

        // 撮影モードかどうかを確認して、撮影モードではなかったら撮影モードに切り替える
        if ((changeRunModeExecutor != null) && (!changeRunModeExecutor.isRecordingMode()))
        {
            // Runモードを切り替える。（でも切り替えると、設定がクリアされてしまう...。
            changeRunModeExecutor.changeRunMode(true);
        }
        synchronizeCameraProperties(true);
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

        // Preference変更のリスナを解除
        preferences.unregisterOnSharedPreferenceChangeListener(this);

        Log.v(TAG, "onPause() End");
    }

    /**
     * カメラプロパティとPreferenceとの同期処理を実行
     */
    private void synchronizeCameraProperties(boolean isPropertyLoad)
    {
        // 実行中ダイアログを取得する
        busyDialog = new ProgressDialog(getActivity());
        busyDialog.setTitle(getString(R.string.dialog_title_loading_properties));
        busyDialog.setMessage(getString(R.string.dialog_message_loading_properties));
        busyDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        busyDialog.setCancelable(false);
        busyDialog.show();

        // データ読み込み処理（別スレッドで実行）
        if (isPropertyLoad)
        {
            new Thread(preferenceSynchronizer).start();
        }
    }

    /**
     * Preferenceが更新された時に呼び出される処理
     *
     * @param sharedPreferences sharedPreferences
     * @param key               変更されたキー
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.v(TAG, "onSharedPreferenceChanged() : " + key);
        String propertyValue;
        boolean value;
        try
        {
            if (key != null)
            {
                switch (key)
                {
                    case IPreferencePropertyAccessor.RAW:
                        value = preferences.getBoolean(key, true);
                        setBooleanPreference(key, key, value);
                        propertyValue = (value) ? "ON" : "OFF";
                        setCameraProperty(IOlyCameraProperty.RAW, propertyValue);
                        break;

                    case IPreferencePropertyAccessor.BLE_POWER_ON:
                        value = preferences.getBoolean(key, false);
                        Log.v(TAG, " " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA:
                        value = preferences.getBoolean(key, true);
                        Log.v(TAG, " " + key + " , " + value);
                        break;

                    case IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW:
                        value = preferences.getBoolean(key, true);
                        Log.v(TAG, "  " + key + " , " + value);
                        break;

                    default:
                        String strValue = preferences.getString(key, "");
                        setListPreference(key, key, strValue);
                        String propertyKey = convertKeyFromPreferenceToCameraPropertyKey(key);
                        if (propertyKey != null)
                        {
                            setCameraProperty(propertyKey, strValue);
                        }
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
     * ListPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private void setListPreference(String pref_key, String key, String defaultValue)
    {
        ListPreference pref;
        pref = (ListPreference) findPreference(pref_key);
        String value = preferences.getString(key, defaultValue);
        if (pref != null)
        {
            pref.setValue(value);
            pref.setSummary(value);
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
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(pref_key);
        if (pref != null)
        {
            boolean value = preferences.getBoolean(key, defaultValue);
            pref.setChecked(value);
        }
    }

    /**
     *
     *
     */
    private String convertKeyFromPreferenceToCameraPropertyKey(String key)
    {
        String target = null;
        if (key == null)
        {
            return (null);
        }
        switch (key)
        {
            case IPreferencePropertyAccessor.TAKE_MODE:
                target = IOlyCameraProperty.TAKE_MODE;
                break;

            case IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL:
                target = IOlyCameraProperty.SOUND_VOLUME_LEVEL;
                break;

            default:
                // target == null
                break;
        }
        return (target);
    }

    /**
     * カメラプロパティの同期処理終了通知
     */
    @Override
    public void synchronizedProperty()
    {
        FragmentActivity activity = getActivity();
        if (activity == null)
        {
            try
            {
                busyDialog.dismiss();
                busyDialog = null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Preferenceの画面に反映させる
                    setListPreference(IPreferencePropertyAccessor.TAKE_MODE, IPreferencePropertyAccessor.TAKE_MODE, IPreferencePropertyAccessor.TAKE_MODE_DEFAULT_VALUE);
                    setListPreference(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL_DEFAULT_VALUE);
                    setBooleanPreference(IPreferencePropertyAccessor.RAW, IPreferencePropertyAccessor.RAW, true);
                    setBooleanPreference(IPreferencePropertyAccessor.BLE_POWER_ON, IPreferencePropertyAccessor.BLE_POWER_ON, false);
                    setBooleanPreference(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);

                    // カメラキットのバージョン
                    findPreference(IPreferencePropertyAccessor.CAMERAKIT_VERSION).setSummary(OLYCamera.getVersion());
                    if (hardwareStatusInterface != null)
                    {
                        // その他のハードウェア情報の情報設定
                        setHardwareSummary();
                    }

                    // 実行中ダイアログを消す
                    busyDialog.dismiss();
                    busyDialog = null;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
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
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
