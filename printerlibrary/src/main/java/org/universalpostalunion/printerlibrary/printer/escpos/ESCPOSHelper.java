package org.universalpostalunion.printerlibrary.printer.escpos;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.universalpostalunion.printerlibrary.printer.util.ImageLoader;
import org.universalpostalunion.printerlibrary.printer.util.MobileImageConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ESCPOSHelper {

    private static final byte GS = 29;
    private final byte[] LF = {10};
    private ESCPOS escpos;
    private Charset charSet;

    private final ESCPOSByteArrayOutputStream posByteArrayOutputStream;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public ESCPOSHelper() {
        charSet = StandardCharsets.ISO_8859_1;
        escpos = new ESCPOS();
        posByteArrayOutputStream = new ESCPOSByteArrayOutputStream(escpos);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public ByteArrayOutputStream getOutputStream() {
        return posByteArrayOutputStream;
    }

    public void initializeByteArrayOutputStream(Charset charSet) throws IOException {
        this.charSet = charSet;
        posByteArrayOutputStream.reset();
        posByteArrayOutputStream.setCharSet(charSet);
    }

    public void initializeByteArrayOutputStream(Charset charSet, int codePage) throws IOException {
        initializeByteArrayOutputStream(charSet);
        if (codePage >= 0 && codePage <= 58) {
            if ((codePage < 6 || codePage > 15) && (codePage < 20 || codePage > 39)) {
                posByteArrayOutputStream.write(this.escpos.ESC_t(codePage));
            }
        }
    }

    public void setAlignment(int alignment) throws IOException {
        posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
    }

    public void setText(int textSize, int attribute) throws IOException {
        if ((attribute & 1) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_EXCLAMATION(1));
        } else {
            posByteArrayOutputStream.write(this.escpos.ESC_EXCLAMATION(0));
        }

        posByteArrayOutputStream.write(this.escpos.GS_EXCLAMATION(textSize));
        if ((attribute & 8) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_E(1));
        } else {
            posByteArrayOutputStream.write(this.escpos.ESC_E(0));
        }

        if ((attribute & 16) > 0) {
            posByteArrayOutputStream.write(this.escpos.GS_B(1));
        } else {
            posByteArrayOutputStream.write(this.escpos.GS_B(0));
        }

        if ((attribute & 256) <= 0 && (attribute & 128) <= 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(0));
        } else if ((attribute & 256) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(2));
        } else if ((attribute & 128) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(1));
        }
    }

    public void printNormal(String data) throws IOException {
        this.posByteArrayOutputStream.parseJposCMD(data);
    }

    public void printString(String data) throws IOException {
        posByteArrayOutputStream.write(data.getBytes(this.charSet));
    }

    public void printText(String data, int alignment, int attribute, int textSize) throws IOException {
        posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
        if ((attribute & 1) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_EXCLAMATION(1));
        }

        posByteArrayOutputStream.write(this.escpos.GS_EXCLAMATION(textSize));
        if ((attribute & 8) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_E(1));
        }

        if ((attribute & 16) > 0) {
            posByteArrayOutputStream.write(this.escpos.GS_B(1));
        }

        if ((attribute & 256) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(2));
            posByteArrayOutputStream.write(this.escpos.FS_HYPHEN(2));
        } else if ((attribute & 128) > 0) {
            posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(1));
            posByteArrayOutputStream.write(this.escpos.FS_HYPHEN(1));
        }

        posByteArrayOutputStream.write(data.getBytes(this.charSet));
        posByteArrayOutputStream.write(this.escpos.ESC_EXCLAMATION(0));
        posByteArrayOutputStream.write(this.escpos.FS_EXCLAMATION(0));
        posByteArrayOutputStream.write(this.escpos.ESC_E(0));
        posByteArrayOutputStream.write(this.escpos.GS_B(0));
        posByteArrayOutputStream.write(this.escpos.ESC_HYPHEN(0));
        posByteArrayOutputStream.write(this.escpos.FS_HYPHEN(0));
        posByteArrayOutputStream.write(this.escpos.ESC_a(0));
    }

    public int printBitmap(String bitmapName, int alignment) throws IOException {
        return this.printBitmap(bitmapName, alignment, 0, 0);
    }

    public int printBitmap(String bitmapName, int alignment, int size) throws IOException {
        return this.printBitmap(bitmapName, alignment, size, 0);
    }

    public int printBitmap(Bitmap bitmap, int alignment) throws IOException {
        return this.printBitmap((Bitmap) bitmap, alignment, 0);
    }

    public int printBitmap(Bitmap bitmap, int alignment, int size) throws IOException {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        if (originalWidth != size && size >= 4) {
            int heightRatio;
            if (originalWidth > size) {
                heightRatio = originalHeight / (originalWidth / size);
            } else {
                heightRatio = originalHeight * (size / originalWidth);
            }

            Bitmap outbitmap = Bitmap.createScaledBitmap(bitmap, size, heightRatio, false);
            this.printBitmapBrightness(outbitmap, alignment, 0, 0, 0);
            return 0;
        } else {
            ImageLoader imageLoader = new ImageLoader();
            MobileImageConverter mConverter = new MobileImageConverter();
            int[][] img = imageLoader.getByteArray(bitmap);
            if (img != null) {
                byte[] bimg = mConverter.convertBitImage(img, imageLoader.getThresHoldValue());
                posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
                posByteArrayOutputStream.write(this.escpos.GS_v(size, mConverter.getxL(), mConverter.getxH(), mConverter.getyL(), mConverter.getyH(), bimg));
                posByteArrayOutputStream.write(this.escpos.ESC_a(0));
                return 0;
            } else {
                return -1;
            }
        }
    }

    public void printBarCode(String data, int symbology, int height, int width, int alignment, int textPosition) throws IOException {
        int symbol = this.getBarCodeSymbol(symbology);
        byte[] dataBuffer;
        if (symbol == 73) {
            dataBuffer = new String("{B" + data).getBytes();
        } else {
            dataBuffer = data.getBytes(this.charSet);
        }

        posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
        posByteArrayOutputStream.write(this.escpos.GS_w(width));
        posByteArrayOutputStream.write(this.escpos.GS_h(height));
        posByteArrayOutputStream.write(this.escpos.GS_H(textPosition));
        posByteArrayOutputStream.write(this.escpos.GS_k(symbol, dataBuffer.length, dataBuffer));
        posByteArrayOutputStream.write(this.escpos.ESC_a(0));
    }

    public void lineFeed(int lfCount) throws IOException {
        for (int i = 0; i < lfCount; ++i) {
            posByteArrayOutputStream.write(this.LF);
        }
    }

    public int printFromAndroidFont(Typeface typeface, boolean isBold, boolean isItalic, boolean isUnderline, String textString, int widthDots, int textSize, int alignment) throws IOException {
        Bitmap croppedBmp = this.drawText(typeface, isBold, isItalic, isUnderline, textString, widthDots, textSize, alignment);
        ImageLoader imageLoader = new ImageLoader();
        MobileImageConverter mConverter = new MobileImageConverter();
        int[][] img = imageLoader.getByteArray(croppedBmp);
        if (img != null) {
            byte[] bimg = mConverter.convertBitImage(img, imageLoader.getThresHoldValue());

            posByteArrayOutputStream.write(this.escpos.GS_v(0, mConverter.getxL(), mConverter.getxH(), mConverter.getyL(), mConverter.getyH(), bimg));
            return 0;
        } else {
            return -1;
        }
    }

    protected int getBarCodeSymbol(int select) {
        int retVal = 0;
        switch (select) {
            case 101:
                retVal = 65;
                break;
            case 102:
                retVal = 66;
                break;
            case 103:
                retVal = 68;
                break;
            case 104:
                retVal = 67;
                break;
            case 105:
                retVal = 68;
                break;
            case 106:
                retVal = 67;
                break;
            case 107:
                retVal = 70;
                break;
            case 108:
                retVal = 71;
                break;
            case 109:
                retVal = 69;
                break;
            case 110:
                retVal = 72;
                break;
            case 111:
                retVal = 73;
        }

        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private int divideBitmap(int m, int xL, int xH, int yL, int yH, byte[] buf) throws IOException {
        byte[] command = new byte[]{29, 118, 48, (byte) m, (byte) xL, (byte) xH, (byte) yL, (byte) yH};
        posByteArrayOutputStream.write(command);
        int sendedSize = 0;
        int maxSize;
        if (buf.length > 4096) {
            maxSize = 4096;
        } else {
            maxSize = buf.length;
        }

        int totalSize;
        int remainSize = totalSize = buf.length;
        if (totalSize > maxSize) {
            while (remainSize > 0) {
                byte[] sendBuffer;
                if (remainSize > maxSize) {
                    sendBuffer = new byte[maxSize];
                    System.arraycopy(buf, sendedSize, sendBuffer, 0, maxSize);
                    posByteArrayOutputStream.write(sendBuffer);
                    sendedSize += maxSize;
                    remainSize = totalSize - sendedSize;
                } else {
                    sendBuffer = new byte[remainSize];
                    System.arraycopy(buf, sendedSize, sendBuffer, 0, remainSize);
                    posByteArrayOutputStream.write(sendBuffer);
                    sendedSize += remainSize;
                    remainSize = totalSize - sendedSize;
                }
            }
        } else {
            posByteArrayOutputStream.write(buf);
        }

        return 0;
    }

    private int printBitmap(String bitmapName, int alignment, int size, int mode) throws IOException {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapName, options);
        int orgwidth = options.outWidth;
        int orgheight = options.outHeight;
        if (orgwidth != size && size >= 4) {
            int heightratio;
            if (orgwidth > size) {
                heightratio = orgheight / (orgwidth / size);
            } else {
                heightratio = orgheight * (size / orgwidth);
            }

            options.inSampleSize = this.calculateInSampleSize(options, size, heightratio);
            options.inJustDecodeBounds = false;
            Bitmap orgbitmap = BitmapFactory.decodeFile(bitmapName, options);
            Bitmap bitmap = Bitmap.createScaledBitmap(orgbitmap, size, heightratio, false);
            this.printBitmapBrightness(bitmap, alignment, 0, 0, 0);
            return 0;
        } else {
            ImageLoader imageLoader = new ImageLoader();
            int[][] img = imageLoader.imageLoad(bitmapName);
            if (img != null) {
                MobileImageConverter mConverter = new MobileImageConverter();
                byte[] bimg = mConverter.convertBitImage(img, imageLoader.getThresHoldValue());
                posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
                posByteArrayOutputStream.write(this.escpos.GS_v(size, mConverter.getxL(), mConverter.getxH(), mConverter.getyL(), mConverter.getyH(), bimg));
                posByteArrayOutputStream.write(this.escpos.ESC_a(0));
                return 0;
            } else {
                return -1;
            }
        }
    }

    private int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = Math.round((float) height / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        return inSampleSize;
    }

    private int printBitmapBrightness(Bitmap bitmap, int alignment, int size, int bright, int reverse) throws IOException {
        ImageLoader imageLoader = new ImageLoader();
        MobileImageConverter mConverter = new MobileImageConverter();
        int[][] img = imageLoader.getByteArray(bitmap);
        if (img != null) {
            int thresh = imageLoader.getThresHoldValue();
            byte[] bimg;
            if (reverse == 1) {
                bimg = mConverter.convertBitImageReverse(img, thresh + 60 - bright);
            } else {
                bimg = mConverter.convertBitImage(img, thresh + 60 - bright);
            }

            posByteArrayOutputStream.write(this.escpos.ESC_a(alignment));
            posByteArrayOutputStream.write(this.escpos.GS_v(size, mConverter.getxL(), mConverter.getxH(), mConverter.getyL(), mConverter.getyH(), bimg));
            posByteArrayOutputStream.write(this.escpos.ESC_a(0));
            return 0;
        } else {
            return -1;
        }
    }

    private Bitmap drawText(String text, int textWidth, int textSize, int alignment) {
        TextPaint textPaint = new TextPaint(65);
        textPaint.setStyle(Style.FILL);
        textPaint.setColor(-16777216);
        textPaint.setTextSize((float) textSize);
        StaticLayout mTextLayout = null;
        switch (alignment) {
            case 0:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
                break;
            case 1:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
                break;
            case 2:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, false);
        }

        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Config.RGB_565);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(65);
        paint.setStyle(Style.FILL);
        paint.setColor(-1);
        c.drawPaint(paint);
        c.save();
        c.translate(0.0F, 0.0F);
        mTextLayout.draw(c);
        c.restore();
        return b;
    }

    private Bitmap drawText(Typeface typeface, String text, int textWidth, int textSize, int alignment) {
        TextPaint textPaint = new TextPaint(65);
        textPaint.setStyle(Style.FILL);
        textPaint.setColor(-16777216);
        textPaint.setTypeface(typeface);
        textPaint.setTextSize((float) textSize);
        StaticLayout mTextLayout = null;
        switch (alignment) {
            case 0:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
                break;
            case 1:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
                break;
            case 2:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, false);
        }

        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Config.RGB_565);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(65);
        paint.setStyle(Style.FILL);
        paint.setColor(-1);
        c.drawPaint(paint);
        c.save();
        c.translate(0.0F, 0.0F);
        mTextLayout.draw(c);
        c.restore();
        return b;
    }

    private Bitmap drawText(Typeface typeface, boolean isBold, boolean isItalic, boolean isUnderline, String text, int textWidth, int textSize, int alignment) {
        TextPaint textPaint = new TextPaint(65);
        textPaint.setStyle(Style.FILL);
        textPaint.setColor(-16777216);
        textPaint.setTypeface(typeface);
        textPaint.setFakeBoldText(isBold);
        if (isItalic) {
            textPaint.setTextSkewX(-0.25F);
        } else {
            textPaint.setTextSkewX(0.0F);
        }

        textPaint.setUnderlineText(isUnderline);
        textPaint.setTextSize((float) textSize);
        StaticLayout mTextLayout = null;
        switch (alignment) {
            case 0:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
                break;
            case 1:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
                break;
            case 2:
                mTextLayout = new StaticLayout(text, textPaint, textWidth, Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, false);
        }

        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Config.RGB_565);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(65);
        paint.setStyle(Style.FILL);
        paint.setColor(-1);
        c.drawPaint(paint);
        c.save();
        c.translate(0.0F, 0.0F);
        mTextLayout.draw(c);
        c.restore();
        return b;
    }


    private byte[] convertCodeC(String data) {
        int index = 0;
        boolean lastOne = false;
        int length = data.length();
        byte[] result = new byte[(length - 2) / 2 + (length - 2) % 2 + 2];
        if (length % 2 > 0) {
            lastOne = true;
        }

        length = result.length;
        index = index + 1;
        result[index] = 123;
        result[index++] = 67;

        for (int j = 2; index < length; ++index) {
            if (index == length - 1 && lastOne) {
                result[index] = Byte.parseByte(data.substring(j, j + 1));
            } else {
                result[index] = Byte.parseByte(data.substring(j, j + 2));
            }

            j += 2;
        }

        return result;
    }
}
