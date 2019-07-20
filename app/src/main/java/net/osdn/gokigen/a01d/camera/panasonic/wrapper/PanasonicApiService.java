package net.osdn.gokigen.a01d.camera.panasonic.wrapper;

class PanasonicApiService implements IPanasonicApiService
{
    private final String name;
    private final String actionUrl;

    PanasonicApiService(String name, String actionUrl)
    {
        this.name = name;
        this.actionUrl = actionUrl;
    }

    @Override
    public String getName()
    {
        return (name);
    }

    @Override
    public String getActionUrl()
    {
        return (actionUrl);
    }
}
