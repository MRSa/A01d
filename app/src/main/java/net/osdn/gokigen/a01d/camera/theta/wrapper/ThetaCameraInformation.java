package net.osdn.gokigen.a01d.camera.theta.wrapper;

import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThetaCameraInformation implements ICameraInformation, IOlyCameraPropertyProvider
{
    ThetaCameraInformation()
    {

    }

    @Override
    public boolean isManualFocus()
    {
        return false;
    }

    @Override
    public boolean isElectricZoomLens()
    {
        return false;
    }

    @Override
    public boolean isExposureLocked()
    {
        return false;
    }

    @Override
    public Set<String> getCameraPropertyNames() {
        return null;
    }

    @Override
    public String getCameraPropertyValue(String name) {
        return null;
    }

    @Override
    public Map<String, String> getCameraPropertyValues(Set<String> names) {
        return null;
    }

    @Override
    public String getCameraPropertyTitle(String name) {
        return null;
    }

    @Override
    public List<String> getCameraPropertyValueList(String name) {
        return null;
    }

    @Override
    public String getCameraPropertyValueTitle(String propertyValue) {
        return null;
    }

    @Override
    public void setCameraPropertyValue(String name, String value) {

    }

    @Override
    public void setCameraPropertyValues(Map<String, String> values) {

    }

    @Override
    public boolean canSetCameraProperty(String name) {
        return false;
    }
}
