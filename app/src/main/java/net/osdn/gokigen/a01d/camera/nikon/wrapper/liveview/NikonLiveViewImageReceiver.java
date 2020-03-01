package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview;

import android.util.Log;

import androidx.annotation.NonNull;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback;
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class NikonLiveViewImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();

    private IPtpIpLiveViewImageCallback callback;

    private int received_total_bytes = 0;
    private int received_remain_bytes = 0;

    //private int target_image_size = 0;
    private boolean receivedFirstData = false;
    private ByteArrayOutputStream byteStream;

    NikonLiveViewImageReceiver(@NonNull IPtpIpLiveViewImageCallback callback)
    {
        this.callback = callback;
        byteStream = new ByteArrayOutputStream();
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        if (isReceiveMulti())
        {
            receivedMessage_multi(id, rx_body);
        }
        else
        {
            receivedMessage_single(id, rx_body);
        }
    }

    @Override
    public void onReceiveProgress(final int currentBytes, final int totalBytes, byte[] rx_body)
    {
        int body_length = 0;
        if (rx_body != null)
        {
            body_length = rx_body.length;
        }
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes + " LENGTH: " + body_length + " bytes.");

        // 受信したデータから、通信のヘッダ部分を削除する
        cutHeader(rx_body);
    }

    private void receivedMessage_single(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body != null)
            {
                Log.v(TAG, "receivedMessage_single() : " + rx_body.length + " bytes.");
                if (rx_body.length > 64)
                {
                    SimpleLogDumper.dump_bytes(" LV (FIRST) : ", Arrays.copyOfRange(rx_body, 0, 64));
                    SimpleLogDumper.dump_bytes(" LV (-END-) : ", Arrays.copyOfRange(rx_body, (rx_body.length - 64), rx_body.length));
                }
                callback.onCompleted(rx_body, null);
            }
            else
            {
                Log.v(TAG, "receivedMessage_single() : NULL");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void receivedMessage_multi(int id, byte[] rx_body)
    {
        try
        {
            int body_length = 0;
            if (rx_body != null)
            {
                body_length = rx_body.length;
            }
            Log.v(TAG, " receivedMessage_multi()  id[" + id + "] size : " + body_length + " ");
            //Log.v(TAG, " receivedMessage_multi()  id[" + id + "] size : " + body_length + " target length : " +  target_image_size + " ");

            // end of receive sequence.
            //byte [] thumbnail = byteStream.toByteArray();
            //byte [] thumbnail = rx_body;
            //Log.v(TAG, " TransferComplete() RECEIVED  id[" + id + "] size : " + target_image_size + " (" + thumbnail.length + ")");
            //SimpleLogDumper.dump_bytes(" [xxxxx]", Arrays.copyOfRange(thumbnail, 0, (512)));
            //SimpleLogDumper.dump_bytes(" [zzzzz]", Arrays.copyOfRange(thumbnail, (thumbnail.length - 128), (thumbnail.length)));
            //callback.onCompleted(rx_body, null);
            callback.onCompleted(byteStream.toByteArray(), null);
            receivedFirstData = false;
            received_remain_bytes = 0;
            received_total_bytes = 0;
            //target_image_size = 0;
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
            //SimpleLogDumper.dump_bytes(" [sssXXXsss]", Arrays.copyOfRange(rx_body, first_offset, (first_offset + 64)));
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
            int body_size = (rx_body[data_position] & 0xff) + ((rx_body[data_position + 1] & 0xff) << 8) + ((rx_body[data_position + 2] & 0xff) << 16) + ((rx_body[data_position + 3] & 0xff) << 24);
            if (body_size <= 12)
            {
                Log.v(TAG, " ----- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + " ");
                //Log.v(TAG, " --- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.length + "  (" + target_image_size + ")");
                //int startpos = (data_position > 48) ? (data_position - 48) : 0;
                //SimpleLogDumper.dump_bytes(" [xxx]", Arrays.copyOfRange(rx_body, startpos, (data_position + 48)));
                break;
            }

            // 受信データ(のヘッダ部分)をダンプする
            //Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");

            if ((data_position + body_size) > length)
            {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                int copysize = (length - ((data_position + (12))));
                byteStream.write(rx_body, (data_position + (12)), copysize);
                received_remain_bytes = body_size - copysize - (12);  // マイナス12は、ヘッダ分
                received_total_bytes = received_total_bytes + copysize;
                //Log.v(TAG, " ----- copy : " + (data_position + (12)) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                break;
            }
            try
            {
                byteStream.write(rx_body, (data_position + (12)), (body_size - (12)));
                data_position = data_position + body_size;
                received_total_bytes = received_total_bytes + (12);
                //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - (12)) + " remain : " + received_remain_bytes);
            }
            catch (Exception e)
            {
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
