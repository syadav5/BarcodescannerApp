package com.example.barcode.application;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraPreview.ImageCaptureCallbackListener{

    private static final String TAG = "MainActivity";
    private static final int CAMERA_ID_NOT_SET = -1;
    private static final String SELECTED_CAMERA_ID = "SELECTED_CAMERA_ID";
    public static final String IMAGE_DATA = "IMAGE_DATA";
    int _frontFacingCameraId = CAMERA_ID_NOT_SET;
    int _backFacingCameraId = CAMERA_ID_NOT_SET;
    boolean _hasCamera=false;
    boolean _hasFrontCamera=false;
    int _selectedCameraId = CAMERA_ID_NOT_SET;
    Camera _selectedCamera;
    CameraPreview mCameraView;
    ProgressBar mProgressBar;
    Camera.Parameters mCameraParameters;
    Camera.Size mCameraSize;
    List<Camera.Size> listOfFilesSupported;
    Button imageCaptureButton,zoomButton;
    private List<Camera.Size> mSupportedPictureSizes;
    private Camera.Size selectedPictureSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.cameraPreview);
        mProgressBar=findViewById(R.id.spinner);
        imageCaptureButton = findViewById(R.id.captureBtn);
        zoomButton=findViewById(R.id.zoomBtn);
        PackageManager packageManager =  getPackageManager();
        _hasCamera =  packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        _hasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        if(!_hasCamera){
            showNoCameraDialog();
        }
        if(savedInstanceState!=null)
            _selectedCameraId = savedInstanceState.getInt(SELECTED_CAMERA_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openSelectedCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if(_selectedCameraId!=CAMERA_ID_NOT_SET){
            outState.putInt(SELECTED_CAMERA_ID,_selectedCameraId);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    /**
     * This method returns the camera id for the camera of interest such as backfacing or front facing ..
     * @param id
     * @return
     */
    int getCameraIdForCameraNeeded(int id){
        int cameraId = CAMERA_ID_NOT_SET;
        int nCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i=0;i<nCameras;i++){
            // Iterating over Camera info, getting CameraInfo for each Camera
            // While iterating, we'll select the camera with cameraId same as the one passed to the method
          //  CameraInfo class has attribute called facing which determines whether it is front facing camera or back facing camera
            Camera.getCameraInfo(i,cameraInfo); // populates the camera info object with the information attributes
            if(cameraInfo.facing == id){
                // found the required camera
                cameraId=i; // saves the id of the camera we are looking for
                break;
            }
            // At this point, we have found the cameraId for camera
        }
        return cameraId;
    }

    /***
     * This method returns the back facing camera
     * @return
     */

    int getBackFacingCameraId(){
        if(_selectedCameraId==CAMERA_ID_NOT_SET)
        {
            _backFacingCameraId=getCameraIdForCameraNeeded(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        return _backFacingCameraId;
    }

    /**
     *
     * @return
     */
    int getFrontFacingCameraId(){
        if(_selectedCameraId==CAMERA_ID_NOT_SET)
        {
            _frontFacingCameraId=getCameraIdForCameraNeeded(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        return _backFacingCameraId;
    }

    void openSelectedCamera(){
        String message="";
        releaseSelectedCamera();
        // If condition to set selectedCameraId only if it is not pre selected..this is case with onSaveInstanceState
        if(this._selectedCameraId==CAMERA_ID_NOT_SET) {
            this._selectedCameraId = getBackFacingCameraId();
        }

        if(_selectedCameraId != CAMERA_ID_NOT_SET){
            try{
                _selectedCamera = Camera.open(_selectedCameraId);
                mCameraView.connectCamera(_selectedCamera,_selectedCameraId);
                mCameraParameters = _selectedCamera.getParameters();
                mSupportedPictureSizes = mCameraParameters.getSupportedPictureSizes();
                if(selectedPictureSize !=null){
                    selectedPictureSize = mCameraParameters.getPictureSize();
                }
                _selectedCamera.setPreviewDisplay(mCameraView.mSurfaceHolder);
                //message=String.format("Opened Camera with id : %d",_selectedCameraId);
            }
            catch(Exception e){
                e.printStackTrace();
                message="Error while opening the camera. please try again later...";
            }
            Log.d(TAG, message);
            //Toast.makeText(this,message,Toast.LENGTH_LONG).show();
        }
    }

    void releaseSelectedCamera(){
        if(this._selectedCamera!=null){
            mCameraView=findViewById(R.id.cameraPreview);
            mCameraView.releaseCamera();
            this._selectedCamera.release();
            this._selectedCamera=null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        if(!_hasCamera){
            disableCameraMenuItem(menu);
        }
        if(!_hasFrontCamera){
            disableFrontCameraMenuItem(menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void openBackCamera(MenuItem item) {
        logMenuChoice(item);

    }

    private void logMenuChoice(MenuItem item) {
    }

    public void openFrontCamera(MenuItem item) {
    }

    private void showNoCameraDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Camera");
        builder.setMessage("Device does not have required camera support. Some features may not be available.");
        builder.setCancelable(true);
        builder.show();
    }
    private void disableCameraMenuItem(Menu menu){
        menu.findItem(R.id.back_camera).setEnabled(false);
        menu.findItem(R.id.front_camera).setEnabled(false);
        imageCaptureButton.setEnabled(false);
    }

    private void disableFrontCameraMenuItem(Menu menu){
        menu.findItem(R.id.back_camera).setEnabled(true);
        menu.findItem(R.id.front_camera).setEnabled(false);
    }

    @Override
    protected void onPause() {
        releaseSelectedCamera();
        super.onPause();
    }


    @Override
    public void onImageCaptured(byte[] bytes) {
                     // mProgressBar.setVisibility(View.VISIBLE);
                       Intent intent = new Intent(this,BarcodeResultsActivity.class);
                       intent.putExtra(IMAGE_DATA,bytes);
                       startActivity(intent);
    }

    public void capturePhoto(View view) {
        _selectedCamera.takePicture(null,null,new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onPictureJpeg(data,camera);
            }
        });
    }

    public void takePhoto(MenuItem view) {
        _selectedCamera.takePicture(null,null,new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onPictureJpeg(data,camera);
            }
        });
    }

    private void onPictureJpeg(byte[] data, Camera camera) {
        Toast.makeText(this, "Picture Taken", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onPictureJpeg: Image Bytes "+data);
        //Once the picture bytes data is available, go to next activtiy to display the image and simultaneously
        // forward the bytes to Barcode API for barcode scanning..
        Intent intent = new Intent(this,BarcodeResultsActivity.class);
        intent.putExtra(IMAGE_DATA,data);
        startActivity(intent);

    }
}
