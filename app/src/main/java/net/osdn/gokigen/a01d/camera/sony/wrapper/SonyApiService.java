package net.osdn.gokigen.a01d.camera.sony.wrapper;

class SonyApiService implements ISonyApiService
{
    private final String name;
    private final String actionUrl;

    SonyApiService(String name, String actionUrl)
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
