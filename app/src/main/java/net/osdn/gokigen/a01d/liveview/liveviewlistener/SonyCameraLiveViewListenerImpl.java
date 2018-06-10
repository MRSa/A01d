package net.osdn.gokigen.a01d.liveview.liveviewlistener;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraLiveViewListener;

import java.util.Map;

public class SonyCameraLiveViewListenerImpl implements ILiveViewListener, ISonyCameraLiveViewListener
{
    private final String TAG = toString();
    private IImageDataReceiver imageView = null;

    /**
     * コンストラクタ
     */
    public SonyCameraLiveViewListenerImpl()
    {

    }

    /**
     * 更新するImageViewを拾う
     *
     */
    @Override
    public void setCameraLiveImageView(IImageDataReceiver target)
    {
        imageView = target;
    }

    /**
     * LiveViewの画像データを更新する
     *
     */
    @Override
    public void onUpdateLiveView(byte[] data, Map<String, Object> metadata)
    {
        if (imageView != null)
        {
            //Log.v(TAG, "onUpdateLiveView() " + data.length);
            imageView.setImageData(data, metadata);
        }
    }
}
