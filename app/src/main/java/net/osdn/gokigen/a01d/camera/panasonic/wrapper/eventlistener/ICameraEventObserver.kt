package net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener

import net.osdn.gokigen.a01d.camera.ICameraChangeListener

interface ICameraEventObserver
{
    fun activate()
    fun start(): Boolean
    fun stop()
    fun release()

    fun setEventListener(listener: ICameraChangeListener)
    fun clearEventListener()

    fun getCameraStatusHolder(): ICameraStatusHolder
}
