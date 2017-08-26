package net.osdn.gokigen.a01d.liveview.bitmapconvert;

import android.graphics.Bitmap;

/**
 *
 *
 */
class ConvertNothing implements IPreviewImageConverter
{
    /**
     *   変換後のビットマップを応答する
     *
     * @return 変換後のビットマップ
     */
    @Override
    public Bitmap getModifiedBitmap(Bitmap src)
    {
        return (src);
    }
}
