package com.example.barcode.application.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    public static File getPhotoDirectory(){
        File outputDir = null;
        String externalStorageState = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(externalStorageState)){
            File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir=new File(pictureDir,"barcodeApp");
            if(!outputDir.exists()){
                if(!outputDir.mkdirs()){
                    Log.d(TAG, "getPhotoDirectory: Failed to create output directory "+outputDir.getAbsolutePath());
                outputDir=null;
                }
            }
        }
        return outputDir;
    }
    public static File generateTimestampPhotoFile(){
        File photoFile = null;
        File outputDir = getPhotoDirectory();
        if(outputDir!=null){
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String photoFileName = "BAR_IMG_"+timestamp+".jpg";
            photoFile = new File(outputDir,photoFileName);
        }
        return photoFile;
    }
}
