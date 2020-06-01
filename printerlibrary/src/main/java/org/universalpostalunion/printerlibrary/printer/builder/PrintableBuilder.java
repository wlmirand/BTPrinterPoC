package org.universalpostalunion.printerlibrary.printer.builder;

import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;
import org.universalpostalunion.printerlibrary.printer.util.PrintCommand;

import java.io.IOException;

public interface PrintableBuilder {

    void printPhoto(int resourceId, PrintCommand alignment) throws Exception;

    void print(String text, PrintCommand... style) throws IOException;

    void printLine(String line, PrintCommand... style) throws IOException;

    void print(String text) throws IOException;

    void printNewLine() throws IOException;

    void printLine(String line) throws IOException;

    void setDefaultStyle(PrintCommand... styleAndAlignment) throws IOException;

    void printStyle(PrintCommand... styleAndAlignment) throws IOException;

    void printBarCodeFromText(String text, PrintCommand alignment) throws Exception;

    void resetStyleToDefault() throws IOException;

    PrintableDocument build() throws IOException;

}
