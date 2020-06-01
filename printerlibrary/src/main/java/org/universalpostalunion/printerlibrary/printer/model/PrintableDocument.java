package org.universalpostalunion.printerlibrary.printer.model;

import java.io.ByteArrayOutputStream;

public interface PrintableDocument {
    ByteArrayOutputStream getPrintable();
}
