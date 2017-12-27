package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;

public interface IOlyCameraEntryList
{
    int MAX_STORE_PROPERTIES = 10;  // Olympus Airは、最大10個登録可能
    String NAME_KEY = "AirBtName";
    String CODE_KEY = "AirBtCode";
    String DATE_KEY = "AirBtId";
}
