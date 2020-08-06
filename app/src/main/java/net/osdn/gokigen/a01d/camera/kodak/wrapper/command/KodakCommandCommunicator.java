package net.osdn.gokigen.a01d.camera.kodak.wrapper.command;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandReceiveOnly;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

import static net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_RECEIVE_ONLY;
import static net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper.dump_bytes;

public class KodakCommandCommunicator implements IKodakCommandPublisher, IKodakCommunication
{
    private static final String TAG = KodakCommandCommunicator.class.getSimpleName();

    private static final int BUFFER_SIZE = 1024 * 1024 + 16;  // 受信バッファは 1MB
    private static final int COMMAND_SEND_RECEIVE_DURATION_MS = 5;
    private static final int COMMAND_SEND_RECEIVE_DURATION_MAX = 3000;
    private static final int COMMAND_POLL_QUEUE_MS = 5;

    private final String ipAddress;
    private final int portNumber;

    private boolean isStart = false;
    private boolean tcpNoDelay;
    private boolean waitForever;
    private Socket socket = null;
    private DataOutputStream dos = null;
    private BufferedReader bufferedReader = null;
    private Queue<IKodakCommand> commandQueue;

    public KodakCommandCommunicator(@NonNull String ip, int portNumber, boolean tcpNoDelay, boolean waitForever)
    {
        this.ipAddress = ip;
        this.portNumber = portNumber;
        this.tcpNoDelay = tcpNoDelay;
        this.waitForever = waitForever;
        this.commandQueue = new ArrayDeque<>();
        commandQueue.clear();
    }

    @Override
    public boolean isConnected()
    {
        return (socket != null);
    }

    @Override
    public boolean connect()
    {
        try
        {
            Log.v(TAG, " connect()");
            socket = new Socket();
            socket.setTcpNoDelay(tcpNoDelay);
            if (tcpNoDelay)
            {
                socket.setKeepAlive(false);
                socket.setPerformancePreferences(0, 2, 0);
                socket.setOOBInline(true);
                socket.setReuseAddress(false);
                socket.setTrafficClass(0x80);
            }
            socket.connect(new InetSocketAddress(ipAddress, portNumber), 0);
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            socket = null;
        }
        return (false);
    }

    private void closeOutputStream()
    {
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
    }

    private void closeBufferedReader()
    {
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
    }

    private void closeSocket()
    {
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
    }

    @Override
    public void disconnect()
    {
        // 通信関連のクローズ
        closeOutputStream();
        closeBufferedReader();
        closeSocket();

        isStart = false;
        commandQueue.clear();
        System.gc();
    }

    @Override
    public void start()
    {
        if (isStart)
        {
            // すでにコマンドのスレッド動作中なので抜ける
            return;
        }
        isStart = true;
        Log.v(TAG, " start()");

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    InputStream is = socket.getInputStream();
                    dos = new DataOutputStream(socket.getOutputStream());
                    while (isStart)
                    {
                        try
                        {
                            IKodakCommand command = commandQueue.poll();
                            if (command != null)
                            {
                                issueCommand(command);
                            }
                            Thread.sleep(COMMAND_POLL_QUEUE_MS);
                            if ((is != null)&&(is.available() > 0))
                            {
                                Log.v(TAG, " --- RECV MSG --- ");
                                receive_from_camera(new KodakCommandReceiveOnly(SEQ_RECEIVE_ONLY, null));
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.v(TAG, "<<<<< IP : " + ipAddress + " port : " + portNumber + " >>>>>");
                    e.printStackTrace();
                }
            }
        });
        try
        {
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        isStart = false;
        commandQueue.clear();
    }

