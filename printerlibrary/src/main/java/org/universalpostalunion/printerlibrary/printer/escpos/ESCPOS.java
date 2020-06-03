package org.universalpostalunion.printerlibrary.printer.escpos;

class ESCPOS {

    byte[] ESC_EXCLAMATION(int n) {
        return this.EPCommand((byte) 27, '!', n);
    }

    byte[] ESC_HYPHEN(int n) {
        return this.EPCommand((byte) 27, '-', n);
    }

    byte[] ESC_E(int n) {
        return this.EPCommand((byte) 27, 'E', n);
    }

    byte[] ESC_J(int n) {
        return this.EPCommand((byte) 27, 'J', n);
    }

    byte[] ESC_M(int n) {
        return this.EPCommand((byte) 27, 'M', n);
    }

    byte[] ESC_a(int n) {
        return this.EPCommand((byte) 27, 'a', n);
    }

    byte[] ESC_d(int n) {
        return this.EPCommand((byte) 27, 'd', n);
    }

    byte[] ESC_t(int n) {
        return this.EPCommand((byte) 27, 't', n);
    }

    byte[] FS_EXCLAMATION(int n) {
        return this.EPCommand((byte) 28, '!', n);
    }

    byte[] FS_HYPHEN(int n) {
        return this.EPCommand((byte) 28, '-', n);
    }

    byte[] FS_p(int n, int m) {
        return this.EPCommand((byte) 28, 'p', n, m);
    }

    byte[] GS_EXCLAMATION(int n) {
        return this.EPCommand((byte) 29, '!', n);
    }

    byte[] GS_SLASH(int m) {
        return this.EPCommand((byte) 29, '/', m);
    }

    byte[] GS_B(int n) {
        return this.EPCommand((byte) 29, 'B', n);
    }

    byte[] GS_H(int n) {
        return this.EPCommand((byte) 29, 'H', n);
    }

    byte[] GS_h(int n) {
        return this.EPCommand((byte) 29, 'h', n);
    }

    byte[] GS_k(int m, int n, byte[] data) {
        byte[] command = new byte[4 + n];
        command[0] = 29;
        command[1] = 107;
        command[2] = (byte) m;
        command[3] = (byte) n;
        System.arraycopy(data, 0, command, 4, n);
        return command;
    }

    byte[] GS_v(int m, int xL, int xH, int yL, int yH, byte[] buf) {
        int length = buf.length;
        byte[] command = new byte[8 + length];
        command[0] = 29;
        command[1] = 118;
        command[2] = 48;
        command[3] = (byte) m;
        command[4] = (byte) xL;
        command[5] = (byte) xH;
        command[6] = (byte) yL;
        command[7] = (byte) yH;
        System.arraycopy(buf, 0, command, 8, length);
        return command;
    }

    byte[] GS_w(int n) {
        return this.EPCommand((byte) 29, 'w', n);
    }

    private byte[] EPCommand(byte group, char select, int n) {
        return new byte[]{group, (byte) select, (byte) n};
    }

    private byte[] EPCommand(byte group, char select, int n, int m) {
        return new byte[]{group, (byte) select, (byte) n, (byte) m};
    }

}

