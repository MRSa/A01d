package net.osdn.gokigen.a01d.camera.fujix.wrapper.connection;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandPublisher;

public class FujiXCameraModeChangeToPlayback implements View.OnClickListener
{
    private final String TAG = toString();
    private final IFujiXCommandPublisher publisher;
    private final IFujiXCommandCallback callback;

    public FujiXCameraModeChangeToPlayback(@NonNull IFujiXCommandPublisher publisher, @Nullable IFujiXCommandCallback callback)
    {
        this.publisher = publisher;
        this.callback = callback;
    }

    @Override
    public void onClick(View v)
    {
        Log.v(TAG, "onClick");

    }
}
