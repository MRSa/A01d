package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.util.Log;

import androidx.annotation.NonNull;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 *   Canonサムネイル画像の受信
 *
 *
 */
/*
public class CanonLiveViewImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final ICanonLiveViewImageCallback callback;
    private ByteArrayOutputStream byteStream;

    CanonLiveViewImageReceiver(@NonNull ICanonLiveViewImageCallback callback)
    {
        this.callback = callback;
        byteStream = new ByteArrayOutputStream();
    }


    public void reset()
    {
        try
        {
            byteStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        byteStream.reset();
    }


    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body == null)
            {
                Log.v(TAG, " BITMAP IS NONE...");
                callback.onCompleted(null, null);
                return;
            }
            Log.v(TAG, " CanonLiveViewImageReceiver::receivedMessage() : " + rx_body.length);

            /////// 受信データから、サムネイルの先頭(0xff 0xd8)を検索する  /////
            int offset = rx_body.length - 22;
            //byte[] thumbnail0 = Arrays.copyOfRange(rx_body, 0, rx_body.length);
            while (offset > 32)
            {
                if ((rx_body[offset] == (byte) 0xff)&&((rx_body[offset + 1] == (byte) 0xd8)))
                {
                    break;
                }
                offset--;
            }
            byte[] thumbnail = Arrays.copyOfRange(rx_body, offset, rx_body.length);
            callback.onCompleted(thumbnail, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            {
                callback.onErrorOccurred(e);
            }
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
        try
        {
            if (byteStream != null)
            {
                byteStream.write(body, 0, currentBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (true);
    }
}

*/


public class CanonLiveViewImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();

    private ICanonLiveViewImageCallback callback;

    private int received_total_bytes = 0;
    private int received_remain_bytes = 0;

    private int target_image_size = 0;
    private boolean receivedFirstData = false;
    private ByteArrayOutputStream byteStream;

    CanonLiveViewImageReceiver(@NonNull ICanonLiveViewImageCallback callback)
    {
        this.callback = callback;
        byteStream = new ByteArrayOutputStream();
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            // end of receive sequence.
            //byte [] thumbnail = byteStream.toByteArray();
            //Log.v(TAG, " TransferComplete() RECEIVED  : " + id + " size : " + target_image_size + " (" + thumbnail.length + ")");
            //SimpleLogDumper.dump_bytes(" [xxxxx]", Arrays.copyOfRange(thumbnail, 0, (64)));
            //SimpleLogDumper.dump_bytes(" [zzzzz]", Arrays.copyOfRange(thumbnail, (thumbnail.length - 64), (thumbnail.length)));
            callback.onCompleted(byteStream.toByteArray(), null);
            receivedFirstData = false;
            received_remain_bytes = 0;
            received_total_bytes = 0;
            target_image_size = 0;

            byteStream.reset();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            {
                callback.onErrorOccurred(e);
            }
        }
    }

    @Override
    public void onReceiveProgress(final int currentBytes, final int totalBytes, byte[] rx_body)
    {
        // 受信したデータから、通信のヘッダ部分を削除する
        cutHeader(rx_body);
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes);
    }

    private void cutHeader(byte[] rx_body)
    {
        if (rx_body == null)
        {
            return;
        }
        int length = rx_body.length;
        int data_position = 0;
        if (!receivedFirstData)
        {
            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            receivedFirstData = true;
            data_position = (int) rx_body[0] & (0xff);
            //Log.v(TAG, " FIRST DATA POS. : " + data_position);
            //SimpleLogDumper.dump_bytes(" [sssss]", Arrays.copyOfRange(rx_body, 0, (64)));
        }
        else if (received_remain_bytes > 0)
        {
            // データの読み込みが途中だった場合...
            if (length < received_remain_bytes)
            {
                // 全部コピーする、足りないバイト数は残す
                received_remain_bytes = received_remain_bytes - length;
                received_total_bytes = received_total_bytes + rx_body.length;
                byteStream.write(rx_body, 0, rx_body.length);
                return;
            }
            else
            {
                byteStream.write(rx_body, data_position, received_remain_bytes);
                data_position = received_remain_bytes;
                received_remain_bytes = 0;
            }
        }

        while (data_position <= (length - 12)) {
            int body_size = (rx_body[data_position] & 0xff) + ((rx_body[data_position + 1] & 0xff) << 8) +
                    ((rx_body[data_position + 2] & 0xff) << 16) + ((rx_body[data_position + 3] & 0xff) << 24);
            if (body_size <= 12) {
                Log.v(TAG, " --- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + "  (" + target_image_size + ")");
                //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                //SimpleLogDumper.dump_bytes(" [xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                break;
            }

            // 受信データ(のヘッダ部分)をダンプする
            //Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
            //SimpleLogDumper.dump_bytes(" [zzz] " + data_position + ": ", Arrays.copyOfRange(rx_body, data_position, (data_position + 48)));

            if ((data_position + body_size) > length) {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                int copysize = (length - ((data_position + 12)));
                byteStream.write(rx_body, (data_position + 12), copysize);
                received_remain_bytes = body_size - copysize - 12;  // マイナス12は、ヘッダ分
                received_total_bytes = received_total_bytes + copysize;
                //Log.v(TAG, " ----- copy : " + (data_position + 12) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                break;
            }
            try {
                byteStream.write(rx_body, (data_position + 12), (body_size - 12));
                data_position = data_position + body_size;
                received_total_bytes = received_total_bytes + 12;
                //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - 12) + " remain : " + received_remain_bytes);

            } catch (Exception e) {
                Log.v(TAG, "  pos : " + data_position + "  size : " + body_size + " length : " + length);
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (true);
    }

}
