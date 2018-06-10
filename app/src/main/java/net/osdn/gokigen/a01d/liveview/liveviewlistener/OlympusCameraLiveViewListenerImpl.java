package net.osdn.gokigen.a01d.liveview.liveviewlistener;

import java.util.Map;

import jp.co.olympus.camerakit.OLYCamera;
import jp.co.olympus.camerakit.OLYCameraLiveViewListener;

/**
 *  OLYCameraLiveViewListener の実装
 *  （LiveViewFragment用）
 *
 */
public class OlympusCameraLiveViewListenerImpl implements OLYCameraLiveViewListener, ILiveViewListener
{
    private IImageDataReceiver imageView = null;

    /**
     * コンストラクタ
     */
    public OlympusCameraLiveViewListenerImpl()
    {
        //
    }

    /**
     * 更新するImageViewを拾う
     *
     */
    @Override
    public void setCameraLiveImageView(IImageDataReceiver target)
    {
        this.imageView = target;
    }

    /**
     * LiveViewの画像データを更新する
     *
     */
    @Override
    public void onUpdateLiveView(OLYCamera camera, byte[] data, Map<String, Object> metadata)
    {
        if (imageView != null)
        {
            imageView.setImageData(data, metadata);
        }
    }
}
