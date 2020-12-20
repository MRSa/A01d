package net.osdn.gokigen.a01d.camera.canon.wrapper.status

import android.util.Log
import net.osdn.gokigen.a01d.camera.ICameraStatus
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommand
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGenericWithRetry
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.specific.InitEventRequest
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify
import java.io.DataOutputStream
import java.net.Socket
import java.util.*

class CanonStatusChecker(private val issuer: IPtpIpCommandPublisher, val ipAddress: String, val portNumber: Int) : IPtpIpCommandCallback, ICameraStatusWatcher, ICameraStatus
{
    private val statusHolder = CanonStatusHolder()
    private val logcat = false
    private lateinit var notifier: ICameraStatusUpdateNotify
    private lateinit var socket: Socket
    private lateinit var dos: DataOutputStream
    private var whileFetching = false
    private var eventConnectionNumber = 0

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, body: ByteArray)
    {
        Log.v(TAG, " $currentBytes/$totalBytes")
    }

    override fun isReceiveMulti(): Boolean
    {
        return (false)
    }

    override fun receivedMessage(id: Int, data: ByteArray)
    {
        try
        {
            logcat("receivedMessage : " + id + ", length: " + data.size)
            if (id == IPtpIpMessages.SEQ_EVENT_INITIALIZE)
            {
                // 終わる...んじゃなくて、イベント受信待ちに遷移する。
                Log.v(TAG, " ----- PTP-IP Connection is ESTABLISHED. -----")
                waitForEvent()
                return
            }
            if (data.size < STATUS_MESSAGE_HEADER_SIZE)
            {
                Log.v(TAG, " received status event length is short. (" + data.size + " bytes.)")
                return
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getStatusList(key: String): List<String>
    {
        try
        {
            return (statusHolder.getAvailableItemList(key))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    override fun getStatus(key: String): String
    {
        try
        {
            return (statusHolder.getItemStatus(key))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    override fun setStatus(key: String, value: String)
    {
        try
        {
            logcat("setStatus($key, $value)")

            //
            // ここで設定を行う。
            //
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startStatusWatch(notifier: ICameraStatusUpdateNotify)
    {
        if (whileFetching)
        {
            Log.v(TAG, "startStatusWatch() already starting.")
            return
        }
        try
        {
            this.notifier = notifier
            whileFetching = true

            // セッションをオープンする
            val isConnect = connect()
            if (!isConnect)
            {
                Log.v(TAG, "  CONNECT FAIL...(EVENT) : $ipAddress  $portNumber")
            }
            issueCommand(InitEventRequest(this, eventConnectionNumber))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stopStatusWatch()
    {
        Log.v(TAG, "stoptStatusWatch()")
        whileFetching = false
    }

    private fun logcat(message: String)
    {
        if (logcat)
        {
            Log.v(TAG, message)
        }
    }

    private fun connect(): Boolean
    {
        try
        {
            socket = Socket(ipAddress, portNumber)
            return (true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    fun setEventConnectionNumber(connectionNumber: Int)
    {
        eventConnectionNumber = connectionNumber
    }

    private fun issueCommand(command: IPtpIpCommand)
    {
        try
        {
            val commandBody = command.commandBody()
            if (commandBody != null)
            {
                // コマンドボディが入っていた場合には、コマンド送信（入っていない場合は受信待ち）
                sendToCamera(command.dumpLog(), commandBody)
            }
            receiveFromCamera(command.dumpLog(), command.id, command.responseCallback(), command.receiveAgainShortLengthMessage(), command.receiveDelayMs())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラにコマンドを送信する（メイン部分）
     *
     */
    private fun sendToCamera(isDumpReceiveLog: Boolean, byte_array: ByteArray)
    {
        try
        {
            if (!::socket.isInitialized)
            {
                // ソケットが初期化されていないので送らない
                Log.w(TAG, " DO NOT INITIALIZE Socket. (SEND)")
                return
            }

            // メッセージボディを加工： 最初に４バイトのレングス長をつける
            val sendData = ByteArray(byte_array.size + 4)
            sendData[0] = (byte_array.size + 4).toByte()
            sendData[1] = 0x00
            sendData[2] = 0x00
            sendData[3] = 0x00
            System.arraycopy(byte_array, 0, sendData, 4, byte_array.size)
            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                SimpleLogDumper.dump_bytes("Evt.SEND[" + sendData.size + "] ", sendData)
            }

            // (データを)送信
            if (!::dos.isInitialized)
            {
                dos = DataOutputStream(socket.getOutputStream())
            }
            dos.write(sendData)
            dos.flush()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * カメラからにコマンドの結果を受信する（メイン部分）
     *
     */
    private fun receiveFromCamera(isDumpReceiveLog: Boolean, id: Int, callback: IPtpIpCommandCallback?, receiveAgain: Boolean, delayMs: Int)
    {
        if (!::socket.isInitialized)
        {
            // ソケットが初期化されていないので受信しない
            Log.w(TAG, " DO NOT INITIALIZE Socket. (RECV)")
            return
        }

        try
        {
            sleep(delayMs)
            var isFirstTime = true
            var totalReadBytes: Int
            val receive_message_buffer_size = BUFFER_SIZE
            val byte_array = ByteArray(receive_message_buffer_size)
            val inputStream = socket.getInputStream()
            if (inputStream != null) {
                var read_bytes = inputStream.read(byte_array, 0, receive_message_buffer_size)
                val receive_body: ByteArray
                if (read_bytes > 4) {
                    if (receiveAgain) {
                        val length = (byte_array[3].toInt() and 0xff shl 24) + (byte_array[2].toInt() and 0xff shl 16) + (byte_array[1].toInt() and 0xff shl 8) + (byte_array[0].toInt() and 0xff)
                        if (length > receive_message_buffer_size) {
                            Log.v(TAG, "+++++ TOTAL RECEIVE MESSAGE SIZE IS $length +++++")
                        }
                        totalReadBytes = read_bytes
                        while (length > totalReadBytes || length == read_bytes && byte_array[4].toInt() == 0x02) {
                            // データについて、もう一回受信が必要な場合...
                            if (isDumpReceiveLog) {
                                Log.v(TAG, "--- RECEIVE AGAIN --- [" + length + "(" + read_bytes + ") " + byte_array[4] + "] ")
                            }
                            sleep(delayMs)
                            val read_bytes2 = inputStream.read(byte_array, read_bytes, receive_message_buffer_size - read_bytes)
                            if (read_bytes2 > 0) {
                                read_bytes = read_bytes + read_bytes2
                                totalReadBytes = totalReadBytes + read_bytes2
                            } else {
                                // よみだし終了。
                                Log.v(TAG, "FINISHED RECEIVE... ")
                                break
                            }
                            if (callback != null) {
                                if (callback.isReceiveMulti) {
                                    var offset = 0
                                    if (isFirstTime) {
                                        // 先頭のヘッダ部分をカットして送る
                                        offset = 12
                                        isFirstTime = false
                                        //Log.v(TAG, " FIRST TIME : " + read_bytes + " " + offset);
                                    }
                                    callback.onReceiveProgress(read_bytes - offset, length, Arrays.copyOfRange(byte_array, offset, read_bytes))
                                    read_bytes = 0
                                } else {
                                    callback.onReceiveProgress(read_bytes, length, null)
                                }
                            }
                        }
                    }
                    receive_body = Arrays.copyOfRange(byte_array, 0, read_bytes)
                } else {
                    receive_body = ByteArray(1)
                }
                if (isDumpReceiveLog) {
                    // ログに受信メッセージを出力する
                    Log.v(TAG, " receive_from_camera() : $read_bytes bytes.")
                    SimpleLogDumper.dump_bytes(" Evt.RECV[" + receive_body.size + "] ", receive_body)
                }
                if (callback != null) {
                    if (callback.isReceiveMulti) {
                        callback.receivedMessage(id, null)
                    } else {
                        callback.receivedMessage(id, receive_body)
                        //callback.receivedMessage(id, Arrays.copyOfRange(receive_body, 0, receive_body.length));
                    }
                }
            }
        }
        catch (e: Throwable)
        {
            e.printStackTrace()
        }
    }

    private fun sleep(delayMs: Int)
    {
        try
        {
            Thread.sleep(delayMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun waitForEvent()
    {
        val delayMs = 25
        try
        {
            val thread = Thread(object : Runnable {
                override fun run()
                {
                    try
                    {
                        Log.v(TAG, " waitForEvent : $whileFetching")
                        sleep(delayMs)

                        // 受信待ちする...
                        receiveFromCamera(true, IPtpIpMessages.SEQ_EVENT_RECEIVE, object : IPtpIpCommandCallback {
                            override fun receivedMessage(id: Int, rx_body: ByteArray)
                            {
                                try
                                {
                                    //  メッセージを受信。 応答を返さないといけない...
                                    sendReplyMessage(rx_body)

                                    //  そして次のイベントを待つ
                                    waitForEvent()
                                }
                                catch (e: Exception)
                                {
                                    e.printStackTrace()
                                }
                            }

                            override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray)
                            {
                                Log.v(TAG, " onReceiveProgress : [$currentBytes/$totalBytes]")
                            }

                            override fun isReceiveMulti(): Boolean
                            {
                                return (false)
                            }
                        }, true, delayMs)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            })
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendReplyMessage(received_message: ByteArray)
    {
        try
        {
            Log.v(TAG, " RECEIVE : " + received_message.size + " bytes.")
            if (received_message.size >= 26)
            {
                // 受信パケットを解析
                val packetType = received_message[4].toUByte().toInt() and 0xff
                val eventCode = (received_message[9].toUByte().toInt() and 0xff shl 8) + (received_message[8].toUByte().toInt() and 0xff)
                val parameter1 = (received_message[17].toUByte().toInt() and 0xff shl 24) + (received_message[16].toUByte().toInt() and 0xff shl 16) + (received_message[15].toUByte().toInt() and 0xff shl 8) + (received_message[14].toUByte().toInt() and 0xff)
                val parameter2 = (received_message[21].toUByte().toInt() and 0xff shl 24) + (received_message[20].toUByte().toInt() and 0xff shl 16) + (received_message[19].toUByte().toInt() and 0xff shl 8) + (received_message[18].toUByte().toInt() and 0xff)
                val parameter3 = (received_message[25].toUByte().toInt() and 0xff shl 24) + (received_message[24].toUByte().toInt() and 0xff shl 16) + (received_message[23].toUByte().toInt() and 0xff shl 8) + (received_message[22].toUByte().toInt() and 0xff)
                Log.v(TAG, String.format(" event : 0x%x, code: 0x%x, prm1: 0x%x, prm2: 0x%x, prm3 : 0x%x", packetType, eventCode, parameter1, parameter2, parameter3))
                if (eventCode == 0xc101)
                {
                    // イベントの受信指示
                    issuer.enqueueCommand(PtpIpCommandGenericWithRetry(this, IPtpIpMessages.SEQ_GET_STATUS, 50, 200, isRetry = false, isDumpLog = true, holdId = 0, opcode = 0x9116, bodySize = 0, value = 0))
                    Log.v(TAG, " SEND [Get Status] ")
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val BUFFER_SIZE = 1024 * 1024 + 8
        private const val STATUS_MESSAGE_HEADER_SIZE = 14
        private const val TAG = "CanonStatusChecker"
    }

}
