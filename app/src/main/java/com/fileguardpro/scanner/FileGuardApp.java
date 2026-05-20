package com.fileguardpro.scanner;

import android.app.Application;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class FileGuardApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PDFBoxResourceLoader.init(getApplicationContext());
    }
}
