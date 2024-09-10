package net.osdn.gokigen.a01d.camera.panasonic.wrapper

import net.osdn.gokigen.a01d.camera.ICameraChangeListener

interface IPanasonicCameraHolder
{
    fun detectedCamera(camera: IPanasonicCamera)
    fun prepare()
    fun startRecMode()
    fun startEventWatch(listener: ICameraChangeListener?)
}
