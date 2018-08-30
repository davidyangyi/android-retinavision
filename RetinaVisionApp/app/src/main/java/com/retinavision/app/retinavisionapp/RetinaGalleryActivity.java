package com.retinavision.app.retinavisionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.retinavision.app.AppManager;
import com.retinavision.app.ImageStorage;
import com.retinavision.app.MyApplication;

import java.io.*;
import java.util.*;

public class RetinaGalleryActivity extends BaseActivity {

    List<Map<String,Object>> data = new ArrayList<>();
    private File[] retinaFiles;
    private ListView listview;

    private static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        int h = options.outHeight;//get image height
        int w = options.outWidth;//get image width
        int scaleWidth = w / width; //calculate height zoom
        int scaleHeight = h / height; //calculate height zoon
        int scale = 1;//initial zoom
        if (scaleWidth < scaleHeight) {//choose proper zoom scale
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        if (scale <= 0) {
        }
        options.inSampleSize = scale;
        // read image, read bitmap after zoom，set inJustDecodeBounds as false
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // create thumbnail by ThumbnailUtils
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retina_gallery);


        listview = (ListView) findViewById(R.id.retinagallery_listview);
        Button btn_retinagallery_upload = (Button) findViewById(R.id.retinagallery_upload);
        Button btn_retinagallery_goback = (Button) findViewById(R.id.retinagallery_goback);
        Button btn_retinagallery_exit = (Button) findViewById(R.id.retinagallery_exit);

        btn_retinagallery_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetinaGalleryActivity.this.uploadcsv();
            }
        });

        btn_retinagallery_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetinaGalleryActivity.this.finish();
            }
        });
        btn_retinagallery_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishAllActivity();
            }
        });
    }

    private void uploadcsv() {
        File csvfile = new File(MyApplication.getImageStorage().getTempFolder(),System.nanoTime()+".csv");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(csvfile));
        } catch (IOException e) {
        }
        if (writer==null) {
            return;
        }

        int i=0;
        String list="label;vector;framenum;timestamp;fixationy;fixationx;retinatype;\n"; // fields explain
        try {
            writer.write(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File file:retinaFiles) {
            i++;
            String label = MyApplication.getLabelStorage().readImageLabel(file.getName());
            String v = MyApplication.getLabelStorage().readImageV(file.getName());
            String frameNum = String.valueOf(i);
            String timestamp = String.valueOf(MyApplication.getLabelStorage().readTimestamp(file.getName())) ;
            String fix_x = String.valueOf(MyApplication.getLabelStorage().readImageFixX(file.getName()));
            String fix_y = String.valueOf(MyApplication.getLabelStorage().readImageFixY(file.getName()));
            String retinatype = "50knode"; // current using the 50k node retina, can be changed in future

            //seperate different fields by ;
             String line = String.format("%s;%s;%s;%s;%s;%s;%s;\n", label, v, frameNum, timestamp, fix_y, fix_x, retinatype);
            try {
                writer.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri imageUri = Uri.fromFile(csvfile);
        Intent shareIntent = new Intent();
        shareIntent.setType("text/plain");
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(shareIntent, "Share csv file"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        data.clear();
        ImageStorage storage = MyApplication.getImageStorage();
        retinaFiles = storage.getCortexImageFolder().listFiles();

        MyAdapter adapter = new MyAdapter(this,Arrays.asList(retinaFiles));
        listview.setAdapter(adapter);

    }
}
