package org.universalpostalunion.printerlibrary.bluetooth.model;

public enum BluetoothState {
    ON(12),
    OFF(10),
    TURNING_OFF(13),
    TURNING_ON(11),
    UNKNOWN(-1);

    private final int code;

    BluetoothState(int code) {
        this.code = code;
    }

    public static BluetoothState fromCode(int code) {
        for (BluetoothState current : values()) {
            if (code == current.code) {
                return current;
            }
        }
        return UNKNOWN;
    }
}
