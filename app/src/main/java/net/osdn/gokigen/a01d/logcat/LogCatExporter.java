package net.osdn.gokigen.a01d.logcat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import net.osdn.gokigen.a01d.ConfirmationDialog;
import net.osdn.gokigen.a01d.R;

import androidx.annotation.NonNull;
import java.lang.ref.WeakReference;
import android.widget.Adapter;
import android.widget.Toast;

public class LogCatExporter implements AdapterView.OnItemLongClickListener {

    private static final String TAG = LogCatExporter.class.getSimpleName();
    private final WeakReference<Activity> activityRef;

    public LogCatExporter(@NonNull Activity activity) {
        // メモリーリーク防止のため WeakReference を使用
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int position, long id) {
        Log.v(TAG, "onItemLongClick()");

        final Activity activity = activityRef.get();
        // Activity がすでに破棄されている場合は処理を中断
        if (activity == null || activity.isFinishing())
        {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed())
            {
                return false;
            }
        }

        ConfirmationDialog confirm = ConfirmationDialog.newInstance(activity);

        confirm.show(
                R.string.dialog_confirm_title_output_log,
                R.string.dialog_confirm_message_output_log,
                () -> {
                    Log.v(TAG, "confirm()");

                    Activity currentActivity = activityRef.get();
                    if (currentActivity == null || currentActivity.isFinishing()) {
                        return;
                    }

                    exportLog(currentActivity, adapterView);
                }
        );
        return true;
    }

    private void exportLog(@NonNull Activity activity, @NonNull AdapterView<?> adapterView)
    {
        try {
            Adapter adapter = adapterView.getAdapter();
            if (adapter == null) {
                Log.w(TAG, "Adapter is null. Export canceled.");
                return;
            }

            StringBuilder buf = new StringBuilder();
            int count = adapter.getCount();
            for (int index = 0; index < count; index++) {
                Object item = adapter.getItem(index);
                if (item != null) {
                    buf.append(item).append("\r\n");
                }
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(
                    Intent.EXTRA_TITLE,
                    "debug log for " + activity.getString(R.string.app_name)
            );
            intent.putExtra(Intent.EXTRA_TEXT, buf.toString());

            // 共有を受け取れるアプリが存在するか検証して起動
            Intent chooser = Intent.createChooser(intent, null);
            activity.startActivity(chooser);

        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No app available to handle ACTION_SEND intent", e);
            Toast.makeText(activity, activity.getString(R.string.activity_does_not_found), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to export log", e);
            Toast.makeText(activity, activity.getString(R.string.log_export_failure), Toast.LENGTH_SHORT).show();
        }
    }
}
