package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_VIDEO
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandBase

class KodakExecuteVideo(private val callback: IKodakCommandCallback, isStop: Boolean = false, private val isDumpLog: Boolean = false) : KodakCommandBase()
{
    private val data0: Byte = if (isStop) 0x03.toByte() else 0x02.toByte()

    override fun getId() : Int
    {
        return (SEQ_VIDEO)
    }

    override fun dumpLog(): Boolean
    {
        return (isDumpLog)
    }

    override fun commandBody(): ByteArray
    {
        return byteArrayOf(

            //  (byte) 0xf9, (byte) 0x03
            0x2e.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x08.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0xf0.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x80.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x08.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            data0, 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
        )
    }

    override fun responseCallback(): IKodakCommandCallback
    {
        return callback
    }

}