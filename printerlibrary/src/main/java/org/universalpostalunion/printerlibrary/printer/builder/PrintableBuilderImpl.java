package org.universalpostalunion.printerlibrary.printer.builder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import org.apache.commons.text.WordUtils;
import org.universalpostalunion.printerlibrary.printer.escpos.ESCPOSConstant;
import org.universalpostalunion.printerlibrary.printer.escpos.ESCPOSHelper;
import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;

import java.io.IOException;
import java.nio.charset.Charset;

public class PrintableBuilderImpl implements PrintableBuilder {

    enum SizeAndWidth {
        TITLE(25, 384, 32),
        NORMAL(25, 384, 32);

        private final int size;
        private final int widthDots;
        private final int stringPosSplit;

        SizeAndWidth(final int size, final int widthDots, final int stringPosSplit) {
            this.size = size;
            this.widthDots = widthDots;
            this.stringPosSplit = stringPosSplit;
        }
    }

    private final Context appContext;
    private final ESCPOSHelper escposHelper;

    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderlined = false;
    private Typeface defaultTypeFace = Typeface.SANS_SERIF;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public PrintableBuilderImpl(Context appContext, ESCPOSHelper escposHelper) {
        this.appContext = appContext;
        this.escposHelper = escposHelper;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void configure(Charset charset) throws IOException {
        escposHelper.initializeByteArrayOutputStream(charset);
    }

    @Override
    public void setBold(boolean isActive) {
        this.isBold = isActive;
    }

    @Override
    public void setItalic(boolean isActive) {
        this.isItalic = isActive;
    }

    @Override
    public void setUnderline(boolean isActive) {
        this.isUnderlined = isActive;
    }

    @Override
    public void printTitle(String title) throws Exception {
        escposHelper.printFromAndroidFont(defaultTypeFace,
                true,
                isItalic,
                true,
                WordUtils.wrap(title, SizeAndWidth.TITLE.stringPosSplit),
                SizeAndWidth.TITLE.widthDots,
                SizeAndWidth.TITLE.size,
                ESCPOSConstant.CMP_ALIGNMENT_CENTER.code());

    }

    @Override
    public void printPhoto(int resourceId, ESCPOSConstant alignment) throws Exception {
        final Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), resourceId);
        escposHelper.printBitmap(bitmap, alignment.code(), 350);
    }

    @Override
    public void printBarCodeFromText(String text, ESCPOSConstant alignment) throws Exception {
        escposHelper.printBarCode(text,
                ESCPOSConstant.CMP_BCS_Code128.code(),
                80,
                576,
                ESCPOSConstant.CMP_ALIGNMENT_CENTER.code(),
                ESCPOSConstant.CMP_HRI_TEXT_BELOW.code());
    }

    @Override
    public void print(String text, boolean wrapLines, ESCPOSConstant... style) throws IOException {
        ESCPOSConstant alignment = ESCPOSConstant.CMP_ALIGNMENT_LEFT;
        final String textWrapped = wrapLines ? WordUtils.wrap(text, SizeAndWidth.NORMAL.stringPosSplit) : text;
        if (style != null && style.length > 0) {
            alignment = style[0];
        }

        escposHelper.printFromAndroidFont(defaultTypeFace,
                isBold,
                isItalic,
                isUnderlined,
                textWrapped,
                SizeAndWidth.NORMAL.widthDots,
                SizeAndWidth.NORMAL.size,
                alignment.code());
    }

    @Override
    public void printNewLine(int numberOfLine) throws IOException {
        escposHelper.lineFeed(numberOfLine);
    }

    @Override
    public PrintableDocument build() throws IOException {
        printNewLine(4);
        return () -> escposHelper.getOutputStream();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}
