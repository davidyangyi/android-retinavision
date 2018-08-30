package com.retinavision.app.retinavisionapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.retinavision.app.MyApplication;

import java.io.File;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    private final Context context;
    private LayoutInflater mInflater;
    private List<File> mDatas;

    //create context to get the Layout.inflater，load item from inflater
    public MyAdapter(Context context, List<File> datas) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    //retrun data length
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        int h = options.outHeight;//get image height
        int w = options.outWidth;//get image weight
        int scaleWidth = w / width; //calculate zoom in width
        int scaleHeight = h / height; //calculate zoon in height
        int scale = 1;//initial zoom
        if (scaleWidth < scaleHeight) {//choose proper zoom scale
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        if (scale <= 0) {
        }
        options.inSampleSize = scale;
        // read image, read bitmap after zoom, set inJustDecodeBounds as false
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // create thumbnail by ThumbnailUtils
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_retinagallery, parent, false); //load layout
            holder = new ViewHolder();

            holder.label = (TextView) convertView.findViewById(R.id.listitem_retinagallery_label);
            holder.cortexImage = (ImageView) convertView.findViewById(R.id.listitem_retinagallery_retina);
            holder.backProjectImage = (ImageView) convertView.findViewById(R.id.listitem_retinagallery_origin);
            holder.setLabelButton = (Button) convertView.findViewById(R.id.listitem_retinagallery_setlabel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final File imgfile = mDatas.get(position);
        File bimgfile = new File(MyApplication.getImageStorage().getBackprojectImageFolder(), imgfile.getName());
        final File cimgfile = new File(MyApplication.getImageStorage().getCortexImageFolder(), imgfile.getName());
        String labelname = MyApplication.getLabelStorage().readImageLabel(imgfile.getName());
        if(labelname==null||"".equals(labelname)) labelname = "NO LABEL";
        holder.label.setText(labelname);
        holder.backProjectImage.setImageBitmap(getImageThumbnail(bimgfile.getAbsolutePath(),1024,768 ));
        holder.cortexImage.setImageBitmap(getImageThumbnail(cimgfile.getAbsolutePath(),1024,768 ));
        final ViewHolder finalHolder = holder;
        holder.setLabelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetLabelClick(finalHolder.label,imgfile);
            }
        });

        holder.cortexImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAdapter.this.onImgClick(cimgfile);
            }
        });

        holder.backProjectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAdapter.this.onImgClick(cimgfile);
            }
        });

        return convertView;
    }

    private void onImgClick(File cimgfile){
        Intent intent = new Intent();
        intent.setClass(context,ImageViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imgfilepath",cimgfile.getAbsolutePath());
        intent.putExtras(bundle);
        this.context.startActivity(intent);
    }

    private void onSetLabelClick(final TextView label, final File imgfile){
        Context context = label.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter the label");
        final EditText input = new EditText(context);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String l = input.getText().toString();
                MyApplication.getLabelStorage().writeImageLabel(imgfile.getName(),l);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class ViewHolder {
        ImageView cortexImage;
        ImageView backProjectImage;
        Button setLabelButton;
        TextView label;
    }

}