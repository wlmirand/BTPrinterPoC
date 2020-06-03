package org.universalpostalunion.printerlibrary.printer.builder;

import android.graphics.Typeface;

import org.universalpostalunion.printerlibrary.printer.escpos.ESCPOSConstant;
import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;
import org.universalpostalunion.printerlibrary.printer.util.PrintCommand;

import java.io.IOException;
import java.nio.charset.Charset;

public interface PrintableBuilder {

    void configure(Charset charset) throws IOException;

    void setBold(boolean isActive);

    void setItalic(boolean isActive);

    void setUnderline(boolean isActive);

    void printTitle(String title) throws Exception;

    void printPhoto(int resourceId, ESCPOSConstant alignment) throws Exception;

    void print(String text, boolean wrapLines, ESCPOSConstant... style) throws IOException;

    void printNewLine(int numberOfLines) throws IOException;

    void printBarCodeFromText(String text, ESCPOSConstant alignment) throws Exception;

    PrintableDocument build() throws IOException;

}
