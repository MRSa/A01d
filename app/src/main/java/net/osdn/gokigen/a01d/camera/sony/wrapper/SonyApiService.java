package net.osdn.gokigen.a01d.camera.sony.wrapper;

class SonyApiService
{
    private final String name;
    private final String actionUrl;

    SonyApiService(String name, String actionUrl)
    {
        this.name = name;
        this.actionUrl = actionUrl;
    }

    public String getName()
    {
        return (name);
    }
    public String getActionUrl()
    {
        return (actionUrl);
    }
}
