package net.osdn.gokigen.a01d.camera.panasonic

import net.osdn.gokigen.a01d.camera.ICameraConnection
import net.osdn.gokigen.a01d.camera.ICameraInformation
import net.osdn.gokigen.a01d.camera.ICaptureControl
import net.osdn.gokigen.a01d.camera.IDisplayInjector
import net.osdn.gokigen.a01d.camera.IFocusingControl
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.IZoomLensControl
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener

interface IPanasonicInterfaceProvider
{
    fun getPanasonicCameraConnection(): ICameraConnection?
    fun getPanasonicLiveViewControl(): ILiveViewControl?
    fun getLiveViewListener(): ILiveViewListener?
    fun getFocusingControl(): IFocusingControl?
    fun getCameraInformation(): ICameraInformation?
    fun getZoomLensControl(): IZoomLensControl?
    fun getCaptureControl(): ICaptureControl?
    fun getDisplayInjector(): IDisplayInjector?

    fun getPanasonicCamera(): IPanasonicCamera?
}