package com.example.daggerapplication.services.printer.model;

import java.io.ByteArrayOutputStream;

public interface PrintableDocument {
    ByteArrayOutputStream getPrintable();
}
