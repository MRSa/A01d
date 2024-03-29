package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.util.Log;

import androidx.annotation.NonNull;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class CanonLiveViewImageReceiverPrev implements IPtpIpCommandCallback
{
    private final String TAG = toString();

    private final IPtpIpLiveViewImageCallback callback;

    private int received_total_bytes = 0;
    private int received_remain_bytes = 0;

    private int target_image_size = 0;
    private boolean receivedFirstData = false;
    private final ByteArrayOutputStream byteStream;

    CanonLiveViewImageReceiverPrev(@NonNull IPtpIpLiveViewImageCallback callback)
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
            if (rx_body != null)
            {
                byteStream.write(rx_body, 0, rx_body.length);
            }
            byte [] thumbnail = byteStream.toByteArray();
            Log.v(TAG, " TransferComplete() RECEIVED  : " + id + " size : " + target_image_size + " (" + thumbnail.length + ")");
            int dump_length = 96;
            if (thumbnail.length > dump_length)
            {
                SimpleLogDumper.dump_bytes(" [xxx(head)xxx]", Arrays.copyOfRange(thumbnail, 0, (dump_length)));
                SimpleLogDumper.dump_bytes(" [zz(bottom)zz]", Arrays.copyOfRange(thumbnail, (thumbnail.length - dump_length), (thumbnail.length)));
            }
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
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes);

        // 受信したデータから、通信のヘッダ部分を削除する
        cutHeader(rx_body);
    }

    private void cutHeader(byte[] rx_body)
    {
        if (rx_body == null)
        {
            return;
        }
        try
        {
            int length = rx_body.length;
            int data_position = 0;
            if (!receivedFirstData)
            {
                // データを最初に読んだとき。ヘッダ部分を読み飛ばす
                receivedFirstData = true;
                data_position = (int) rx_body[0] & (0xff);
                Log.v(TAG, " FIRST DATA POS. : " + data_position + " [size : " + rx_body.length + "]");
                //SimpleLogDumper.dump_bytes(" [sssss]", Arrays.copyOfRange(rx_body, 0, (96)));
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

            while (data_position <= (length - 12))
            {
                int body_size = (rx_body[data_position] & 0xff) + ((rx_body[data_position + 1] & 0xff) << 8) +
                        ((rx_body[data_position + 2] & 0xff) << 16) + ((rx_body[data_position + 3] & 0xff) << 24);
                if (body_size <= 12)
                {
                    Log.v(TAG, " --- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + "  (" + target_image_size + ")");
                    //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                    //SimpleLogDumper.dump_bytes(" [xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                    break;
                }

                // 受信データ(のヘッダ部分)をダンプする
                Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
                //SimpleLogDumper.dump_bytes(" [zzz] " + data_position + ": ", Arrays.copyOfRange(rx_body, data_position, (data_position + 48)));

                if ((data_position + body_size) > length)
                {
                    // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                    int copysize = (length - ((data_position + 12)));
                    byteStream.write(rx_body, (data_position + 12), copysize);
                    received_remain_bytes = body_size - copysize - 12;  // マイナス12は、ヘッダ分
                    received_total_bytes = received_total_bytes + copysize;
                    Log.v(TAG, " ----- copy : " + (data_position + 12) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                    break;
                }
                try
                {
                    byteStream.write(rx_body, (data_position + 12), (body_size - 12));
                    data_position = data_position + body_size;
                    received_total_bytes = received_total_bytes + 12;
                    Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - 12) + " remain : " + received_remain_bytes);

                }
                catch (Exception e)
                {
                    Log.v(TAG, "  pos : " + data_position + "  size : " + body_size + " length : " + length);
                    e.printStackTrace();
                }
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
