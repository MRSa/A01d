package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.osdn.gokigen.a01d.ConfirmationDialog;
import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

/**
 *   Olympus AIR の Bluetooth設定を記録する
 *
 *
 */
public class OlyCameraPowerOnSelector implements android.support.v7.preference.Preference.OnPreferenceClickListener, ConfirmationDialog.Callback
{
    private final String TAG = toString();
    private final AppCompatActivity context;
    //private String preferenceKey = null;

    /**
     *   コンストラクタ
     *
     */
    public OlyCameraPowerOnSelector(AppCompatActivity context)
    {
        this.context = context;
    }

    /**
     *   クラスの準備
     *
     */
    public void prepare()
    {
        // 何もしない
    }

    /**
     *
     *
     * @param preference クリックしたpreference
     * @return false : ハンドルしない / true : ハンドルした
     */
    @Override
    public boolean onPreferenceClick(android.support.v7.preference.Preference preference)
    {
        Log.v(TAG, "onPreferenceClick() : ");
        if (!preference.hasKey())
        {
            return (false);
        }

        String preferenceKey = preference.getKey();
        if (preferenceKey.contains(IPreferencePropertyAccessor.OLYCAMERA_BLUETOOTH_SETTINGS))
        {
            try
            {
                // My Olympus Air登録用ダイアログを表示する
                OlyCameraEntryListDialog dialogFragment = OlyCameraEntryListDialog.newInstance(context.getString(R.string.pref_air_bt), context.getString(R.string.pref_summary_air_bt));
                dialogFragment.setRetainInstance(false);
                dialogFragment.setShowsDialog(true);
                dialogFragment.show(context.getSupportFragmentManager(), "dialog");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return (true);
        }
        return (false);
    }

    /**
     *
     *
     */
    @Override
    public void confirm()
    {
        Log.v(TAG, "confirm() ");
    }
}
