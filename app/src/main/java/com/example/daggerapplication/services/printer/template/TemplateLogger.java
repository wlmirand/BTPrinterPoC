package com.example.daggerapplication.services.printer.template;

import android.util.Log;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class TemplateLogger implements LogChute {
    private final static String LOG_TAG = "Printing Template";

    @Override
    public void init(RuntimeServices runtimeServices) {
    }

    @Override
    public boolean isLevelEnabled(int level) {
        return level < LogChute.DEBUG_ID;
    }

    @Override
    public void log(int level, String msg) {
        switch (level) {
            case LogChute.TRACE_ID:
            case LogChute.DEBUG_ID:
                Log.d(LOG_TAG, msg);
                break;
            case LogChute.ERROR_ID:
                Log.e(LOG_TAG, msg);
                break;
            case LogChute.INFO_ID:
                Log.i(LOG_TAG, msg);
                break;
            case LogChute.WARN_ID:
                Log.w(LOG_TAG, msg);
        }
    }

    @Override
    public void log(int level, String msg, Throwable t) {
        switch (level) {
            case LogChute.TRACE_ID:
            case LogChute.DEBUG_ID:
                Log.d(LOG_TAG, msg, t);
                break;
            case LogChute.ERROR_ID:
                Log.e(LOG_TAG, msg, t);
                break;
            case LogChute.INFO_ID:
                Log.i(LOG_TAG, msg, t);
                break;
            case LogChute.WARN_ID:
                Log.w(LOG_TAG, msg, t);
        }
    }
}
