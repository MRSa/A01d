package net.osdn.gokigen.a01d.liveview.bitmapconvert;

import android.graphics.Bitmap;

/**
 *   ビットマップ変換
 */
public interface IPreviewImageConverter
{
    Bitmap getModifiedBitmap(Bitmap src);
}
