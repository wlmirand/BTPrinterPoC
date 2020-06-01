package com.example.daggerapplication.services.printer.util;

public enum PrintCommand {

    TITLE_STYLE(new byte[]{0x1B, 0x21, 48, 0x1B, 0x45, 0x01, 0x1B, 0x2d, 0x01, 0x1D, 0x21, 0x01}),
    NORMAL_STYLE(new byte[] {0x1B, 0x21, 0, 0x1D, 0x21, 0x00,0x1B, 0x2d, 0x00,0x1B, 0x45, 0x00}),
    ESC_ALIGN_LEFT(new byte[] { 0x1b, 'a', 0x00 }),
    ESC_ALIGN_RIGHT(new byte[] { 0x1b, 'a', 0x02 }),
    ESC_ALIGN_CENTER(new byte[] { 0x1b, 'a', 0x01 }),
    ESC_LINE_FEED(new byte[] {10}),
    ESC_TAB(new byte[] {0x09}),
    ESC_BOLD(new byte[]{0x1B,0x45,0x01}),
    ESC_NO_BOLD(new byte[]{0x1B,0x45,0x00}),
    ESC_UNDERLINE(new byte[]{0x1B, 0x2d, 0x01}),
    ESC_NOT_UNDERLINE(new byte[]{0x1B, 0x2d, 0x00});

    private final byte[] bytes;

    PrintCommand(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] toBytes() {
        return bytes;
    }

}
