package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;

public interface IOlyCameraEntryList
{
    static final int MAX_STORE_PROPERTIES = 10;  // Olympus Airは、最大10個登録可能
    static final String NAME_KEY = "AirBtName";
    static final String CODE_KEY = "AirBtCode";
    static final String DATE_KEY = "AirBtId";
}
