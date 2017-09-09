package net.osdn.gokigen.a01d.camera.olympus.myolycameraprops;

class MyCameraPropertySetItems
{
    private final String itemId;
    private String itemName = "";
    private String itemInfo = "";
    private int iconResource = 0;

    MyCameraPropertySetItems(int iconResource, String itemId, String itemName, String itemInfo)
    {
        this.iconResource = iconResource;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemInfo = itemInfo;
    }

    String getItemId()
    {
        return itemId;
    }

    String getItemName()
    {
        return itemName;
    }

    String getItemInfo()
    {
        return itemInfo;
    }

    int getIconResource()
    {
        return iconResource;
    }
}
