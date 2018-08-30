package com.retinavision.app.retinavisionapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.retinavision.app.AppManager;
import com.retinavision.app.MyApplication;
import com.squareup.okhttp.*;

import java.io.*;
import java.util.List;

public class CameraViewActivity extends BaseActivity implements SurfaceHolder.Callback {
    private final OkHttpClient client = new OkHttpClient();
    private String label;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private TextView cameraview_input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        SurfaceView mView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mView.getHolder();
        mHolder.addCallback(this);
       // print input
        cameraview_input = (TextView)findViewById(R.id.cameraview_input);

        ImageButton btn_cameraview_takepic = (ImageButton) findViewById(R.id.cameraview_takepic);
        Button btn_cameraview_setlabel = (Button) findViewById(R.id.cameraview_setlabel);
        Button btn_cameraview_goback = (Button) findViewById(R.id.cameraview_goback);
        Button btn_cameraview_exit = (Button) findViewById(R.id.cameraview_exit);

        btn_cameraview_setlabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraViewActivity.this.onSetLabelClick();
            }
        });
        btn_cameraview_takepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraViewActivity.this.onTakePictureClick();
            }
        });
        btn_cameraview_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraViewActivity.this.finish();
            }
        });
        btn_cameraview_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishAllActivity();
            }
        });
    }

    private void onTakePictureClick(){
        if (mCamera != null) {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mCamera.cancelAutoFocus(); //close the autofocus function
                    Camera.Parameters mCameraParameters = mCamera.getParameters();
                    //resume the autofocus function
                    mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    mCameraParameters.setFocusAreas(null);
                    mCamera.setParameters(mCameraParameters);
                    //
                    mCamera.startPreview();

                    byte[] tempData = data;

                    if (tempData != null && tempData.length > 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(tempData, 0, tempData.length);
                        bitmap = adjustPhotoRotation(bitmap,90);
                        try {
                            File cameraFile = MyApplication.getImageStorage().newOriginImageFile();
                            FileOutputStream fout = new FileOutputStream(cameraFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fout);
                            fout.flush();
                            fout.close();
                            onUploadAlbum(cameraFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }

    private void onSetLabelClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the label");
        final EditText input = new EditText(this);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String l = input.getText().toString();
                setLabel(l);
                cameraview_input.setText(getLabel());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        refreshCamera();
    }

    private void onUploadAlbum(final File imgFile){
        byte[] data = new byte[]{-2,-3,-4,-5};
        String s = Base64.encodeToString(data, Base64.NO_WRAP);
        byte[] dd = Base64.decode(s, Base64.NO_WRAP);


        final ProgressDialog dlg = ProgressDialog.show(CameraViewActivity.this, "Hint", "Waiting for server reply");
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), imgFile);
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file",imgFile.getName(),fileBody)
                .build();
        Request request = new Request.Builder()
                .url(MyApplication.getRetinaServiceUrl())
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
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.e("xxx",String.valueOf(response.code()));
                    if(response.code()==200){
                        InputStream stream = response.body().byteStream();
                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(new InputStreamReader(stream));
                        String bimgbase64str = jsonObject.get("bimg").getAsString();
                        String cimgbase64str = jsonObject.get("cimg").getAsString();
                        double fix_x = jsonObject.get("fix_x").getAsDouble();
                        double fix_y = jsonObject.get("fix_y").getAsDouble();
                        String V = jsonObject.get("V").getAsString();

                        byte[] bimgdata = Base64.decode(bimgbase64str.getBytes(), Base64.NO_PADDING);
                        byte[] cimgdata = Base64.decode(cimgbase64str.getBytes(), Base64.NO_PADDING);

                        final File cortexfile = new File(MyApplication.getImageStorage().getCortexImageFolder(),imgFile.getName());
                        final File backprojectfile = new File(MyApplication.getImageStorage().getBackprojectImageFolder(),imgFile.getName());
                        FileOutputStream fout1 = new FileOutputStream(cortexfile);
                        fout1.write(cimgdata);
                        fout1.flush();
                        fout1.close();

                        FileOutputStream fout2 = new FileOutputStream(backprojectfile);
                        fout2.write(bimgdata);
                        fout2.flush();
                        fout2.close();
                        MyApplication.getLabelStorage().writeImageData(imgFile.getName(),label,fix_x,fix_y,V);
                        stream.close();

                        Intent intent = new Intent();
                        intent.setClass(CameraViewActivity.this,ImageViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imgfilepath",cortexfile.getAbsolutePath());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dlg.dismiss();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dlg.dismiss();
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera==null) return;
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHolder.addCallback(this);
        mCamera = getCameraInstance();
        try {
            mCamera.setPreviewDisplay(mHolder);
            initCameraParameters();
            mCamera.startPreview();
        } catch(IOException e) {
            Log.e("xxx", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        try {
            mCamera.setPreviewDisplay(mHolder);
            initCameraParameters();
            mCamera.startPreview();
        } catch(IOException e) {
            Log.e("xxx", "Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera(); // always refresh camera for new actions
        int rotation = getDisplayOrientation(); //phone rotate as display
        mCamera.setDisplayOrientation(90);
//        mCamera.setpreviewor
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        if(mCamera==null) return;
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch(Exception e){
            Log.e("xxx", "camera is not available");
        }
        return c;
    }
    private void initCameraParameters(){

        Camera.Parameters mCameraParameters = mCamera.getParameters();

        List<Camera.Size> pictureSizes = mCameraParameters.getSupportedPictureSizes();
        int maxwidth = 0;
        Camera.Size bestsize = null;
        for (Camera.Size pictureSize : pictureSizes){
            if(maxwidth<pictureSize.width){
                maxwidth = pictureSize.width;
                bestsize = pictureSize;
            }
        }
        if (bestsize!=null){
            mCameraParameters.setPictureSize(bestsize.width,bestsize.height);
        }

        mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCameraParameters.setFocusAreas(null);
        mCamera.setParameters(mCameraParameters);
    }
    private int getDisplayOrientation(){
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }
    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);


        return bm1;
    }
    private void refreshCamera(){
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch(Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            initCameraParameters();
            mCamera.startPreview();
        } catch (Exception e) {

        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }



}
