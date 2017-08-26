package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;

import jp.co.olympus.camerakit.OLYCamera;

/**
 *
 *
 */
class OlyCameraWrapper
{
    private final Activity context;
    private final OLYCamera camera;

    OlyCameraWrapper(Activity context)
    {
        this.context = context;
        camera = new OLYCamera();
        camera.setContext(context.getApplicationContext());
    }

    OLYCamera getOLYCamera()
    {
        return (camera);
    }

}
