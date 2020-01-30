package net.osdn.gokigen.a01d.camera.ptpip.operation;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.DialogFragment;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;


public class PtpIpCameraCommandSendDialog  extends DialogFragment
{
    private final String TAG = toString();
    private Dialog myDialog = null;
    private IPtpIpCommandPublisher commandPublisher = null;
    private PtpIpCameraCommandResponse responseReceiver = null;
    private SparseArrayCompat<String> commandNameIndexArray;
    private boolean isDumpLog = true;

    private int selectedCommandIdPosition = 0;
    private int selectedBodyLengthPosition = 0;

    public static PtpIpCameraCommandSendDialog newInstance(@NonNull IPtpIpInterfaceProvider interfaceProvider, boolean isDumpLog)
    {
        PtpIpCameraCommandSendDialog instance = new PtpIpCameraCommandSendDialog();
        instance.prepare(interfaceProvider, isDumpLog);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("method", method);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    private void prepare(@NonNull IPtpIpInterfaceProvider interfaceProvider, boolean isDumpLog)
    {
        this.commandPublisher = interfaceProvider.getCommandPublisher();
        this.commandNameIndexArray = new SparseArrayCompat<>();
        this.isDumpLog = isDumpLog;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Activity activity = getActivity();
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        // Get the layout inflater
        LayoutInflater inflater = activity.getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.ptpip_request_command_layout, null, false);
        alertDialog.setView(alertView);

        alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp);
        alertDialog.setTitle(getString(R.string.dialog_ptpip_command_title_command));
        try
        {
            final TextView commandResponse = alertView.findViewById(R.id.command_response_value);
            final EditText edit_command_id = alertView.findViewById(R.id.edit_command_id);
            final EditText edit_message_body1 = alertView.findViewById(R.id.edit_message_body1);
            final EditText edit_message_body2 = alertView.findViewById(R.id.edit_message_body2);
            final EditText edit_message_body3 = alertView.findViewById(R.id.edit_message_body3);
            final EditText edit_message_body4 = alertView.findViewById(R.id.edit_message_body4);
            final Spinner selection_command_id = alertView.findViewById(R.id.spinner_selection_command_id);
            final Spinner selection_message_body_length = alertView.findViewById(R.id.spinner_selection_message_body_length);
            final Button sendButton = alertView.findViewById(R.id.send_message_button);
            final Button liveViewButton = alertView.findViewById(R.id.change_to_liveview);
            final Button playbackButton = alertView.findViewById(R.id.change_to_playback);

            responseReceiver = new PtpIpCameraCommandResponse(activity, commandResponse);

            initializeCommandSelection(activity, selection_command_id, edit_command_id);
            initializeBodyLengthSelection(activity, selection_message_body_length);

            sendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        //Log.v(TAG, "SEND COMMAND");
                        if (responseReceiver != null)
                        {
                            responseReceiver.clear();
                            int id = parseInt(edit_command_id);
                            int value1 = parseInt(edit_message_body1);
                            int value2 = parseInt(edit_message_body2);
                            int value3 = parseInt(edit_message_body3);
                            int value4 = parseInt(edit_message_body4);

                            if (selectedBodyLengthPosition == 0)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id));
                            }
                            else if (selectedBodyLengthPosition == 1)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id, 2, value1));
                            }
                            else if (selectedBodyLengthPosition == 2)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id, 4, value1));
                            }
                            else if (selectedBodyLengthPosition == 3)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id, 8, value1, value2));
                            }
                            else if (selectedBodyLengthPosition == 4)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id, 12, value1, value2, value3));
                            }
                            else // if (selectedBodyLengthPosition == 5)
                            {
                                commandPublisher.enqueueCommand(new PtpIpCommandGeneric(responseReceiver, id, isDumpLog, 0, id, 16, value1, value2, value3, value4));
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            if ((responseReceiver != null)&&(commandPublisher != null))
            {
                //liveViewButton.setOnClickListener(new FujiXCameraModeChangeToLiveView(commandPublisher, responseReceiver));
                //playbackButton.setOnClickListener(new FujiXCameraModeChangeToPlayback(commandPublisher, responseReceiver));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        alertDialog.setCancelable(true);

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(activity.getString(R.string.dialog_positive_execute),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //dialog.dismiss();
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
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "AlertDialog::onPause()");
        if (myDialog != null)
        {
            myDialog.cancel();
        }
    }

    private int parseInt(EditText area)
    {
        try
        {
            String value = (area.getText().toString()).toLowerCase();
            int index =  value.indexOf("x");
            if (index > 0)
            {
                value = value.substring(index + 1);
            }
            if (value.length() < 1)
            {
                // 未入力のときには０を返す
                return (0);
            }
            //int convertValue = (int)Long.parseLong(value, 16);
            //Log.v(TAG, String.format(Locale.US, "PARSED VALUE : 0x%08x (%d)", convertValue, convertValue));
            //return (convertValue);
            return ((int)Long.parseLong(value, 16));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "[" + area.getText().toString() + "]");
        }
        return (-1);
    }


    private ArrayAdapter<String> prepareCommandAdapter(@NonNull final Activity activity)
    {
        int position = 0;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        commandNameIndexArray.clear();

        // せっせとコマンドを入れていく...
        adapter.add("(Direct Input)");
        commandNameIndexArray.append(position++, "");

        return (adapter);
    }

    private void initializeCommandSelection(@NonNull final Activity activity, final Spinner spinner, final EditText commandIdArea)
    {
        try
        {
            commandIdArea.setText("");
            ArrayAdapter<String> adapter = prepareCommandAdapter(activity);
            spinner.setAdapter(adapter);
            spinner.setSelection(selectedCommandIdPosition);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    Log.v(TAG, "onItemSelected : " + position + " (" + id + ")");
                    try
                    {
                        selectedCommandIdPosition = position;
                        commandIdArea.setText(commandNameIndexArray.get(position, ""));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    Log.v(TAG, "onNothingSelected");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initializeBodyLengthSelection(final Activity activity, final Spinner spinner)
    {
        try
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
            adapter.add("0");
            adapter.add("2");
            adapter.add("4");
            adapter.add("8");
            adapter.add("12");
            adapter.add("16");

            spinner.setAdapter(adapter);
            spinner.setSelection(selectedBodyLengthPosition);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    Log.v(TAG, "onItemSelected : " + position + " (" + id + ")");
                    selectedBodyLengthPosition = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    Log.v(TAG, "onNothingSelected");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
