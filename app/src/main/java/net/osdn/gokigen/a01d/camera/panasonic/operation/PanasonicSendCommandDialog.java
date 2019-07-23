package net.osdn.gokigen.a01d.camera.panasonic.operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import net.osdn.gokigen.a01d.R;

public class PanasonicSendCommandDialog  extends DialogFragment
{
    private final String TAG = toString();
    private PanasonicSendCommandDialog.Callback callback;
    Dialog myDialog = null;

    /**
     *
     *
     */
    public static PanasonicSendCommandDialog newInstance(@Nullable PanasonicSendCommandDialog.Callback callback)
    {
        PanasonicSendCommandDialog instance = new PanasonicSendCommandDialog();
        instance.prepare(callback);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("method", method);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@Nullable PanasonicSendCommandDialog.Callback callback)
    {
        this.callback = callback;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "AlertDialog::onPause()");
        if (myDialog != null)
        {
            myDialog.cancel();
        }
    }

    /**
     *
     *
     */
    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();

        // コマンド送信ダイアログの生成
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.panasonic_request_layout, null, false);
        alertDialog.setView(alertView);

        alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp);
        alertDialog.setTitle(activity.getString(R.string.dialog_fujix_command_title_command));
        //final TextView titleName = alertView.findViewById(R.id.method_name);
        final EditText service = alertView.findViewById(R.id.edit_service);
        final EditText parameter = alertView.findViewById(R.id.edit_parameter);
        final EditText command = alertView.findViewById(R.id.edit_command);
        alertDialog.setCancelable(true);

        try
        {
            if (service != null)
            {
                service.setText(activity.getText(R.string.panasonic_service_string));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(activity.getString(R.string.dialog_positive_execute),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            Activity activity = getActivity();
                            if (activity != null)
                            {
                                if (callback != null)
                                {
                                    callback.sendPanasonicCommandRequest(service.getText().toString(), command.getText().toString(), parameter.getText().toString());
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            if (callback != null)
                            {
                                callback.cancelledPanasonicCommandRequest();
                            }
                        }
                        dialog.dismiss();
                    }
                });

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null)
                        {
                            callback.cancelledPanasonicCommandRequest();
                        }
                        dialog.cancel();
                    }
                });

        // 確認ダイアログを応答する
        myDialog = alertDialog.create();
        return (myDialog);
    }

    /**
     * コールバックインタフェース
     *
     */
    public interface Callback
    {
        void sendPanasonicCommandRequest(String service, String command, String parameter); // OKを選択したとき
        void cancelledPanasonicCommandRequest();   // キャンセルしたとき
    }
}