    @Override
    public boolean enqueueCommand(@NonNull IKodakCommand command)
    {
        try
        {
            //Log.v(TAG, "Enqueue : "  + command.getId());
            return (commandQueue.offer(command));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    private void issueCommand(@NonNull IKodakCommand command)
    {
        try
        {
            boolean retry_over = true;
            while (retry_over)
            {
                //Log.v(TAG, "issueCommand : " + command.getId());
                byte[] commandBody = command.commandBody();
                if (commandBody != null)
                {
                    // コマンドボディが入っていた場合には、コマンド送信（入っていない場合は受信待ち）
                    send_to_camera(command.dumpLog(), commandBody);
                    byte[] commandBody2 = command.commandBody2();
                    if (commandBody2 != null)
                    {
                        // コマンドボディの２つめが入っていた場合には、コマンドを連続送信する
                        send_to_camera(command.dumpLog(), commandBody2);
                    }
                }
                retry_over = receive_from_camera(command);
                if (retry_over)
                {
                    retry_over = command.sendRetry();
                }
            }
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
    private void send_to_camera(boolean isDumpReceiveLog, @NonNull byte[] byte_array)
    {
        try
        {
            if (dos == null)
            {
                Log.v(TAG, " DataOutputStream is null.");
                return;
            }

            if (byte_array.length <= 0)
            {
                // メッセージボディがない。終了する
                Log.v(TAG, " SEND BODY IS NOTHING.");
                return;
            }

            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                dump_bytes("SEND[" + byte_array.length + "] ", byte_array);
            }

            // (データを)送信
            dos.write(byte_array);
            dos.flush();
        }
        catch (Exception e)
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

    /**
     *    カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private boolean receive_from_camera(@NonNull IKodakCommand command)
    {
        int delayMs = command.receiveDelayMs();
        if ((delayMs < 0)||(delayMs > COMMAND_SEND_RECEIVE_DURATION_MAX))
        {
            delayMs = COMMAND_SEND_RECEIVE_DURATION_MS;
        }

        //  受信した後、すべてをまとめて「受信したよ」と応答するパターン
        return (receive_single(command, delayMs));
    }

    private boolean receive_single(@NonNull IKodakCommand command, int delayMs)
    {
        boolean isDumpReceiveLog = command.dumpLog();
        int id = command.getId();
        IKodakCommandCallback callback = command.responseCallback();
        try
        {
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.");
                receivedAllMessage(isDumpReceiveLog, id, null, callback);
                return (false);
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            int read_bytes = waitForReceive(is, delayMs, command.maxRetryCount());
            if (read_bytes < 0)
            {
                // リトライオーバー検出
                Log.v(TAG, " DETECT RETRY OVER...");
                return (true);
            }

            // 受信したデータをバッファに突っ込む
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            while (read_bytes > 0)
            {
                read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                if (read_bytes <= 0)
                {
                    Log.v(TAG, " RECEIVED MESSAGE FINISHED (" + read_bytes + ")");
                    break;
                }
                byteStream.write(byte_array, 0, read_bytes);
                sleep(delayMs);
                read_bytes = is.available();
            }
            receivedAllMessage(isDumpReceiveLog, id, byteStream.toByteArray(), callback);
            System.gc();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (false);
    }

    private void receivedAllMessage(boolean isDumpReceiveLog, int id, byte[] body, IKodakCommandCallback callback)
    {
        Log.v(TAG, "receivedAllMessage() : " + ((body == null) ? 0 : body.length) + " bytes.");
        if ((isDumpReceiveLog)&&(body != null))
        {
            // ログに受信メッセージを出力する
            dump_bytes("RECV[" + body.length + "] ", body);
        }
        if (checkReceiveStatusMessage(body))
        {
            send_secondary_message(isDumpReceiveLog, body);
        }

        if (callback != null)
        {
            callback.receivedMessage(id, body);
        }
    }

    private void send_secondary_message(boolean isDumpReceiveLog, @Nullable byte[] received_body)
    {
        if (received_body == null)
        {
            Log.v(TAG, "send_secondary_message : NULL ");
            return;
        }
        Log.v(TAG, "send_secondary_message : [" + received_body[8] + "] [" + received_body[9] + "] ");
        try {
            byte[] message_to_send = null;
            if ((received_body[8] == (byte) 0xd2) && (received_body[9] == (byte) 0xd2)) {
                message_to_send = new byte[]{
                        (byte) 0x2e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0xd2, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x80,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                };
            }

            if ((received_body[8] == (byte) 0xba) && (received_body[9] == (byte) 0x0b)) {
                message_to_send = new byte[]
                        {
                                (byte) 0x2e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0xba, (byte) 0x0b, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x80,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        };
            }
            if ((received_body[8] == (byte) 0xbb) && (received_body[9] == (byte) 0x0b)) {
                message_to_send = new byte[]
                        {
                                (byte) 0x2e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0xbb, (byte) 0x0b, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x80,
                                (byte) 0x1f, (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                        };
            }
            if ((isDumpReceiveLog)&&(message_to_send != null))
            {
                // ログに受信メッセージを出力する
                dump_bytes("SND2[" + message_to_send.length + "] ", message_to_send);
            }

            if ((dos != null)&&(message_to_send != null))
            {
                // (データを)送信
                dos.write(message_to_send);
                dos.flush();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkReceiveStatusMessage(@Nullable byte[] receive_body)
    {
        boolean isReceivedStatusMessage = false;
        try
        {
            if (receive_body == null)
            {
                return (false);
            }
            if (receive_body.length < 16)
            {
                Log.v(TAG, " BODY SIZE IS SMALL. : " + receive_body.length);
                return (false);
            }
            if (((receive_body[8] == (byte) 0xd2)&&(receive_body[9] == (byte) 0x07))||
                    ((receive_body[8] == (byte) 0xba)&&(receive_body[9] == (byte) 0x0b))||
                    ((receive_body[8] == (byte) 0xbb)&&(receive_body[9] == (byte) 0x0b)))

            {
                isReceivedStatusMessage = true;
                Log.v(TAG, "  >>> RECEIVED HOST PRIMARY MESSAGE. <<<");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (isReceivedStatusMessage);
    }

    private int waitForReceive(InputStream is, int delayMs, int retry_count)
    {
        boolean isLogOutput = true;
        int read_bytes = 0;
        try
        {
            while (read_bytes <= 0)
            {
                Log.v(TAG, "  --- waitForReceive : " + retry_count + " delay : " + delayMs + "ms");
                sleep(delayMs);
                read_bytes = is.available();
                if (read_bytes <= 0)
                {
                    if (isLogOutput)
                    {
                        Log.v(TAG, "  waitForReceive:: is.available() WAIT... : " + delayMs + "ms");
                        isLogOutput = false;
                    }
                    retry_count--;
                    if ((!waitForever)&&(retry_count < 0))
                    {
                        return (-1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (read_bytes);
    }
}
