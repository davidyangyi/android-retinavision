package com.retinavision.app.retinavisionapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.retinavision.app.AppManager;
import com.retinavision.app.MyApplication;

import java.io.File;

public class ImageViewActivity extends BaseActivity {

    private String imgfilepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        setTitle("Retina View");

        ImageView retinapic = (ImageView) findViewById(R.id.imageview_retinapic);
        ImageView backprojectpic = (ImageView) findViewById(R.id.imageview_backproject);
        Button btn_share = (Button) findViewById(R.id.imageview_share);
        Button btn_save = (Button) findViewById(R.id.imageview_save);
        Button btn_delete = (Button) findViewById(R.id.imageview_delete);
        Button btn_imageview_goback = (Button) findViewById(R.id.imageview_goback);
        Button btn_imageview_exit = (Button) findViewById(R.id.imageview_exit);


        Bundle bundle = this.getIntent().getExtras();
        imgfilepath = bundle.getString("imgfilepath");
        retinapic.setImageURI(Uri.fromFile(new File(imgfilepath)));
        backprojectpic.setImageURI(Uri.fromFile(new File(MyApplication.getImageStorage().getBackprojectImageFolder(),new File(imgfilepath).getName())));

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareButtonClick();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewActivity.this);
                AlertDialog dialog = builder.setTitle("Confirm")
                        .setMessage("Delete this file?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean r = new File(imgfilepath).delete();
                                Toast.makeText(ImageViewActivity.this,"File deleted",Toast.LENGTH_LONG).show();
                                ImageViewActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setCancelable(true)
                        .show();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewActivity.this.finish();
            }
        });
        btn_imageview_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewActivity.this.finish();
            }
        });
        btn_imageview_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishAllActivity();
            }
        });

    }

    private void onShareButtonClick() {
        Uri imageUri = Uri.fromFile(new File(imgfilepath));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share retina view image"));

    }


}
