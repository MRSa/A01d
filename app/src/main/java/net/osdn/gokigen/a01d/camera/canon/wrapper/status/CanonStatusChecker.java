package net.osdn.gokigen.a01d.camera.canon.wrapper.status;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommand;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.specific.InitEventRequest;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper.dump_bytes;

public class CanonStatusChecker implements IPtpIpCommandCallback, ICameraStatusWatcher, ICameraStatus
{
    private final String TAG = toString();

    private static final int BUFFER_SIZE = 1024 * 1024 + 8;
    private static final int STATUS_MESSAGE_HEADER_SIZE = 14;
    private final IPtpIpCommandPublisher issuer;
    private ICameraStatusUpdateNotify notifier = null;
    private CanonStatusHolder statusHolder;
    private boolean whileFetching = false;
    private boolean logcat = false;
    private final String ipAddress;
    private final int portNumber;

    private Socket socket = null;
    private DataOutputStream dos = null;
    private BufferedReader bufferedReader = null;
    private int eventConnectionNumber = 0;

    public CanonStatusChecker(@NonNull Activity activity, @NonNull IPtpIpCommandPublisher issuer, @NonNull String ip, int portNumber)
    {
        this.issuer = issuer;
        this.statusHolder = new CanonStatusHolder();
        this.ipAddress = ip;
        this.portNumber = portNumber;
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] data)
    {
        try
        {
            logcat("receivedMessage : " + id + ", length: " + data.length);
            if (id == IPtpIpMessages.SEQ_EVENT_INITIALIZE)
            {
                // 終わる...んじゃなくて、イベント受信待ちに遷移する。
                Log.v(TAG, " ----- PTP-IP Connection is ESTABLISHED. -----");
                waitForEvent();
                return;
            }

            if (data.length < STATUS_MESSAGE_HEADER_SIZE)
            {
                Log.v(TAG, "received status length is short. (" + data.length + " bytes.)");
                return;
            }
            int nofStatus = (data[13] * 256) + data[12];
            int statusCount = 0;
            int index = STATUS_MESSAGE_HEADER_SIZE;
            while ((statusCount < nofStatus)&&(index < data.length))
            {
                int dataId = ((((int)data[index + 1]) & 0xff) * 256) + (((int) data[index]) & 0xff);
                statusHolder.updateValue(notifier, dataId, data[index + 2], data[index + 3], data[index +4], data[index + 5]);
                index = index + 6;
                statusCount++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getStatusList(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return (new ArrayList<>());
            }
            return (statusHolder.getAvailableItemList(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new ArrayList<>());
    }

    @Override
    public String getStatus(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return ("");
            }
            return (statusHolder.getItemStatus(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public void setStatus(String key, String value)
    {
        try
        {
            if (logcat)
            {
                Log.v(TAG, "setStatus(" + key + ", " + value + ")");
            }

            // ここで設定を行う。
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier)
    {
        if (whileFetching)
        {
            Log.v(TAG, "startStatusWatch() already starting.");
            return;
        }
        try
        {
            this.notifier = notifier;
            whileFetching = true;

            // セッションをオープンする
            boolean isConnect = connect();
            if (!isConnect)
            {
                Log.v(TAG, "  CONNECT FAIL...(EVENT) : " + ipAddress + "  " + portNumber);
            }
            issueCommand(new InitEventRequest(this, eventConnectionNumber));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopStatusWatch()
    {
        Log.v(TAG, "stoptStatusWatch()");
        whileFetching = false;
        this.notifier = null;
    }

    private void logcat(String message)
    {
        if (logcat)
        {
            Log.v(TAG, message);
        }
    }

    private boolean connect()
    {
        try
        {
            socket = new Socket(ipAddress, portNumber);
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            socket = null;
        }
        return (false);
    }

    private void disconnect()
    {
        // ストリームを全部閉じる
        try
        {
            if (dos != null)
            {
                dos.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        dos = null;

        try
        {
            if (bufferedReader != null)
            {
                bufferedReader.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        bufferedReader = null;

        try
        {
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        socket = null;
        System.gc();
    }

    public void setEventConnectionNumber(int connectionNumber)
    {
        eventConnectionNumber = connectionNumber;
    }

    private void issueCommand(@NonNull IPtpIpCommand command)
    {
        try
        {
            //Log.v(TAG, "issueCommand : " + command.getId());
            byte[] commandBody = command.commandBody();
            if (commandBody != null)
            {
                // コマンドボディが入っていた場合には、コマンド送信（入っていない場合は受信待ち）
                send_to_camera(command.dumpLog(), commandBody);
            }
            receive_from_camera(command.dumpLog(), command.getId(), command.responseCallback(), command.receiveAgainShortLengthMessage(), command.receiveDelayMs());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *    カメラにコマンドを送信する（メイン部分）
     *
     */
    private void send_to_camera(boolean isDumpReceiveLog, byte[] byte_array)
    {
        try
        {
            dos = new DataOutputStream(socket.getOutputStream());  // ここにいたらいけない？

            // メッセージボディを加工： 最初に４バイトのレングス長をつける
            byte[] sendData = new byte[byte_array.length + 4];

            sendData[0] = (byte) (byte_array.length + 4);
            sendData[1] = 0x00;
            sendData[2] = 0x00;
            sendData[3] = 0x00;
            System.arraycopy(byte_array,0,sendData,4, byte_array.length);

            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                dump_bytes("SEND[" + sendData.length + "] ", sendData);
            }

            // (データを)送信
            dos.write(sendData);
            dos.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *    カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private void receive_from_camera(boolean isDumpReceiveLog, int id, IPtpIpCommandCallback callback, boolean receiveAgain, int delayMs)
    {
        try
        {
            sleep(delayMs);

            boolean isFirstTime = true;
            int totalReadBytes;
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is != null)
            {
                int read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                byte[] receive_body;
                if (read_bytes > 4)
                {
                    if (receiveAgain)
                    {
                        int length = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
                        if (length > receive_message_buffer_size)
                        {
                            Log.v(TAG, "+++++ TOTAL RECEIVE MESSAGE SIZE IS " + length + " +++++");
                        }
                        totalReadBytes = read_bytes;
                        while ((length > totalReadBytes) || ((length == read_bytes) && ((int) byte_array[4] == 0x02)))
                        {
                            // データについて、もう一回受信が必要な場合...
                            if (isDumpReceiveLog)
                            {
                                Log.v(TAG, "--- RECEIVE AGAIN --- [" + length + "(" + read_bytes + ") " + byte_array[4] + "] ");
                            }
                            sleep(delayMs);
                            int read_bytes2 = is.read(byte_array, read_bytes, receive_message_buffer_size - read_bytes);
                            if (read_bytes2 > 0)
                            {
                                read_bytes = read_bytes + read_bytes2;
                                totalReadBytes = totalReadBytes + read_bytes2;
                            } else {
                                // よみだし終了。
                                Log.v(TAG, "FINISHED RECEIVE... ");
                                break;
                            }
                            if (callback != null)
                            {
                                if (callback.isReceiveMulti())
                                {
                                    int offset = 0;
                                    if (isFirstTime)
                                    {
                                        // 先頭のヘッダ部分をカットして送る
                                        offset = 12;
                                        isFirstTime = false;
                                        //Log.v(TAG, " FIRST TIME : " + read_bytes + " " + offset);
                                    }
                                    callback.onReceiveProgress(read_bytes - offset, length, Arrays.copyOfRange(byte_array, offset, read_bytes));
                                    read_bytes = 0;
                                } else {
                                    callback.onReceiveProgress(read_bytes, length, null);
                                }
                            }
                        }
                    }
                    receive_body = Arrays.copyOfRange(byte_array, 0, read_bytes);
                } else {
                    receive_body = new byte[1];
                }
                if (isDumpReceiveLog)
                {
                    // ログに受信メッセージを出力する
                    Log.v(TAG, " receive_from_camera() : " + read_bytes + " bytes.");
                    dump_bytes("RECV[" + receive_body.length + "] ", receive_body);
                }
                if (callback != null)
                {
                    if (callback.isReceiveMulti())
                    {
                        callback.receivedMessage(id, null);
                    } else {
                        callback.receivedMessage(id, receive_body);
                        //callback.receivedMessage(id, Arrays.copyOfRange(receive_body, 0, receive_body.length));
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void sleep(int delayMs)
    {
        try
        {
            Thread.sleep(delayMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void waitForEvent()
    {
        final int delayMs = 25;
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        Log.v(TAG, " waitForEvent : " + whileFetching);
                        // if (whileFetching)
                        {
                            sleep(delayMs);

                            // 受信待ちする...
                            receive_from_camera(true, IPtpIpMessages.SEQ_EVENT_RECEIVE, new IPtpIpCommandCallback() {
                                        @Override
                                        public void receivedMessage(int id, byte[] rx_body)
                                        {
                                            try
                                            {
                                                //  メッセージを受信。 応答を返さないといけない...
                                                sendReplyMessage(rx_body);

                                                //  そして次のイベントを待つ
                                                waitForEvent();
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
                                        {
                                            Log.v(TAG, " onReceiveProgress : [" + currentBytes + "/" + totalBytes + "]");
                                        }

                                        @Override
                                        public boolean isReceiveMulti()
                                        {
                                            return false;
                                        }
                                    }, true, delayMs);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void sendReplyMessage(byte[] received_message)
    {
        try
        {
            Log.v(TAG, " RECEIVE : " + received_message.length + " bytes.");
            if (received_message.length >= 26)
            {
                // 受信パケットを解析
                int packetType = (((int) received_message[4]) & 0xff);
                int eventCode =  ((((int) received_message[9]) & 0xff) << 8) + (((int) received_message[8]) & 0xff);
                int parameter1 = ((((int) received_message[17]) & 0xff) << 24) + ((((int) received_message[16]) & 0xff) << 16) + ((((int) received_message[15]) & 0xff) << 8) + (((int) received_message[14]) & 0xff);
                int parameter2 = ((((int) received_message[21]) & 0xff) << 24) + ((((int) received_message[20]) & 0xff) << 16) + ((((int) received_message[19]) & 0xff) << 8) + (((int) received_message[18]) & 0xff);
                int parameter3 = ((((int) received_message[25]) & 0xff) << 24) + ((((int) received_message[24]) & 0xff) << 16) + ((((int) received_message[23]) & 0xff) << 8) + (((int) received_message[22]) & 0xff);
                Log.v(TAG, String.format(" event : 0x%x, code: 0x%x, prm1: 0x%x, prm2: 0x%x, prm3 : 0x%x", packetType, eventCode, parameter1, parameter2, parameter3));
                if (eventCode == 0xc101)
                {
                    // イベントの受信指示
                    issuer.enqueueCommand(new PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_STATUS, true, 0, 0x9116));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
