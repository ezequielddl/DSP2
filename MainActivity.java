package com.example.detec_borde;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Para mensajes de falla o exito
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat gry;
    Boolean startCanny = false;
    int blurFilterSize = 5;
    int cannyThresh1 = 20;
    int cannyThresh2 = 20;
    SeekBar SeekBar1;
    SeekBar SeekBar2;
    Switch Switch1;



    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }

            });




    //Llama a OpenCV manager
    //CameraBridgeViewBase mOpenCvCameraView. This variable acts as a bridge between camera and OpenCV library.
    //BaseLoaderCallback mLoaderCallback = …..Once OpenCV library is loaded, you may want to perform some actions. For example, displaying a success or failure message.

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");  //La clase Log.i crea un mensaje de registro que se muestra en el logcat
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //Inicia Logcat
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    //Muestra la camara de OpenCV en el layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (check  == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }


        //Inicia los views
        SeekBar1=(SeekBar)findViewById(R.id.seekBar1);
        SeekBar2=(SeekBar)findViewById(R.id.seekBar2);
        Switch1= (Switch) findViewById(R.id.switch1);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.CameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        SeekBar1.setMax(50);
        SeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, "Treshold1: " + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }
        });

        SeekBar2.setMax(50);
        SeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, "Treshold2: " + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Maneja los eventos cuando la app esta en Paused, Resumed y Destroyed
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    //Recibe datos de la imagen cuando el preview se muestra en pantalla
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        gry = new Mat();
    }


    //Destruye los datos de la imagen cuando se detiene el preview en la pantalla
    public void onCameraViewStopped() {
        mRgba.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        switch (mOpenCvCameraView.getDisplay().getRotation()) {
            case Surface.ROTATION_0: // Vertical portrait
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
                Core.flip(mRgbaF, mRgba, 1);
                break;
            case Surface.ROTATION_90: // 90° anti-clockwise
                break;
            case Surface.ROTATION_180: // Vertical portrait
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
                Core.flip(mRgbaF, mRgba, 0);
                break;
            case Surface.ROTATION_270: // 90° clockwise
                Imgproc.resize(mRgba, mRgbaF, mRgbaF.size(), 0, 0, 0);
                Core.flip(mRgbaF, mRgba, -1);
                break;
            default:
        }

        cannyThresh1= SeekBar1.getProgress(); // devuelve el valor del Seekbar
        cannyThresh2= SeekBar2.getProgress();
        Boolean switchState = Switch1.isChecked();

        if (startCanny) {
            Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2GRAY);
            if(switchState) {
                Imgproc.blur(mRgba, mRgba, new Size(blurFilterSize, blurFilterSize));
            }
            Imgproc.Canny(mRgba, mRgba, cannyThresh1, cannyThresh2);
        }
        return mRgba;
    }


    public void canny(View Button){
        if (!startCanny){
            startCanny=true;

        }
        else{
            startCanny=false;
        }
    }

}



//Umbral (threshold)
//Imgproc.adaptiveThreshold(gry,mRgba,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,15,40);
//Imgproc.threshold(gry,mRgba,150,255,Imgproc.THRESH_BINARY);

//Morphology to clean up small white or black regions
//Mat kernel = Mat.ones(3, 3, CvType.CV_8U);
// Mat morph = new Mat();
//Imgproc.morphologyEx(mRgba, morph, Imgproc.MORPH_CLOSE, kernel);
//Imgproc.morphologyEx(morph, mRgba, Imgproc.MORPH_OPEN, kernel);
//Imgproc.morphologyEx(mRgba, mRgba, Imgproc.MORPH_ERODE, kernel);



//Imgproc.Canny(gry, mRgba, 30, 300);





//Mat src = inputFrame.gray(); // convertir a escala de grises
// Mat cannyEdges = new Mat();  // objeto para almacenar el resultado

// aplicar el algoritmo canny para detectar los bordes
// Imgproc.Canny(src, cannyEdges, 30, 300);

// devolver el objeto Mat procesado
//return cannyEdges;


//RGB
// @Override
// public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//     return inputFrame.rgba();


