package org.universalpostalunion.printerlibrary.printer.util;


public class MobileImageConverter {
    private int xL;
    private int xH;
    private int yL;
    private int yH;
    private int aX;
    private int aY;

    public MobileImageConverter() {
    }

    public int getaX() {
        return this.aX;
    }

    public int getaY() {
        return this.aY;
    }

    public int getxL() {
        return this.xL;
    }

    public int getxH() {
        return this.xH;
    }

    public int getyL() {
        return this.yL;
    }

    public int getyH() {
        return this.yH;
    }

    public int getByteWidth(int iWidth) {
        int byteWidth = iWidth / 8;
        if (iWidth % 8 != 0) {
            ++byteWidth;
        }

        return byteWidth;
    }

    public void setLHLength(int byteWidth, int iHeight) {
        this.xL = byteWidth % 256;
        this.xH = byteWidth / 256;
        this.yL = iHeight % 256;
        this.yH = iHeight / 256;
        if (this.xH > 255) {
            this.xH = 255;
        }

        if (this.yH > 8) {
            this.yH = 8;
        }

    }

    public void setLength(int iWidth, int iHeight) {
        int byteWidth = this.getByteWidth(iWidth);
        this.setLHLength(byteWidth, iHeight);
    }

    private int pow(int n, int times) {
        int retVal = 1;

        for(int i = 0; i < times; ++i) {
            retVal *= n;
        }

        return retVal;
    }

    public byte[] convertBitImage(int[][] tpix, int thresHoldValue) {
        int index = 0;
        int width = tpix.length;
        int height = tpix[0].length;
        int byteWidth = this.getByteWidth(width);
        this.setLHLength(byteWidth, height);
        byte[] result = new byte[byteWidth * height];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                if (x != 0 && x % 8 == 0) {
                    ++index;
                }

                if (tpix[x][y] <= thresHoldValue) {
                    result[index] |= (byte)this.pow(2, 7 - x % 8);
                }
            }

            ++index;
        }

        return result;
    }

    public byte[] convertBitImageReverse(int[][] tpix, int thresHoldValue) {
        int index = 0;
        int width = tpix.length;
        int height = tpix[0].length;
        int byteWidth = this.getByteWidth(width);
        this.setLHLength(byteWidth, height);
        byte[] result = new byte[byteWidth * height];

        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                if (x != 0 && x % 8 == 0) {
                    ++index;
                }

                if (tpix[x][y] > thresHoldValue) {
                    result[index] |= (byte)this.pow(2, 7 - x % 8);
                }
            }

            ++index;
        }

        return result;
    }

    public byte[] convertGSAsteriskImage(int[][] img, int thresHoldValue) {
        int index = 0;
        int width = img.length;
        int height = img[0].length;
        int bwidth = width;
        int remX = width % 8;
        if (remX != 0) {
            bwidth = width + remX;
        }

        this.aX = bwidth / 8;
        this.aY = height / 8;
        if (height % 8 != 0) {
            ++this.aY;
        }

        byte[] image = new byte[bwidth * this.aY];

        for(int i = 0; i < width; ++i) {
            for(int j = 0; j < height; ++j) {
                if (j != 0 && j % 8 == 0) {
                    ++index;
                }

                if (img[i][j] <= thresHoldValue) {
                    image[index] |= (byte)this.pow(2, 7 - j % 8);
                }
            }

            ++index;
        }

        return image;
    }
}

