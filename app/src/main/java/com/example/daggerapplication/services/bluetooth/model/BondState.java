package com.example.daggerapplication.services.bluetooth.model;

public enum BondState {
    BOND_NONE(10),
    BOND_BONDING(11),
    BOND_BONDED(12),
    UNKNOWN(-1);

    final int code;

    BondState(int code) {
        this.code = code;
    }

    public static BondState fromCode(int codeValue) {
        for (BondState currentState : values()) {
            if (currentState.code == codeValue) {
                return currentState;
            }
        }
        return UNKNOWN;
    }
}