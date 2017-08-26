package net.osdn.gokigen.a01d.preference;

import android.content.SharedPreferences;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlyCameraProperty;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.CameraPropertyUtilities;

class PreferenceSynchronizer implements Runnable
{
    private final String TAG = toString();
    private final IOlyCameraPropertyProvider propertyInterface;
    private final SharedPreferences preference;
    private final IPropertySynchronizeCallback callback;

    PreferenceSynchronizer(IOlyCameraPropertyProvider propertyInterface, SharedPreferences preference, IPropertySynchronizeCallback callback)
    {
        this.propertyInterface = propertyInterface;
        this.preference = preference;
        this.callback = callback;
    }

    private String getPropertyValue(String key)
    {
        String propertyValue;
        try
        {
            String value = propertyInterface.getCameraPropertyValue(key);
            propertyValue = CameraPropertyUtilities.getPropertyValue(value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            propertyValue = "";
        }
        Log.v(TAG, "getPropertyValue(" + key + ") : " + propertyValue);
        return (propertyValue);
    }

    @Override
    public void run()
    {
        Log.v(TAG, "run()");
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.TAKE_MODE, getPropertyValue(IOlyCameraProperty.TAKE_MODE));
        editor.putString(net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, getPropertyValue(IOlyCameraProperty.SOUND_VOLUME_LEVEL));

        boolean value = getPropertyValue(IOlyCameraProperty.RAW).equals("ON");
        editor.putBoolean(net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.RAW, value);
        editor.apply();
        if (callback != null)
        {
            callback.synchronizedProperty();
        }
    }

    interface IPropertySynchronizeCallback
    {
        void synchronizedProperty();
    }
}
