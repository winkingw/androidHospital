package com.serenehealth;

import android.app.Application;

import com.serenehealth.util.SPUtil;

public class SereneHealthApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SPUtil.init(this);
    }
}
