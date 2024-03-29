package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper.dump_bytes;

public class PtpIpCommandPublisherLegacy implements IPtpIpCommandPublisher, IPtpIpCommunication
{
    private static final String TAG = PtpIpCommandPublisher.class.getSimpleName();

    private static final int SEQUENCE_START_NUMBER = 1;
    private static final int BUFFER_SIZE = 1024 * 1024 + 16;  // 受信バッファは 1MB
    private static final int COMMAND_SEND_RECEIVE_DURATION_MS = 5;
    private static final int COMMAND_SEND_RECEIVE_DURATION_MAX = 3000;
    private static final int COMMAND_POLL_QUEUE_MS = 5;

    private final String ipAddress;
    private final int portNumber;

    private boolean isStart = false;
    private boolean isHold = false;
    private boolean tcpNoDelay;
    private boolean waitForever;
    private int holdId = 0;
    private Socket socket = null;
    private DataOutputStream dos = null;
    private BufferedReader bufferedReader = null;
    private int sequenceNumber = SEQUENCE_START_NUMBER;
    private Queue<IPtpIpCommand> commandQueue;
    private Queue<IPtpIpCommand> holdCommandQueue;

    public PtpIpCommandPublisherLegacy(@NonNull String ip, int portNumber, boolean tcpNoDelay, boolean waitForever)
    {
        this.ipAddress = ip;
        this.portNumber = portNumber;
        this.tcpNoDelay = tcpNoDelay;
        this.waitForever = waitForever;
        this.commandQueue = new ArrayDeque<>();
        this.holdCommandQueue = new ArrayDeque<>();
        commandQueue.clear();
        holdCommandQueue.clear();
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
            //socket = new Socket(ipAddress, portNumber);
            socket = new Socket();
            socket.setTcpNoDelay(tcpNoDelay);
            if (tcpNoDelay)
            {
                socket.setKeepAlive(false);
                socket.setPerformancePreferences(0, 2, 0);
                socket.setOOBInline(true);
                socket.setReuseAddress(false);
                socket.setTrafficClass(0x80);
                //socket.setSoLinger(true, 3000);
                //socket.setReceiveBufferSize(2097152);
                //socket.setSendBufferSize(524288);
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

    @Override
    public void disconnect()
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
        sequenceNumber = SEQUENCE_START_NUMBER;
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
                    dos = new DataOutputStream(socket.getOutputStream());
                    while (isStart)
                    {
                        try
                        {
                            IPtpIpCommand command = commandQueue.poll();
                            if (command != null)
                            {
                                issueCommand(command);
                            }
                            Thread.sleep(COMMAND_POLL_QUEUE_MS);
                            // Log.v(TAG, " QUEUE SIZE : " + commandQueue.size());
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
    public boolean enqueueCommand(@NonNull IPtpIpCommand command)
    {
        try
        {
            if (isHold) {
                if (holdId == command.getHoldId()) {
                    if (command.isRelease()) {
                        // コマンドをキューに積んだ後、リリースする
                        boolean ret = commandQueue.offer(command);
                        isHold = false;

                        //  溜まっているキューを積みなおす
                        while (holdCommandQueue.size() != 0) {
                            IPtpIpCommand queuedCommand = holdCommandQueue.poll();
                            commandQueue.offer(queuedCommand);
                            if ((queuedCommand != null)&&(queuedCommand.isHold()))
                            {
                                // 特定シーケンスに入った場合は、そこで積みなおすのをやめる
                                isHold = true;
                                holdId = queuedCommand.getHoldId();
                                break;
                            }
                        }
                        return (ret);
                    }
                    return (commandQueue.offer(command));
                } else {
                    // 特定シーケンスではなかったので HOLD
                    return (holdCommandQueue.offer(command));
                }
            }
            if (command.isHold())
            {
                isHold = true;
                holdId = command.getHoldId();
            }
            //Log.v(TAG, "Enqueue : "  + command.getId());
            return (commandQueue.offer(command));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public int getCurrentQueueSize()
    {
        return (commandQueue.size());
    }

    @Override
    public boolean flushQueue()
    {
        Log.v(TAG, "  flushHoldQueue() : CLEAR QUEUE : " + commandQueue.size());
        commandQueue.clear();
        System.gc();
        return (true);
    }

    @Override
    public int isExistCommandMessageQueue(int id)
    {
        int count = 0;
        for (IPtpIpCommand cmd : commandQueue)
        {
            if (cmd.getId() == id)
            {
                count++;
            }
        }
        return (count);
    }

    @Override
    public boolean flushHoldQueue()
    {
        Log.v(TAG, "  flushHoldQueue()");
        holdCommandQueue.clear();
        System.gc();
        return (true);
    }

    private void issueCommand(@NonNull IPtpIpCommand command)
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
                    send_to_camera(command.dumpLog(), commandBody, command.useSequenceNumber(), command.embeddedSequenceNumberIndex());
                    byte[] commandBody2 = command.commandBody2();
                    if (commandBody2 != null)
                    {
                        // コマンドボディの２つめが入っていた場合には、コマンドを連続送信する
                        send_to_camera(command.dumpLog(), commandBody2, command.useSequenceNumber(), command.embeddedSequenceNumberIndex2());
                    }
                    byte[] commandBody3 = command.commandBody3();
                    if (commandBody3 != null)
                    {
                        // コマンドボディの３つめが入っていた場合には、コマンドを連続送信する
                        send_to_camera(command.dumpLog(), commandBody3, command.useSequenceNumber(), command.embeddedSequenceNumberIndex3());
                    }
                    if (command.isIncrementSeqNumber())
                    {
                        // シーケンス番号を更新する
                        sequenceNumber++;
                    }
                }
                retry_over = receive_from_camera(command);
                if ((retry_over)&&(commandBody != null))
                {
                    if (!command.isRetrySend())
                    {
                        while (retry_over)
                        {
                            //  コマンドを再送信しない場合はここで応答を待つ...
                            retry_over = receive_from_camera(command);
                        }
                        break;
                    }
                    if (!command.isIncrementSequenceNumberToRetry())
                    {
                        // 再送信...のために、シーケンス番号を戻す...
                        sequenceNumber--;
                    }
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
    private void send_to_camera(boolean isDumpReceiveLog, byte[] byte_array, boolean useSequenceNumber, int embeddedSequenceIndex)
    {
        try
        {
            if (dos == null)
            {
                Log.v(TAG, " DataOutputStream is null.");
                return;
            }

            //dos = new DataOutputStream(socket.getOutputStream());  // ここにいたらいけない？

            // メッセージボディを加工： 最初に４バイトのレングス長をつける
            byte[] sendData = new byte[byte_array.length + 4];

            sendData[0] = (byte) (byte_array.length + 4);
            sendData[1] = 0x00;
            sendData[2] = 0x00;
            sendData[3] = 0x00;
            System.arraycopy(byte_array,0,sendData,4, byte_array.length);

            if (useSequenceNumber)
            {
                // Sequence Number を反映させる
                sendData[embeddedSequenceIndex] = (byte) ((0x000000ff & sequenceNumber));
                sendData[embeddedSequenceIndex + 1] = (byte) (((0x0000ff00 & sequenceNumber) >>> 8) & 0x000000ff);
                sendData[embeddedSequenceIndex + 2] = (byte) (((0x00ff0000 & sequenceNumber) >>> 16) & 0x000000ff);
                sendData[embeddedSequenceIndex + 3] = (byte) (((0xff000000 & sequenceNumber) >>> 24) & 0x000000ff);
                if (isDumpReceiveLog)
                {
                    Log.v(TAG, "----- SEQ No. : " + sequenceNumber + " -----");
                }
            }

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
    private boolean receive_from_camera(@NonNull IPtpIpCommand command)
    {
        IPtpIpCommandCallback callback = command.responseCallback();
        int delayMs = command.receiveDelayMs();
        if ((delayMs < 0)||(delayMs > COMMAND_SEND_RECEIVE_DURATION_MAX))
        {
            delayMs = COMMAND_SEND_RECEIVE_DURATION_MS;
        }
/*
        try
        {
            if (socket != null)
            {
                Log.v(TAG, " SOCKET : send " + socket.getSendBufferSize() + "  recv " + socket.getReceiveBufferSize() + " " + socket.getTcpNoDelay() + " " + socket.getOOBInline() + " " + socket.getKeepAlive() + " " + socket.getReuseAddress() + " " + socket.getSoLinger() + " " + socket.getSoTimeout() + " " + socket.getTrafficClass());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/

        if ((callback != null)&&(callback.isReceiveMulti()))
        {
            // 受信したら逐次「受信したよ」と応答するパターン
            return (receive_multi(command, delayMs));
        }
        //  受信した後、すべてをまとめて「受信したよ」と応答するパターン
        return (receive_single(command, delayMs));
    }

    private boolean receive_single(@NonNull IPtpIpCommand command, int delayMs)
    {
        boolean isDumpReceiveLog = command.dumpLog();
        int id = command.getId();
        IPtpIpCommandCallback callback = command.responseCallback();
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
                // リトライオーバー...
                Log.v(TAG, " RECEIVE : RETRY OVER...");
                if (!command.isRetrySend())
                {
                    // 再送しない場合には、応答がないことを通知する
                    receivedAllMessage(isDumpReceiveLog, id, null, callback);
                }
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
            ByteArrayOutputStream outputStream = cutHeader(byteStream);
            receivedAllMessage(isDumpReceiveLog, id, outputStream.toByteArray(), callback);
            System.gc();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (false);
    }

    private void receivedAllMessage(boolean isDumpReceiveLog, int id, byte[] body, IPtpIpCommandCallback callback)
    {
        Log.v(TAG, "receivedAllMessage() : " + ((body == null) ? 0 : body.length) + " bytes.");
        if ((isDumpReceiveLog)&&(body != null))
        {
            // ログに受信メッセージを出力する
            dump_bytes("RECV[" + body.length + "] ", body);
        }
        if (callback != null)
        {
            callback.receivedMessage(id, body);
        }
    }

    private boolean receive_multi(@NonNull IPtpIpCommand command, int delayMs)
    {
        //int estimatedSize = command.estimatedReceiveDataSize();
        int maxRetryCount = command.maxRetryCount();
        int id = command.getId();
        IPtpIpCommandCallback callback = command.responseCallback();

        try
        {
            Log.v(TAG, " ===== receive_multi() =====");
            int receive_message_buffer_size = BUFFER_SIZE;
            byte[] byte_array = new byte[receive_message_buffer_size];
            InputStream is = socket.getInputStream();
            if (is == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.");
                return (false);
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            int read_bytes = waitForReceive(is, delayMs, command.maxRetryCount());
            if (read_bytes < 0)
            {
                // リトライオーバー...
                Log.v(TAG, " RECEIVE : RETRY OVER...... : " + delayMs + "ms x " + command.maxRetryCount());
                if (command.isRetrySend())
                {
                    // 要求を再送する場合、、、ダメな場合は受信待ちとする
                    return (true);
                }
            }

            int target_length;
            int received_length;

            //boolean read_retry = false;
            //do
            {
                // 初回データの読み込み...
                read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                target_length = parseDataLength(byte_array, read_bytes);
                received_length = read_bytes;
                if (target_length <= 0)
                {
                    // 受信サイズ異常の場合...
                    if (received_length > 0)
                    {
                        SimpleLogDumper.dump_bytes("WRONG DATA : ", Arrays.copyOfRange(byte_array, 0, (Math.min(received_length, 64))));
                    }
                    Log.v(TAG, " WRONG LENGTH. : " + target_length + " READ : " + received_length + " bytes.");
                    callback.receivedMessage(id, null);
                    return (false);
                }

            } //while (read_retry);

            //  一時的な処理
            if (callback != null)
            {
                Log.v(TAG, "  -=-=-=- 1st CALL : read_bytes : "+ read_bytes + "(" + received_length + ") : target_length : " + target_length + "  buffer SIZE : " + byte_array.length);
                callback.onReceiveProgress(received_length, target_length, Arrays.copyOfRange(byte_array, 0, received_length));
            }

            //do
            {
                sleep(delayMs);
                read_bytes = is.available();
                if (read_bytes == 0)
                {
                    //Log.v(TAG, " WAIT is.available() ... [" + received_length + ", " + target_length + "] retry : " + maxRetryCount);
                    maxRetryCount--;
                }
            } // while ((read_bytes == 0)&&(maxRetryCount > 0)&&(received_length < target_length)); // ((read_bytes == 0)&&(estimatedSize > 0)&&(received_length < estimatedSize));

            while ((read_bytes >= 0)&&(received_length < target_length))
            {
                read_bytes = is.read(byte_array, 0, receive_message_buffer_size);
                if (read_bytes <= 0)
                {
                    Log.v(TAG, "  RECEIVED MESSAGE FINISHED (" + read_bytes + ")");
                    break;
                }
                received_length = received_length + read_bytes;

                //  一時的な処理
                if (callback != null)
                {
                    //Log.v(TAG, "  --- CALL : read_bytes : "+ read_bytes + " total_read : " + received_length + " : total_length : " + target_length + "  buffer SIZE : " + byte_array.length);
                    callback.onReceiveProgress(received_length, target_length, Arrays.copyOfRange(byte_array, 0, read_bytes));
                }
                //byteStream.write(byte_array, 0, read_bytes);

                maxRetryCount = command.maxRetryCount();
                //do
                {
                    sleep(delayMs);
                    read_bytes = is.available();
                    //Log.v(TAG, "  is.available() read_bytes : " + read_bytes + " " + received_length + " < " + estimatedSize);
                    if (read_bytes == 0)
                    {
                        Log.v(TAG, " WAIT is.available() ... [" + received_length + ", " + target_length + "] " + read_bytes + " retry : " + maxRetryCount);
                        maxRetryCount--;
                    }
                } // while ((read_bytes == 0)&&(maxRetryCount > 0)&&(received_length < target_length)); // while ((read_bytes == 0)&&(estimatedSize > 0)&&(received_length < estimatedSize));
            }
            //ByteArrayOutputStream outputStream = cutHeader(byteStream);
            //receivedMessage(isDumpReceiveLog, id, outputStream.toByteArray(), callback);

            //  終了報告...一時的？
            if (callback != null)
            {
                Log.v(TAG, "  --- receive_multi : " + id + "  (" + read_bytes + ") [" + maxRetryCount + "] " + receive_message_buffer_size + " (" + received_length + ") ");
                try
                {
                    callback.receivedMessage(id, Arrays.copyOfRange(byte_array, 0, received_length));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    private int parseDataLength(byte[] byte_array, int read_bytes)
    {
        int offset = 0;
        int lenlen = 0;
        //int packetType = 0;
        try
        {
            if (read_bytes > 20)
            {
                if ((int) byte_array[offset + 4] == 0x07)
                {
                    // 前の応答が入っていると考える...
                    offset = 14;
                }

                if (((int) byte_array[offset + 4] == 0x09))
                {
                    lenlen = ((((int) byte_array[offset + 15]) & 0xff) << 24) + ((((int) byte_array[offset + 14]) & 0xff) << 16) + ((((int) byte_array[offset + 13]) & 0xff) << 8) + (((int) byte_array[offset + 12]) & 0xff);
                    //packetType = (((int)byte_array[offset + 16]) & 0xff);
                }
            }
            //Log.v(TAG, " --- parseDataLength() length: " + lenlen + " TYPE: " + packetType + " read_bytes: " + read_bytes + "  offset : " + offset);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (lenlen);
    }

    private ByteArrayOutputStream cutHeader(ByteArrayOutputStream receivedBuffer)
    {
        try
        {
            byte[] byte_array = receivedBuffer.toByteArray();
            int limit = byte_array.length;
            int lenlen = 0;
            int len = ((((int) byte_array[3]) & 0xff) << 24) + ((((int) byte_array[2]) & 0xff) << 16) + ((((int) byte_array[1]) & 0xff) << 8) + (((int) byte_array[0]) & 0xff);
            int packetType = (((int) byte_array[4]) & 0xff);
            if ((limit == len)||(limit < 16384))
            {
                // 応答は１つしか入っていない。もしくは受信データサイズが16kBの場合は、そのまま返す。
                return (receivedBuffer);
            }
            if (packetType == 0x09)
            {
                lenlen = ((((int) byte_array[15]) & 0xff) << 24) + ((((int) byte_array[14]) & 0xff) << 16) + ((((int) byte_array[13]) & 0xff) << 8) + (((int) byte_array[12]) & 0xff);
                //packetType = (((int) byte_array[16]) & 0xff);
            }
            // Log.v(TAG, " ---  RECEIVED MESSAGE : " + len + " bytes (BUFFER: " + byte_array.length + " bytes)" + " length : " + lenlen + " TYPE : " + packetType + " --- ");
            if (lenlen == 0)
            {
                // データとしては変なので、なにもしない
                return (receivedBuffer);
            }
            ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
            //outputStream.write(byte_array, 0, 20);  //
            int position = 20;  // ヘッダ込の先頭
            while (position < limit)
            {
                lenlen = ((((int) byte_array[position + 3]) & 0xff) << 24) + ((((int) byte_array[position + 2]) & 0xff) << 16) + ((((int) byte_array[position + 1]) & 0xff) << 8) + (((int) byte_array[position]) & 0xff);
/*
                packetType = (((int) byte_array[position + 4]) & 0xff);
                if (packetType != 0x0a)
                {
                    Log.v(TAG, " <><><> PACKET TYPE : " + packetType + " LENGTH : " + lenlen);
                }
*/
                int copyByte =  Math.min((limit - (position + 12)), (lenlen - 12));
                outputStream.write(byte_array, (position + 12), copyByte);
                position = position + lenlen;
            }
            return (outputStream);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.gc();
        }
        return (receivedBuffer);
    }

    private int waitForReceive(InputStream is, int delayMs, int retry_count)
    {
        boolean isLogOutput = true;
        int read_bytes = 0;
        try
        {
            while (read_bytes <= 0)
            {
                sleep(delayMs);
                read_bytes = is.available();
                if (read_bytes <= 0)
                {
                    if (isLogOutput)
                    {
                        Log.v(TAG, "waitForReceive:: is.available() WAIT... : " + delayMs + "ms");
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
