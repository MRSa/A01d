package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages

import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback


class PtpIpCommandGenericWithRetry(private val callback: IPtpIpCommandCallback, private val id: Int, private val delayMs: Int, private val retryCount: Int, private val isRetry: Boolean, private val isDumpLog: Boolean, private val holdId: Int, opcode: Int, private val bodySize: Int, value: Int, value2: Int = 0, value3: Int = 0, value4: Int = 0) : PtpIpCommandBase()
{
    private val opCode0: Byte = (0x000000ff and opcode).toByte()
    private val opCode1: Byte = (0x0000ff00 and opcode shr 8).toByte()
    private val data0: Byte = (0x000000ff and value).toByte()
    private val data1: Byte = (0x0000ff00 and value shr 8).toByte()
    private val data2: Byte = (0x00ff0000 and value shr 16).toByte()
    private val data3: Byte = (-0x1000000 and value shr 24).toByte()
    private val data4: Byte = (0x000000ff and value2).toByte()
    private val data5: Byte = (0x0000ff00 and value2 shr 8).toByte()
    private val data6: Byte = (0x00ff0000 and value2 shr 16).toByte()
    private val data7: Byte = (-0x1000000 and value2 shr 24).toByte()
    private val data8: Byte = (0x000000ff and value3).toByte()
    private val data9: Byte = (0x0000ff00 and value3 shr 8).toByte()
    private val dataA: Byte = (0x00ff0000 and value3 shr 16).toByte()
    private val dataB: Byte = (-0x1000000 and value3 shr 24).toByte()
    private val dataC: Byte = (0x000000ff and value4).toByte()
    private val dataD: Byte = (0x0000ff00 and value4 shr 8).toByte()
    private val dataE: Byte = (0x00ff0000 and value4 shr 16).toByte()
    private val dataF: Byte = (-0x1000000 and value4 shr 24).toByte()

    override fun responseCallback(): IPtpIpCommandCallback {
        return callback
    }

    override fun getId(): Int {
        return id
    }

    override fun estimatedReceiveDataSize(): Int {
        return -1
    }

    override fun commandBody(): ByteArray {
        return if (bodySize == 2) {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data ...
                    data0, data1)
        } else if (bodySize == 4) {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data ...
                    data0, data1, data2, data3)
        } else if (bodySize == 8) {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7)
        } else if (bodySize == 12) {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7,
                    data8, data9, dataA, dataB)
        } else if (bodySize == 16) {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data ...
                    data0, data1, data2, data3,
                    data4, data5, data6, data7,
                    data8, data9, dataA, dataB,
                    dataC, dataD, dataE, dataF)
        } else  //  ボディ長が 2, 4, 8, 12 以外の場合... (ボディなし)
        {
            byteArrayOf( // packet type
                    0x06.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // data phase info
                    0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),  // operation code
                    opCode0, opCode1,  // sequence number
                    0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        }
    }

    override fun receiveDelayMs(): Int {
        return delayMs
    }

    override fun getHoldId(): Int {
        return holdId
    }

    override fun maxRetryCount(): Int {
        return retryCount
    }

    override fun isRetrySend(): Boolean {
        return isRetry
    }

    override fun dumpLog(): Boolean {
        return isDumpLog
    }
}
