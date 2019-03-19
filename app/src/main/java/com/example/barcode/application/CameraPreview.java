package com.example.barcode.application;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

/**
 *
 * This is just like a custom view which is embedded into the parent layout and helps rendering the
 * camera Preview
 *
 *
 * The purpose of this class is to display the camera preview to the user while the camera is open
 *
 * Conceptually, Surface is the class which is used to draw what the camera shows as preview
 * SurfaceView is a wrapper around the surface to maintain the view hierarchy
 *
 * SurfaceHolder basically holds the Surface and basically informs the surface view
 * about when the surface is created, changed or detroyed via callbacks
 * SurfaceHolder.callback is used for this communication
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, View.OnClickListener {

    Camera mCamera;

    public SurfaceHolder getmSurfaceHolder() {
        return mSurfaceHolder;
    }

    SurfaceHolder mSurfaceHolder;
    ImageCaptureCallbackListener imageCaptureCallbackListener;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        startPreview();
    }

    public void setImageCaptureCallbackListener(ImageCaptureCallbackListener imageCaptureCallbackListener) {
        this.imageCaptureCallbackListener = imageCaptureCallbackListener;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//Stop and restart the preview
        stopPreview();
        startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        //Stop showing the preview
        stopPreview();
    }

    public void connectCamera(Camera camera,int cameraId){
        mCamera=camera;
        int previewOrientation = getCameraPreviewOrientation(cameraId);
        mCamera.setDisplayOrientation(previewOrientation);
        // display orientation aligned with the device's orientation
        mSurfaceHolder = getHolder(); // Surface View defines the camera holder
        mSurfaceHolder.addCallback(this); //Whatever events occur while working with Surface, the events are listener to by our surfaceview class

    }

    public void releaseCamera(){
        if(mCamera!=null){
            //TODO://stop preview
            stopPreview();
            mCamera=null;
        }
    }

    void startPreview(){
        // Before starting the preview , ensure that the camera is not ull
        // and SurfaceHolder has Surface onto which the preview would be drawn
        if(mCamera!=null && mSurfaceHolder.getSurface()!=null){
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder); //provide SurfaceHolder to the camera
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),R.string.cannot_start_previeww,Toast.LENGTH_LONG).show();
            }
        }
    }

    void stopPreview(){
        if(mCamera!=null){
            try {
                mCamera.stopPreview();
            }
            catch(Exception e){
                Toast.makeText(getContext(),"Error occured while stopping preview",Toast.LENGTH_SHORT).show();
            }
        }
    }
    int getDeviceOrientationDegrees() {
        int degrees = 0;
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int deviceDisplayRotation = windowManager.getDefaultDisplay().getRotation();
        switch (deviceDisplayRotation) {
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
        return degrees;
    }
    int getCameraPreviewOrientation(int cameraId){
        int temp=0;
        final int DEGREES_IN_CIRCLE=360;
        int previewOrientation=0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId,cameraInfo);
        int deviceOrientation = getDeviceOrientationDegrees();
        //Calculations differ based on whether it is front facing camera or back facing
        switch(cameraInfo.facing){
            case Camera.CameraInfo.CAMERA_FACING_BACK:
            temp = cameraInfo.orientation-deviceOrientation+DEGREES_IN_CIRCLE;
            previewOrientation=temp%DEGREES_IN_CIRCLE;
            break;
           case Camera.CameraInfo.CAMERA_FACING_FRONT:
            temp = (cameraInfo.orientation+deviceOrientation)%DEGREES_IN_CIRCLE;
            previewOrientation = (DEGREES_IN_CIRCLE-temp)%DEGREES_IN_CIRCLE ;
            break;
        }
        Toast.makeText(getContext(), String.format("Device orientation : %d and cameraInfo orientation = %d",deviceOrientation,cameraInfo.orientation), Toast.LENGTH_SHORT).show();
        return previewOrientation;

    }


    @Override
    public void onClick(View v) {
        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Toast.makeText(getContext(), "JPEG CALL BACK CALLED WITH DATA = "+data, Toast.LENGTH_SHORT).show();
           if( imageCaptureCallbackListener!=null){
               imageCaptureCallbackListener.onImageCaptured(data);
           }
            }
        };

        Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                mCamera.enableShutterSound(true);
                Toast.makeText(getContext(), "Shutterback callback called with no data", Toast.LENGTH_SHORT).show();

            }
        };

        Camera.PictureCallback rawCallback= new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Toast.makeText(getContext(), "RAW callback called with data"+data, Toast.LENGTH_SHORT).show();
            }
        };
        mCamera.takePicture(shutterCallback,rawCallback,jpegCallback);
    }
    public interface ImageCaptureCallbackListener{
        void onImageCaptured(byte[] bytes);
    }
}
