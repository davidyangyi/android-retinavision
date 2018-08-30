package com.retinavision.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {
    private static Context mContext;
    private static LabelStorage labelStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }

    public static ImageStorage getImageStorage(){
        return new ImageStorage("RetinaVisionApp");
    }

    public static LabelStorage getLabelStorage() {
        if(labelStorage!=null) return labelStorage;
        labelStorage = new LabelStorage("RetinaVisionApp");
        return labelStorage;
    }

    public static String getRetinaServiceUrl(){
        SharedPreferences sharedPreferences = getInstance().getSharedPreferences("default", Context.MODE_PRIVATE);
        String serviceurl = sharedPreferences.getString("serviceurl", "");
        String echourl = sharedPreferences.getString("echourl", "");
        return serviceurl;
    }

    public static String getEchoServiceUrl(){
        SharedPreferences sharedPreferences = getInstance().getSharedPreferences("default", Context.MODE_PRIVATE);
        String serviceurl = sharedPreferences.getString("serviceurl", "");
        String echourl = sharedPreferences.getString("echourl", "");
        return echourl;
    }
}
