package com.retinavision.app;

import android.os.Environment;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class LabelStorage {
    private JsonObject jsonObject;
    private String appFolder = "";
    public LabelStorage(String appFolder) {
        this.appFolder = appFolder;
        File folder = new File(Environment.getExternalStorageDirectory(),appFolder);
        File jsonfile = new File(folder, "labels.json");
        if(jsonfile.exists()) {
            jsonObject = null;
            try {
                jsonObject = (JsonObject) new JsonParser().parse(new FileReader(jsonfile));
            } catch (Exception e) {
                jsonObject = new JsonObject();
            }
        }else {
            jsonObject = new JsonObject();
        }
    }

    public void save(){
        File folder = new File(Environment.getExternalStorageDirectory(),appFolder);
        File jsonfile = new File(folder, "labels.json");
        try {
            PrintWriter pw = new PrintWriter(jsonfile);
            pw.write(jsonObject.toString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeImageData(String imgfilename, String label, double fix_x, double fix_y, String v){
        JsonObject entry = null;
        if(jsonObject.get(imgfilename)!=null) {
            entry = jsonObject.getAsJsonObject(imgfilename);
        }else {
            entry = new JsonObject();
            jsonObject.add(imgfilename,entry);
        }
        entry.addProperty("label",label);
        entry.addProperty("fix_x",fix_x);
        entry.addProperty("fix_y",fix_y);
        entry.addProperty("v",v);
        entry.addProperty("timestamp",System.nanoTime());
        save();
    }

    public void writeImageLabel(String imgfilename, String label) {
        JsonObject entry = null;
        if(jsonObject.get(imgfilename)!=null) {
            entry = jsonObject.getAsJsonObject(imgfilename);
        }else {
            entry = new JsonObject();
            jsonObject.add(imgfilename,entry);
        }
        entry.addProperty("label",label);
        save();
    }

    public String readImageLabel(String imgfilename) {
        if(jsonObject.get(imgfilename)==null) {
            return "NO LABEL";
        }
        JsonObject entry = jsonObject.getAsJsonObject(imgfilename);
        JsonElement labelelem = entry.get("label");
        if(labelelem==null) return "NO LABEL";
        if(labelelem.equals(new JsonNull())) return "NO LABEL";
        return labelelem.getAsString();
    }

    public double readImageFixX(String imgfilename) {
        if(jsonObject.get(imgfilename)==null) {
            return 0;
        }
        JsonObject entry = jsonObject.getAsJsonObject(imgfilename);
        JsonElement labelelem = entry.get("fix_x");
        if(labelelem==null) return 0;
        return labelelem.getAsDouble();
    }

    public double readImageFixY(String imgfilename) {
        if(jsonObject.get(imgfilename)==null) {
            return 0;
        }
        JsonObject entry = jsonObject.getAsJsonObject(imgfilename);
        JsonElement labelelem = entry.get("fix_y");
        if(labelelem==null) return 0;
        return labelelem.getAsDouble();
    }

    public String readImageV(String imgfilename) {
        if(jsonObject.get(imgfilename)==null) {
            return "";
        }
        JsonObject entry = jsonObject.getAsJsonObject(imgfilename);
        JsonElement labelelem = entry.get("v");
        if(labelelem==null) return "";
        return labelelem.getAsString();
    }


    public long readTimestamp(String imgfilename) {
        if(jsonObject.get(imgfilename)==null) {
            return 0;
        }
        JsonObject entry = jsonObject.getAsJsonObject(imgfilename);
        JsonElement labelelem = entry.get("timestamp");
        if(labelelem==null) return 0;
        return labelelem.getAsLong();
    }
}
