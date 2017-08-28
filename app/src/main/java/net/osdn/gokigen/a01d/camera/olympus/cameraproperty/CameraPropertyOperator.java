package net.osdn.gokigen.a01d.camera.olympus.cameraproperty;


import android.content.Context;
import android.util.Log;
import android.view.View;

import net.osdn.gokigen.a01d.ConfirmationDialog;
import net.osdn.gokigen.a01d.R;

public class CameraPropertyOperator implements View.OnClickListener
{
    private final String TAG = toString();

    private final Context context;
    private final CameraPropertyLoader loader;


    public CameraPropertyOperator(Context context, CameraPropertyLoader loader)
    {
        this.context = context;
        this.loader = loader;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        Log.v(TAG, "onClick() : " + id);
        switch (id)
        {
            case R.id.propertySettings_restore:
                processRestoreCameraProperty();
                break;

            default:
                break;
        }
    }

    private void processRestoreCameraProperty()
    {
        try
        {
            final ConfirmationDialog dialog = new ConfirmationDialog(context);
            dialog.show(R.string.dialog_title_confirmation,
                    R.string.dialog_message_restore_camera_property,
                    new ConfirmationDialog.Callback() {
                        @Override
                        public void confirm()
                        {
                            loader.resetProperty();
                        }
                    });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}