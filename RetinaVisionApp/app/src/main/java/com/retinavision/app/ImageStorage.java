package com.retinavision.app;

import android.os.Environment;

import java.io.File;

public class ImageStorage {

    private String appFolder = "";
    public ImageStorage(String appFolder) {
        this.appFolder = appFolder;
    }

    public File newTempImageFile(){
        return new File(getOriginImageFolder(),System.nanoTime()+".jpg");
    }

    public File newTempImageFile(String filename){
        return new File(getOriginImageFolder(),filename);
    }
    // image taken time as the file name
    public File newOriginImageFile(){
        return newOriginImageFile(System.nanoTime()+".jpg");
    }

    public File newOriginImageFile(String filename){
        return new File(getOriginImageFolder(),filename);
    }

    public File newBackprojectImageFile(String filename) {
        return new File(getBackprojectImageFolder(),filename);
    }

    public File newCortexImageFile(String filename){
        return new File(getCortexImageFolder(),filename);
    }

    public File getTempFolder(){
        File folder = new File(getBaseFolder(), "tmp");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public File getBaseFolder(){
        File folder = new File(Environment.getExternalStorageDirectory(),appFolder);
        if (!folder.exists()) folder.mkdirs();
        return folder;
   }

    public File getOriginImageFolder(){
        File folder = new File(getBaseFolder(), "origin");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public File getBackprojectImageFolder(){
        File folder = new File(getBaseFolder(), "backproject");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public File getCortexImageFolder(){
        File folder = new File(getBaseFolder(), "cortex");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }
}
