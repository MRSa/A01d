package net.osdn.gokigen.a01d.camera.ricohgr2.operation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

public class RicohGr2SendCommandDialog  extends DialogFragment implements View.OnClickListener
{
    private final String TAG = toString();
    private Dialog myDialog = null;
    private EditText service = null;
    private EditText parameter = null;
    private EditText command = null;
    private TextView responseArea = null;
    private static final String SEND_URL = "http://192.168.0.1/";
    private static final int TIMEOUT_MS = 90000;

    /**
     *
     *
     */
    public static RicohGr2SendCommandDialog newInstance()
    {
        RicohGr2SendCommandDialog instance = new RicohGr2SendCommandDialog();

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("method", method);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "AlertDialog::onPause()");
        try
        {
            if (myDialog != null)
            {
                myDialog.cancel();
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
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();

        // コマンド送信ダイアログの生成
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.ricoh_request_layout, null, false);
        alertDialog.setView(alertView);

        alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp);
        alertDialog.setTitle(activity.getString(R.string.dialog_ricoh_command_title_command));
        service = alertView.findViewById(R.id.edit_service);
        parameter = alertView.findViewById(R.id.edit_parameter);
        command = alertView.findViewById(R.id.edit_command);
        responseArea = alertView.findViewById(R.id.ricoh_command_response_value);
        final Button sendButton = alertView.findViewById(R.id.send_message_button);

        sendButton.setOnClickListener(this);
        alertDialog.setCancelable(true);
        try
        {
            if (service != null)
            {
                service.setText(activity.getText(R.string.ricoh_service_string));
            }
            if (command != null)
            {
                command.setText(activity.getText(R.string.ricoh_get_status_string));
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
                        dialog.dismiss();
                    }
                });

        // ボタンを設定する (キャンセルボタン）
        alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // 確認ダイアログを応答する
        myDialog = alertDialog.create();
        return (myDialog);
    }

    @Override
    public void onClick(View view)
    {
        try
        {
            String serviceStr = "";
            String commandStr = "";
            String parameterStr = "";
            final Activity activity = getActivity();
            if (activity != null)
            {
                if (service != null)
                {
                    serviceStr = service.getText().toString();
                }
                if (command != null)
                {
                    commandStr = command.getText().toString();
                }
                if (parameter != null)
                {
                    parameterStr = parameter.getText().toString();
                }
                final String method = serviceStr;
                final String url = SEND_URL + commandStr;
                final String param = parameterStr;

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            String reply;
                            if (method.contains("OPTIONS"))
                            {
                                reply = SimpleHttpClient.httpOptions(url, param, TIMEOUT_MS);
                            }
                            else if (method.contains("PUT"))
                            {
                                reply = SimpleHttpClient.httpPut(url, param, TIMEOUT_MS);
                            }
                            else if (method.contains("POST"))
                            {
                                reply = SimpleHttpClient.httpPost(url, param, TIMEOUT_MS);
                            }
                            else // if (method.contains("GET"))
                            {
                                reply = SimpleHttpClient.httpGet(url + "?" + param, TIMEOUT_MS);
                            }
                            Log.v(TAG, method + " " + "" + url + " " + param + " RESPONSE : " + reply);
                            final String response = reply;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (responseArea != null)
                                    {
                                        responseArea.setText(response);
                                    }
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
            else
            {
                Log.v(TAG, "getActivity() Fail...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
