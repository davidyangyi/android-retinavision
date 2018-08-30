package com.retinavision.app.retinavisionapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.retinavision.app.AppManager;
import com.retinavision.app.MyApplication;
import com.squareup.okhttp.*;

import java.io.*;

public class MainActivity extends BaseActivity {
    private TextView txt_main_serverstatus;
    private TextView txt_main_workingstatus;

    private final OkHttpClient client = new OkHttpClient();
    //folder name in storage
    private String appFolder = "/JstImage";
    //Url settings
    private String retinaUrl = "http://192.168.1.101:8888/retina";
    private String echoUrl = "http://192.168.1.101:8888/echo";
    //gallery
    private static final int IMAGE = 1;
    private static final int CAMERA = 2;
    private static final int SETTINGS = 3;
    private File cameraFile;

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, "comxf.activity.provider.download", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath); //read file
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //byte filesize
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("Fail to copy the file");
            e.printStackTrace();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Retina Android Demo");

        Button btn_main_takePicture = (Button) findViewById(R.id.main_takePicture);
        Button btn_main_uploadPicture = (Button) findViewById(R.id.main_uploadPicture);
        Button btn_main_gallery = (Button) findViewById(R.id.main_gallery);
        Button btn_main_settings = (Button) findViewById(R.id.main_settings);
        Button btn_main_checkserver = (Button) findViewById(R.id.main_checkserver);
        Button btn_main_goback = (Button) findViewById(R.id.main_goback);
        Button btn_main_exit = (Button) findViewById(R.id.main_exit);

        txt_main_serverstatus = (TextView) findViewById(R.id.main_serverstatus);
        txt_main_workingstatus = (TextView) findViewById(R.id.main_workingstatus);

        btn_main_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivityForResult(intent, SETTINGS);
            }
        });

        btn_main_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,CameraViewActivity.class);
                startActivity(intent);
            }
        });

        btn_main_uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });

        btn_main_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RetinaGalleryActivity.class);
                startActivity(intent);
            }
        });

        btn_main_checkserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkServerStatus();
            }
        });

        btn_main_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
        btn_main_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishAllActivity();
            }
        });
        loadSettings();
//        checkServerStatus();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the image from path
        if(requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            onUploadAlbum(imagePath);
            Log.e("xxx",imagePath);
            c.close();
        }else if(requestCode == CAMERA && resultCode == Activity.RESULT_OK ) {
            if(cameraFile==null) return;
            onUploadAlbum(cameraFile.getAbsolutePath());
            Log.e("xxx",cameraFile.getAbsolutePath());
        }else if(requestCode == SETTINGS){
            loadSettings();
        }


    }

    private void checkServerStatus() {
        txt_main_serverstatus.setText("Checking");

        final String echostr = String.valueOf(System.currentTimeMillis());
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("str",echostr)
                .build();
        try{
            Request request = new Request.Builder()
                    .url(echoUrl)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e("xxx","error");
                    Log.e("xxx",e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_main_serverstatus.setText("Offline");
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if(response.isSuccessful()){
                        if(response.code()==200){
                            String str = new String(response.body().bytes());
                            if(echostr.equals(str)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txt_main_serverstatus.setText("Online");
                                    }
                                });
                            }
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_main_serverstatus.setText("Offline");
                                }
                            });
                        }
                    }
                }
            });
        }catch (Exception e){}
    }

    private void loadSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        String serviceurl = sharedPreferences.getString("serviceurl", "");
        String echourl = sharedPreferences.getString("echourl", "");
        this.retinaUrl = serviceurl;
        this.echoUrl = echourl;
    }

    private void onUploadAlbum(final String imgFilepath){
        Toast.makeText(this,"Waiting for server reply!",Toast.LENGTH_SHORT).show();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), new File(imgFilepath));
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file",new File(imgFilepath).getName(),fileBody)
                .build();
        Request request = new Request.Builder()
                .url(retinaUrl)
                .post(requestBody)
                .build();
        txt_main_workingstatus.setText("Working");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("xxx","error");
                Log.e("xxx",e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt_main_workingstatus.setText("Error");
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.e("xxx",String.valueOf(response.code()));
                    if(response.code()==200){
                        InputStream stream = response.body().byteStream();
                        String fname = System.nanoTime()+".jpg";
                        final File tarfile = new File(MyApplication.getImageStorage().getCortexImageFolder(),fname);
                        copyFile(imgFilepath,new File(MyApplication.getImageStorage().getOriginImageFolder(),fname).getAbsolutePath());


                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(new InputStreamReader(stream));
                        String bimgbase64str = jsonObject.get("bimg").getAsString();
                        String cimgbase64str = jsonObject.get("cimg").getAsString();
                        double fix_x = jsonObject.get("fix_x").getAsDouble();
                        double fix_y = jsonObject.get("fix_y").getAsDouble();
                        String V = jsonObject.get("V").getAsString();

                        byte[] bimgdata = Base64.decode(bimgbase64str.getBytes(), Base64.NO_PADDING);
                        byte[] cimgdata = Base64.decode(cimgbase64str.getBytes(), Base64.NO_PADDING);

                        final File cortexfile = new File(MyApplication.getImageStorage().getCortexImageFolder(),tarfile.getName());
                        final File backprojectfile = new File(MyApplication.getImageStorage().getBackprojectImageFolder(),tarfile.getName());
                        FileOutputStream fout1 = new FileOutputStream(cortexfile);
                        fout1.write(cimgdata);
                        fout1.flush();
                        fout1.close();

                        FileOutputStream fout2 = new FileOutputStream(backprojectfile);
                        fout2.write(bimgdata);
                        fout2.flush();
                        fout2.close();

                        MyApplication.getLabelStorage().writeImageData(tarfile.getName(),"",fix_x,fix_y,V);
                        stream.close();


                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this,ImageViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imgfilepath",tarfile.getAbsolutePath());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_main_workingstatus.setText("Idle");
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_main_workingstatus.setText("Error");
                            }
                        });
                    }
                }
            }
        });

    }

}
