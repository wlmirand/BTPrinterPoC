package org.universalpostalunion.printerlibrary.printer.escpos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

class ESCPOSByteArrayOutputStream extends ByteArrayOutputStream {

    private final ESCPOS escpos;
    private Charset charSet;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ESCPOSByteArrayOutputStream(ESCPOS escpos) {
        this.escpos = escpos;
    }

    void setCharSet(Charset charSet) {
        this.charSet = charSet;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void parseJposCMD(String input) throws IOException {
        String div = "\u001b|";
        int length = input.length();
        int end = 0;
        int start = input.indexOf(div, end);
        if (start < 0) {
            write(input.getBytes(charSet));
        } else {
            while (true) {
                end = input.indexOf(div, start + 1);
                if (end < 0) {
                    this.convertCommand(input.substring(start, length));
                    write(escpos.ESC_EXCLAMATION(0));
                    write(escpos.FS_EXCLAMATION(0));
                    write(escpos.GS_B(0));
                    write(escpos.ESC_a(0));
                    write(escpos.ESC_M(0));
                    write(escpos.GS_EXCLAMATION(0));
                    write(escpos.ESC_HYPHEN(0));
                    write(escpos.FS_HYPHEN(0));
                    break;
                }

                this.convertCommand(input.substring(start, end));
                start = end;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private int lengthThreeCMD(String input) throws IOException {
        String command = input.substring(0, 3);
        if (command.equals("\u001b|N")) {
            write(escpos.ESC_EXCLAMATION(0));
            write(escpos.GS_B(0));
            write(escpos.ESC_a(0));
            write(escpos.ESC_M(0));
            write(escpos.ESC_HYPHEN(0));
            write(escpos.FS_HYPHEN(0));
            return 3;
        } else {
            return -1;
        }
    }

    private int lengthFourCMD(String input) throws IOException {
        String command = input.substring(0, 4);
        switch (command) {
            case "\u001b|1C":
                write(escpos.GS_EXCLAMATION(0));
                break;
            case "\u001b|2C":
                write(escpos.GS_EXCLAMATION(16));
                break;
            case "\u001b|3C":
                write(escpos.GS_EXCLAMATION(1));
                break;
            case "\u001b|4C":
                write(escpos.GS_EXCLAMATION(17));
                break;
            case "\u001b|rA":
                write(escpos.ESC_a(2));
                break;
            case "\u001b|cA":
                write(escpos.ESC_a(1));
                break;
            case "\u001b|lA":
                write(escpos.ESC_a(0));
                break;
            case "\u001b|uC":
                write(escpos.ESC_HYPHEN(1));
                write(escpos.FS_HYPHEN(1));
                break;
            case "\u001b|bC":
                write(escpos.ESC_E(1));
                break;
            default:
                if (command.charAt(3) != 'B') {
                    return -1;
                }

                write(escpos.GS_SLASH(0));
                break;
        }

        return 4;
    }

    private int lengthFiveCMD(String input) throws IOException {
        String command = input.substring(0, 5);
        if (command.equals("\u001b|!bC")) {
            write(escpos.ESC_E(0));
        } else if (command.equals("\u001b|1uC")) {
            write(escpos.ESC_HYPHEN(1));
            write(escpos.FS_HYPHEN(1));
        } else if (command.equals("\u001b|2uC")) {
            write(escpos.ESC_HYPHEN(2));
            write(escpos.FS_HYPHEN(2));
        } else if (!command.equals("\u001b|!uC") && !command.equals("\u001b|0uC")) {
            if (command.charAt(4) == 'B') {
                write(escpos.GS_SLASH(0));
            } else if (command.equals("\u001b|rvC")) {
                write(escpos.GS_B(1));
            } else {
                switch (command.substring(3, 5)) {
                    case "hC":
                        processHCCase(command);
                        break;
                    case "vC":
                        processVCCase(command);
                        break;
                    case "fT":
                        processFTCase(command);
                        break;
                    case "fP":
                        write(escpos.ESC_d(1));
                        break;
                    case "lF":
                        write(escpos.ESC_d(Integer.parseInt(command.substring(2, 3))));
                        break;
                    default:
                        if (!command.substring(3, 5).equals("uF")) {
                            return -1;
                        }
                        write(escpos.ESC_J(Integer.parseInt(command.substring(2, 3))));
                        break;
                }
            }
        } else {
            write(escpos.ESC_HYPHEN(0));
            write(escpos.FS_HYPHEN(0));
        }

        return 5;
    }

    private void processFTCase(String command) throws IOException {
        int temp;
        temp = Integer.parseInt(command.substring(2, 3));
        --temp;
        if (temp != 1) {
            temp = 0;
        }

        write(escpos.ESC_M(temp));
    }

    private void processVCCase(String command) throws IOException {
        int temp;
        temp = Integer.parseInt(command.substring(2, 3));
        if (temp >= 1 && temp <= 8) {
            --temp;
        } else {
            temp = 0;
        }

        write(escpos.GS_EXCLAMATION(temp));
    }

    private void processHCCase(String command) throws IOException {
        final int subCommand;
        switch (Integer.parseInt(command.substring(2, 3))) {
            case 2:
                subCommand = 16;
                break;
            case 3:
                subCommand = 32;
                break;
            case 4:
                subCommand = 48;
                break;
            case 5:
                subCommand = 64;
                break;
            case 6:
                subCommand = 80;
                break;
            case 7:
                subCommand = 96;
                break;
            case 8:
                subCommand = 112;
                break;
            default:
                subCommand = 0;
        }
        write(escpos.GS_EXCLAMATION(subCommand));
    }

    private int lengthSixCMD(String input) throws IOException {
        String command = input.substring(0, 6);
        if (command.substring(4, 6).equals("fP")) {
            write(escpos.ESC_d(1));
        } else {
            int temp;
            if (command.substring(4, 6).equals("lF")) {
                temp = Integer.parseInt(command.substring(2, 4));
                write(escpos.ESC_d(temp));
            } else if (command.substring(4, 6).equals("uF")) {
                temp = Integer.parseInt(command.substring(2, 4));
                write(escpos.ESC_J(temp));
            } else {
                if (!command.equals("\u001b|!rvC")) {
                    return -1;
                }

                write(escpos.GS_B(0));
            }
        }

        return 6;
    }

    private void convertCommand(String input) throws IOException {
        int length = input.length();
        if (length >= 3) {
            int retval = this.lengthThreeCMD(input);
            if (retval < 0 && length >= 4) {
                retval = this.lengthFourCMD(input);
            }

            if (retval < 0 && length >= 5) {
                retval = this.lengthFiveCMD(input);
            }

            if (retval < 0 && length >= 6) {
                retval = this.lengthSixCMD(input);
            }

            if (retval < 0) {
                write(input.getBytes(charSet));
            } else if (length > retval) {
                write(input.substring(retval, length).getBytes(charSet));
            }
        } else {
            write(input.getBytes(charSet));
        }
    }
}
