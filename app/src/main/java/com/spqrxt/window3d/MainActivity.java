package com.spqrxt.window3d;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {
    /* TODO
    - Figure out some other ways to do smoothing / improve latency
    - Incorporate other sensor data
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};
    private static final int PERMISSIONS_REQUEST_CODE = 0;

    private TextView distance;
    private ViewCalc viewCalc;
    private MyGLSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewCalc = new ViewCalc();
        distance = findViewById(R.id.main_textView_distance);
        surfaceView = findViewById(R.id.myGLSurfaceView);

        if (havePermissions()) {
            initializeCamera();
        } else {
            showPermissionsRequestDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            showPermissionsDeniedMessage();
        }
    }

    private void initializeCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                // Set up use cases
                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setTargetResolution(new Size(480, 640))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        new Analyzer(distance, viewCalc));

                // Attach camera to this activity's lifecycle
                Camera camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        imageAnalysis);
            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.toString());
                ex.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showPermissionsRequestDialog() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }

    private void showPermissionsDeniedMessage() {
        Toast.makeText(this, R.string.permissions_denied_message, Toast.LENGTH_SHORT).show();
    }

    private boolean havePermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    public void calibrate(View view) {
        viewCalc.calibrate();
        surfaceView.attachViewCalc(viewCalc); // TODO dirty
    }
}
