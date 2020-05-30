package com.example.daggerapplication.services.printer.util;


import android.content.Context;
import android.graphics.BitmapFactory;

import com.example.daggerapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrinterUtil {

    private final Context appContext;
    private final SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private byte[] upuLogo;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    PrinterUtil(Context appContext) {
        this.appContext = appContext;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public byte[] getUPULogo() {
        if (upuLogo == null) {
            upuLogo = BitmapUtil.decodeBitmap(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.upu_logo));
        }
        return upuLogo;
    }

    public byte[] getBarCode(String text) {
        return BitmapUtil.createBarcode(text);
    }

    public String getDateTime() {
        return dateAndTimeFormat.format(new Date());
    }

    public String getDate() {
        return dateFormat.format(new Date());
    }

    public String getTime() {
        return timeFormat.format(new Date());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}
