package com.spqrxt.window3d;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.min;
import static java.lang.StrictMath.pow;

/**
 * Takes as input sensor information, and calculates the updated viewpoint angles/positions
 */
public class ViewCalc {
    private static final double WIDTH = 480.0;
    private static final double HEIGHT = 640.0;
    private static final double CALIBRATION_DISTANCE = 2.0; // 20cm
    private static final double CALIBRATION_EYE_DISTANCE = 0.65; // 6.5cm, i.e. average inter-pupillary distance
    private static final int ROLLING_AVERAGE_COUNT = 5;
    private static final int MAX_SMOOTHING_COUNT = 10;

    private int smoothing_count;

    private double distanceRatio;
    private double eyeDistance;

    private double[] viewX = new double[ROLLING_AVERAGE_COUNT];
    private double[] viewY = new double[ROLLING_AVERAGE_COUNT];
    private double[] viewDistance = new double[ROLLING_AVERAGE_COUNT];

    ViewCalc() {}

    public void updateEyePos(
            FirebaseVisionFaceLandmark leftEye,
            FirebaseVisionFaceLandmark rightEye) {
        FirebaseVisionPoint leftPos = leftEye.getPosition();
        FirebaseVisionPoint rightPos = rightEye.getPosition();

        eyeDistance = abs(leftPos.getX() - rightPos.getX()) / WIDTH;
        smoothing_count = 0;

        /*
        The new view position in one direction is:
            - the average of the positions of the left and right eye
            - which is normalized by the dimension of the device in that direction
            - which is centered by substracting 0.5
            - which is scaled into "real life values" with the use of the eye distance calibration
         */
        double newViewX = ((leftPos.getX() + rightPos.getX()) / (2.0 * WIDTH) - 0.5) * (CALIBRATION_EYE_DISTANCE / eyeDistance);
        double newViewY = ((leftPos.getY() + rightPos.getY()) / (2.0 * HEIGHT) - 0.5) * (CALIBRATION_EYE_DISTANCE / eyeDistance);
        double newViewDistance = distanceRatio / eyeDistance;

        shift(viewX, newViewX);
        shift(viewY, newViewY);
        shift(viewDistance, newViewDistance);
    }

    public void incSmoothingCount() {
        smoothing_count = min(smoothing_count + 1, MAX_SMOOTHING_COUNT);
    }

    public double getViewX() {
        double prevViewX = avg(viewX, 0, viewX.length - 1);
        double curViewX = avg(viewX, 1, viewX.length);
        return prevViewX + smoothing_count * (curViewX - prevViewX) / MAX_SMOOTHING_COUNT;
    }

    public double getViewY() {
        double prevViewY = avg(viewY, 0, viewY.length - 1);
        double curViewY = avg(viewY, 1, viewY.length);
        return prevViewY + smoothing_count * (curViewY - prevViewY) / MAX_SMOOTHING_COUNT;
    }

    public double getViewDistance() {
        double prevViewDistance = avg(viewDistance, 0,  viewDistance.length - 1);
        double curViewDistance = avg(viewDistance, 1, viewDistance.length);
        return prevViewDistance + smoothing_count * (curViewDistance - prevViewDistance) / MAX_SMOOTHING_COUNT;
    }

    public String getFormattedViewDistance() {
        return "Distance: " + getViewDistance();
    }

    public void calibrate() {
        distanceRatio = eyeDistance * CALIBRATION_DISTANCE;
    }

    private static void shift(double[] arr, double val) {
        for (int i = 0; i < arr.length - 1; ++i) {
            arr[i] = arr[i + 1];
        }
        arr[arr.length - 1] = val;
    }

    private static double avg(double[] arr, int firstInd, int lastInd) {
        double res = 0d;
        double weights = 0;
        for (int i = firstInd; i < lastInd; ++i) {
            double weight = pow(i + 1, 2);
            res += weight * arr[i]; // aggressive averaging biased towards newer values
            weights += weight;
        }
        return res / weights;
    }
}
