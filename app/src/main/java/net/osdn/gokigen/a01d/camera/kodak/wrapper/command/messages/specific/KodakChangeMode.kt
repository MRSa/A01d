package net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific

import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.IKodakMessages.SEQ_CHANGE_MODE
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.KodakCommandBase

class KodakChangeMode(private val callback: IKodakCommandCallback, modeValue0: Int, modeValue1: Int = 0x00) : KodakCommandBase()
{
    private val data0: Byte = modeValue0.toByte()
    private val data1: Byte = modeValue1.toByte()

    override fun getId() : Int
    {
        return (SEQ_CHANGE_MODE)
    }

    override fun commandBody(): ByteArray
    {
        return byteArrayOf(
            0x2e.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x20.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0xed.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x80.toByte(),

            0x12.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
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
            0x20.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x12.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x69.toByte(), 0x08.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x69.toByte(), 0x08.toByte(), 0x00.toByte(), 0x00.toByte(),

            data0, data1, 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),

            0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )
    }

    override fun responseCallback(): IKodakCommandCallback
    {
        return callback
    }
}