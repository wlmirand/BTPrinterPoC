package org.universalpostalunion.printerlibrary.printer.builder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;
import org.universalpostalunion.printerlibrary.printer.util.BitmapUtil;
import org.universalpostalunion.printerlibrary.printer.util.PrintCommand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PrintableBuilderImpl implements PrintableBuilder {

    private final Context appContext;
    private final ByteArrayOutputStream byteArrayOutput;
    private ByteArrayOutputStream defaultStyle;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public PrintableBuilderImpl(Context appContext) throws IOException {
        this.appContext = appContext;
        byteArrayOutput = new ByteArrayOutputStream();
        initDefaultStyle();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printPhoto(int resourceId, PrintCommand alignment) throws Exception {
        final Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), resourceId);
        if (bitmap == null) {
            throw new Exception("Unable to retrieve/decode resource as a Bitmap - Resource id: " + resourceId);
        }
        final byte[] decodedBitmap = BitmapUtil.decodeBitmap(bitmap);
        if (decodedBitmap == null) {
            throw new Exception("Unable to decode Bitmap - Resource id: " + resourceId);
        }
        byteArrayOutput.write(alignment.toBytes());
        byteArrayOutput.write(decodedBitmap);
        resetStyleToDefault();
    }

    @Override
    public void printBarCodeFromText(String text, PrintCommand alignment) throws Exception {
        byteArrayOutput.write(alignment.toBytes());
        final byte[] encodeBarCode = BitmapUtil.createBarcode(text);
        if (encodeBarCode == null) {
            throw new Exception("Unable to encode text as barcode for text :" + text);
        }
        byteArrayOutput.write(encodeBarCode);
    }

    @Override
    public void print(String text, PrintCommand... style) throws IOException {
        if (style != null) {
            for (PrintCommand currentStyle : style) {
                byteArrayOutput.write(currentStyle.toBytes());
            }
            byteArrayOutput.write(text.getBytes());
        }
        resetStyleToDefault();
    }

    @Override
    public void printLine(String line, PrintCommand... style) throws IOException {
        if (style != null) {
            for (PrintCommand currentStyle : style) {
                byteArrayOutput.write(currentStyle.toBytes());
            }
            byteArrayOutput.write(line.getBytes());
            byteArrayOutput.write(PrintCommand.ESC_LINE_FEED.toBytes());
        }
        resetStyleToDefault();
    }

    @Override
    public void print(String text) throws IOException {
        byteArrayOutput.write(text.getBytes());
    }

    @Override
    public void printNewLine() throws IOException {
        byteArrayOutput.write(PrintCommand.ESC_LINE_FEED.toBytes());
    }

    @Override
    public void printLine(String line) throws IOException {
        byteArrayOutput.write(line.getBytes());
        byteArrayOutput.write(PrintCommand.ESC_LINE_FEED.toBytes());
    }

    @Override
    public void setDefaultStyle(PrintCommand... styleAndAlignment) throws IOException {
        defaultStyle.reset();
        if (styleAndAlignment != null) {
            for (PrintCommand currentStyle : styleAndAlignment) {
                defaultStyle.write(currentStyle.toBytes());
            }
        }
    }

    @Override
    public void printStyle(PrintCommand... styleAndAlignment) throws IOException {
        if (styleAndAlignment != null) {
            for (PrintCommand currentStyle : styleAndAlignment) {
                byteArrayOutput.write(currentStyle.toBytes());
            }
        }
    }

    @Override
    public void resetStyleToDefault() throws IOException {
        defaultStyle.writeTo(byteArrayOutput);
    }

    @Override
    public PrintableDocument build() throws IOException {
        printNewLine();
        printNewLine();
        printNewLine();
        printNewLine();
        resetStyleToDefault();
        return () -> byteArrayOutput;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initDefaultStyle() throws IOException {
        defaultStyle = new ByteArrayOutputStream();
        defaultStyle.write(PrintCommand.NORMAL_STYLE.toBytes());
    }

}
