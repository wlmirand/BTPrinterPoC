package org.universalpostalunion.printerlibrary.printer.escpos;

public enum ESCPOSConstant {

    CMP_ALIGNMENT_LEFT(0),
    CMP_ALIGNMENT_CENTER(1),
    CMP_ALIGNMENT_RIGHT(2),
    CMP_BITMAP_NORMAL(0),
    CMP_BITMAP_WIDTH_DOUBLE(1),
    CMP_BITMAP_HEIGHT_DOUBLE( 2),
    CMP_BITMAP_WIDTH_HEIGHT_DOUBLE(3),
    CMP_FNT_DEFAULT(0),
    CMP_FNT_FONTB(1),
    CMP_FNT_BOLD(8),
    CMP_FNT_REVERSE(16),
    CMP_FNT_UNDERLINE(128),
    CMP_FNT_UNDERLINE2(256),
    CMP_TXT_1WIDTH(0),
    CMP_TXT_2WIDTH(16),
    CMP_TXT_3WIDTH(32),
    CMP_TXT_4WIDTH(48),
    CMP_TXT_5WIDTH(64),
    CMP_TXT_6WIDTH( 80),
    CMP_TXT_7WIDTH(96),
    CMP_TXT_8WIDTH(112),
    CMP_TXT_1HEIGHT(0),
    CMP_TXT_2HEIGHT(1),
    CMP_TXT_3HEIGHT(2),
    CMP_TXT_4HEIGHT(3),
    CMP_TXT_5HEIGHT(4),
    CMP_TXT_6HEIGHT(5),
    CMP_TXT_7HEIGHT(6),
    CMP_TXT_8HEIGHT(7),
    CMP_BCS_UPCA(101),
    CMP_BCS_UPCE(102),
    CMP_BCS_EAN8(103),
    CMP_BCS_ITF(107),
    CMP_BCS_Code128(111),
    CMP_HRI_TEXT_NONE(0),
    CMP_HRI_TEXT_ABOVE(1),
    CMP_HRI_TEXT_BELOW(2);

    private final int code;

    ESCPOSConstant(final int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

}

