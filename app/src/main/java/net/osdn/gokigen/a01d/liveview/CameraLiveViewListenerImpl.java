package net.osdn.gokigen.a01d.liveview;

import java.util.Map;

import jp.co.olympus.camerakit.OLYCamera;
import jp.co.olympus.camerakit.OLYCameraLiveViewListener;

/**
 *  OLYCameraLiveViewListener の実装
 *  （LiveViewFragment用）
 *
 */
public class CameraLiveViewListenerImpl implements OLYCameraLiveViewListener
{
    private IImageDataReceiver imageView = null;

    /**
     * コンストラクタ
     */
    public CameraLiveViewListenerImpl()
    {
        //
    }

    /**
     * 更新するImageViewを拾う
     *
     */
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

    /**
     * 　 CameraLiveImageView
     */
    public interface IImageDataReceiver
    {
        void setImageData(byte[] data, Map<String, Object> metadata);
    }
}
