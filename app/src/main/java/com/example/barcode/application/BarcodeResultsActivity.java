package com.example.barcode.application;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barcode.application.utils.CameraHelper;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BarcodeResultsActivity extends AppCompatActivity {

    ImageView img;
    TextView tv,errorTv;
    byte[] imageArr;
    ViewGroup errorView,scrollview;
    ProgressDialog progressDialog;
    private static final String TAG = "BarcodeResultsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_results);
        tv= findViewById(R.id.resultTv);
        img= findViewById(R.id.img_res);
        errorView = findViewById(R.id.not_found_container);
        errorTv = findViewById(R.id.error_msg_tv);
        errorView.setVisibility(View.GONE);
        scrollview =findViewById(R.id.successPanelContainer);
        Bundle intentData = getIntent().getExtras();
        if(intentData!=null) {
            imageArr = intentData.getByteArray(MainActivity.IMAGE_DATA);
            //TODO://First store the picture in the external storage
            //TODO://After writing the data to the storage, display it to the user
            //TODO://Display the progress spinner
            //TODO://Send the bytes[] data for barcode scanning
            //TODO://Hide the progress spinner and display the results..
            //TODO://Display error message or results data based on the results
            displayImage(imageArr);
          //  displayProgressSpinner();
            getBarcodeFromImage(imageArr);
        }
        else{
           showErrorAndHideSuccessPanel(R.string.barcode_not_found);
            //Nothing available in the intent Extras
            //Toast.makeText(this, "Nothing available in intent", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // errorView.setVisibility(View.GONE);
    }

    private void displayProgressSpinner() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getString(R.string.scanning_img_progress_title));
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (imageArr != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false).setMessage(getString(R.string.save_barcode_img_msg)).setPositiveButton(R.string.save_img, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    writeImageBytesToExternalStorage(imageArr);
                }
            }).setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BarcodeResultsActivity.super.onBackPressed();
                }
            });
            alertDialogBuilder.setTitle(R.string.save_image_title).create().show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Get Barcode.rawValue from the image bytes array provided ..
     * @param imageArr
     */
    private void getBarcodeFromImage(byte[] imageArr) {
        Log.d(TAG, "startScanning: Going to Start scanning the barcode..");

        //Configure the barcode detector to scanbarcode of specific types

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS).build();

        //Create Frame from the Bitmap imageArr
        Bitmap barcodeBitMap = BitmapFactory.decodeByteArray(imageArr, 0, imageArr.length);
        Frame barcodeImageFrame = new Frame.Builder().setBitmap(barcodeBitMap).build();

        // Create SparseArray of the barcodes using barcode detector
        SparseArray<Barcode> barcodes = barcodeDetector.detect(barcodeImageFrame);

        if(barcodes.size()>0 && barcodes.valueAt(0)!=null){

            try {
                Barcode resultingBarcode = barcodes.valueAt(0);
                tv.setText(resultingBarcode.rawValue);
                scrollview.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                Log.d(TAG, "BARCODE DISPLAY DATA VALUE: " + resultingBarcode.rawValue);
            }
            catch(Exception e){
                showErrorAndHideSuccessPanel(R.string.bacode_could_not_be_detected_error_msg);
                e.printStackTrace();
            }
            finally {
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        }
        else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showErrorAndHideSuccessPanel(R.string.bacode_could_not_be_detected_error_msg);
            Toast.makeText(this, getResources().getString(R.string.bacode_could_not_be_detected_error_msg), Toast.LENGTH_SHORT).show();

        }

    }

    private void showErrorAndHideSuccessPanel(@StringRes int errorString) {
        scrollview.setVisibility(View.GONE);
        errorTv.setText(getString(errorString));

        errorView.setVisibility(View.VISIBLE);
    }

    private boolean displayImage(byte[] imageArr){
        if (imageArr != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageArr, 0, imageArr.length);
            img.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }
    private boolean writeImageBytesToExternalStorage(byte[] imageArr) {
        File photoDir= CameraHelper.generateTimestampPhotoFile();
        String msg="";
        boolean result=true;
        OutputStream fileOutputStream=null;
        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(photoDir!=null){
            try {
                fileOutputStream = new BufferedOutputStream(new FileOutputStream(photoDir));
                fileOutputStream.write(imageArr);
                //fileOutputStream.flush();
                //fileOutputStream.close();
                msg="Picture saved at " +photoDir.getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result=false;
                msg="Error storing picture to external storage.";
            } catch (IOException e) {
                e.printStackTrace();
                result=false;
                msg="Error storing picture to external storage.";

            }
            finally {
                if(fileOutputStream!=null){
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        result=false;
                    }
                    // msg="Error storing picture to external storage.";

                }
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+Environment.getExternalStorageDirectory())));
        }
        return result;
    }
}
