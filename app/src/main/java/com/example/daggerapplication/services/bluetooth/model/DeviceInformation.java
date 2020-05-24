package com.example.daggerapplication.services.bluetooth.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class DeviceInformation {
    private String key;
    private String name;
    private String address;
    @EqualsAndHashCode.Exclude
    private BondedState boundedStatus;
    @EqualsAndHashCode.Exclude
    private DeviceType selectedForDeviceType;
}
