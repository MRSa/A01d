package net.osdn.gokigen.a01d.camera.canon.wrapper.connection

import android.app.Activity
import android.graphics.Color
import android.util.Log
import net.osdn.gokigen.a01d.R
import net.osdn.gokigen.a01d.camera.ICameraConnection
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver
import net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific.CanonRegistrationMessage
import net.osdn.gokigen.a01d.camera.canon.wrapper.command.messages.specific.CanonSetDevicePropertyValue
import net.osdn.gokigen.a01d.camera.canon.wrapper.status.CanonStatusChecker
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric

class CanonCameraConnectSequenceType1(val context: Activity, val cameraStatusReceiver: ICameraStatusReceiver, val cameraConnection: ICameraConnection, val interfaceProvider: IPtpIpInterfaceProvider, val statusChecker: CanonStatusChecker) : Runnable, IPtpIpCommandCallback, IPtpIpMessages
{
    private val isDumpLog = false
    private val commandIssuer = interfaceProvider.commandPublisher
    private var requestMessageCount = 0

    override fun run()
    {
        try
        {
            // カメラとTCP接続
            val issuer = interfaceProvider.commandPublisher
            if (!issuer.isConnected)
            {
                if (!interfaceProvider.commandCommunication.connect())
                {
                    // 接続失敗...
                    interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED)
                    cameraConnection.alertConnectingFailed(context.getString(R.string.dialog_title_connect_failed_canon))
                    return
                }
            }
            else
            {
                Log.v(TAG, "SOCKET IS ALREADY CONNECTED...")
            }
            // コマンドタスクの実行開始
            issuer.start()

            // 接続シーケンスの開始
            sendRegistrationMessage()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.dialog_title_connect_failed_canon), false, true, Color.RED)
            cameraConnection.alertConnectingFailed(e.message)
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, body: ByteArray?)
    {
        Log.v(TAG, " $currentBytes/$totalBytes")
    }

    override fun isReceiveMulti(): Boolean
    {
        return false
    }

    @ExperimentalUnsignedTypes
    override fun receivedMessage(id: Int, rx_body: ByteArray)
    {
        when (id)
        {
            IPtpIpMessages.SEQ_REGISTRATION -> if (checkRegistrationMessage(rx_body)) {
                    sendInitEventRequest(rx_body)
            } else {
                    cameraConnection.alertConnectingFailed(context.getString(R.string.connect_error_message))
            }
            IPtpIpMessages.SEQ_EVENT_INITIALIZE -> if (checkEventInitialize(rx_body)) {
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting1), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41))
            } else {
                cameraConnection.alertConnectingFailed(context.getString(R.string.connect_error_message))
            }
            IPtpIpMessages.SEQ_OPEN_SESSION -> {
                Log.v(TAG, " SEQ_OPEN_SESSION ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting2), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_INIT_SESSION, isDumpLog, 0, 0x902f))
            }
            IPtpIpMessages.SEQ_INIT_SESSION -> {
                Log.v(TAG, " SEQ_INIT_SESSION ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting3), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_CHANGE_REMOTE, isDumpLog, 0, 0x9114, 4, 0x15))
            }
            IPtpIpMessages.SEQ_CHANGE_REMOTE -> {
                Log.v(TAG, " SEQ_CHANGE_REMOTE ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting4), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_SET_EVENT_MODE, isDumpLog, 0, 0x9115, 4, 0x02))
            }
            IPtpIpMessages.SEQ_SET_EVENT_MODE -> {
                Log.v(TAG, " SEQ_SET_EVENT_MODE ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting5), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_EVENT, isDumpLog, 0, 0x913d, 4, 0x0fff))
            }
            IPtpIpMessages.SEQ_GET_EVENT -> {
                Log.v(TAG, " SEQ_GET_EVENT ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting6), false, false, 0)
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_EVENT1, isDumpLog, 0, 0x9033, 4, 0x00000000))
            }
            IPtpIpMessages.SEQ_GET_EVENT1 -> {
                Log.v(TAG, " SEQ_GET_EVENT1 ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting7), false, false, 0)
                //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_INFORMATION, isDumpLog, 0, 0x1001));
                commandIssuer!!.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_SET_REMOTE_SHOOTING_MODE, isDumpLog, 0, 0x1001))
            }
            IPtpIpMessages.SEQ_DEVICE_INFORMATION -> {
                Log.v(TAG, " SEQ_DEVICE_INFORMATION ")
                requestMessageCount = 0
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting8), false, false, 0)
                //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1a6));
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1a6))
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d169))
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d16a))
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d16b))
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY, isDumpLog, 0, 0x9127, 4, 0x0000d1af))
            }
            IPtpIpMessages.SEQ_DEVICE_PROPERTY -> {
                requestMessageCount++
                Log.v(TAG, " SEQ_DEVICE_PROPERTY : $requestMessageCount")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting9), false, false, 0)
                if (rx_body[8] == 0x01.toByte() && rx_body[9] == 0x20.toByte())
                {
                    // コマンドが受け付けられたときだけ次に進む！
                    try
                    {
                        // ちょっと(250ms)待つ
                        Thread.sleep(250)

                        if (requestMessageCount >= 5)
                        {
                            // コマンド発行
                            //commandIssuer.enqueueCommand(CanonSetDevicePropertyValue(this, IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY, isDumpLog, 0, 150, 0xd136, 0x00))
                            commandIssuer.enqueueCommand(CanonSetDevicePropertyValue(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY_FINISHED, isDumpLog, 0, 300, 0xd1b0, 0x09))
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY -> {
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY ")
                requestMessageCount = 0
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting10), false, false, 0)
                commandIssuer.enqueueCommand(CanonSetDevicePropertyValue(this, IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY_2, isDumpLog, 0, 150, 0xd136, 0x01))
            }
            IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY_2 -> {
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY_2 ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting11), false, false, 0)
                commandIssuer.enqueueCommand(CanonSetDevicePropertyValue(this, IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY_3, isDumpLog, 0, 150, 0xd136, 0x00))
            }
            IPtpIpMessages.SEQ_SET_DEVICE_PROPERTY_3 -> {
                Log.v(TAG, " SEQ_SET_DEVICE_PROPERTY_3 ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting12), false, false, 0)
                //commandIssuer.enqueueCommand(new CanonSetDevicePropertyValue(this, SEQ_SET_REMOTE_SHOOTING_MODE, isDumpLog, 0, 300, 0xd1b0, 0x08));
                commandIssuer.enqueueCommand(CanonSetDevicePropertyValue(this, IPtpIpMessages.SEQ_DEVICE_PROPERTY_FINISHED, isDumpLog, 0, 300, 0xd1b0, 0x08))
            }
            IPtpIpMessages.SEQ_SET_REMOTE_SHOOTING_MODE -> {
                Log.v(TAG, " SEQ_SET_REMOTE_SHOOTING_MODE ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.canon_connect_connecting12), false, false, 0)
                try {
                    // ちょっと(250ms)待つ
                    Thread.sleep(250)

                    // コマンド発行
                    //commandIssuer.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_PROPERTY_FINISHED, isDumpLog, 0, 0x9086, 4, 0x00000001));
                    commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_INFORMATION, isDumpLog, 0, 0x9086, 4, 0x00000001))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            IPtpIpMessages.SEQ_DEVICE_PROPERTY_FINISHED -> {
                Log.v(TAG, " SEQ_DEVICE_PROPERTY_FINISHED ")
                interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connect_finished), false, false, 0)
                connectFinished()
                Log.v(TAG, "CHANGED MODE : DONE.")
            }
            else -> {
                Log.v(TAG, "RECEIVED UNKNOWN ID : $id")
                cameraConnection.alertConnectingFailed(context.getString(R.string.connect_receive_unknown_message))
            }
        }
    }

    private fun sendRegistrationMessage()
    {
        interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_start), false, false, 0)
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start))
        commandIssuer.enqueueCommand(CanonRegistrationMessage(this))
    }

    @ExperimentalUnsignedTypes
    private fun sendInitEventRequest(receiveData: ByteArray)
    {
        interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_start_2), false, false, 0)
        cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start_2))
        try
        {
            var eventConnectionNumber: Int = receiveData[8].toUByte().toInt() and 0xff
            eventConnectionNumber += (receiveData[9].toUByte().toInt() and 0xff shl 8)
            eventConnectionNumber += (receiveData[10].toUByte().toInt() and 0xff shl 16)
            eventConnectionNumber += (receiveData[11].toUByte().toInt() and 0xff shl 24)
            statusChecker.setEventConnectionNumber(eventConnectionNumber)
            interfaceProvider.cameraStatusWatcher.startStatusWatch(interfaceProvider.statusListener)
            commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_OPEN_SESSION, isDumpLog, 0, 0x1002, 4, 0x41))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkRegistrationMessage(receiveData: ByteArray?): Boolean
    {
        // データ(Connection Number)がないときにはエラーと判断する
        return !(receiveData == null || receiveData.size < 12)
    }

    private fun checkEventInitialize(receiveData: ByteArray?): Boolean
    {
        Log.v(TAG, "checkEventInitialize() ")
        return receiveData != null
    }

    private fun connectFinished()
    {
        try
        {
            // 接続成功のメッセージを出す
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connected), false, false, 0)

            // ちょっと待つ
            Thread.sleep(1000)

            // 接続成功！のメッセージを出す
            interfaceProvider.informationReceiver.updateMessage(context.getString(R.string.connect_connected), false, false, 0)
            onConnectNotify()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onConnectNotify()
    {
        try
        {
            val thread = Thread {
                // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected))
                cameraStatusReceiver.onCameraConnected()
                Log.v(TAG, " onConnectNotify()")
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val TAG = "CanonConnectSeq.1"
    }
}