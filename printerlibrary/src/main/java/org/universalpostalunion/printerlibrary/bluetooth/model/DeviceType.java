package org.universalpostalunion.printerlibrary.bluetooth.model;

public enum DeviceType {
    PRINTER(1028),
    BAR_CODE_SCANNER(1632),
    UNKNOWN(-1);

    private final int code;

    DeviceType(final int code) {
        this.code = code;
    }

    public static DeviceType fromCode(int code) {
        for (DeviceType currentDeviceType : DeviceType.values()) {
            if (code == currentDeviceType.code) {
                return currentDeviceType;
            }
        }
        return DeviceType.UNKNOWN;
    }
}
