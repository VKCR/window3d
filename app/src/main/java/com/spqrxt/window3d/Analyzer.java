package com.spqrxt.window3d;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.experimental.UseExperimental;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

public class Analyzer implements ImageAnalysis.Analyzer {
    private static final String LOG_TAG = Analyzer.class.getSimpleName();
    private final FirebaseVisionFaceDetector detector;
    private final TextView distance;
    private final ViewCalc viewCalc;

    Analyzer(TextView distance, ViewCalc viewCalc) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        //.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        this.distance = distance;
        this.viewCalc = viewCalc;
    }

    @Override
    @UseExperimental(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            return;
        }
        int rotationDegrees = degreesToFirebaseRotation(
                imageProxy.getImageInfo().getRotationDegrees());
        FirebaseVisionImage im = FirebaseVisionImage.fromMediaImage(
                imageProxy.getImage(),
                rotationDegrees);

        detector.detectInImage(im)
                .addOnSuccessListener(faces -> {
                    if (faces.size() > 0) {
                        FirebaseVisionFace face = faces.get(0);
                        FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                        FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);

                        if (leftEye != null && rightEye != null) {
                            viewCalc.updateEyePos(leftEye, rightEye);
                            distance.setText(viewCalc.getFormattedViewDistance());
                        }
                    } else {
                        distance.setText("0");
                    }
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, e.toString());
                    e.printStackTrace();
                    imageProxy.close();
                });
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }
}
