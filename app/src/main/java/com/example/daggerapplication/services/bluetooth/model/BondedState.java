package com.example.daggerapplication.services.bluetooth.model;

public enum BondedState {

    NONE(10),
    BOUNDING(11),
    BONDED(12),
    UNDEFINED(999);

    private final int code;

    BondedState(int code) {
        this.code = code;
    }

    public static BondedState fromCode(int code) {
        for (BondedState state : BondedState.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return UNDEFINED;
    }
}
