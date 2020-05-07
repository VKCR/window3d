package com.spqrxt.window3d;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Triangle triangle;
    private ViewCalc viewCalc;

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private float[] rotX = new float[16];
    private float[] rotY = new float[16];
    private float[] rotZ = new float[16];

    private float posZoffset = -1f; // since the calibration distance is -2 and initial distance is -3
    private volatile float[] pos =  { 0f, 0f, -3f };
    private volatile float[] angles = { 0f, 0f, 0f };

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        triangle = new Triangle();
    }

    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (viewCalc != null) {
            viewCalc.incSmoothingCount();
            pos[0] = (float)viewCalc.getViewX();
            pos[1] = -(float)viewCalc.getViewY();
            pos[2] = posZoffset - (float)viewCalc.getViewDistance();
        }

        Matrix.setLookAtM(viewMatrix, 0, pos[0], pos[1], pos[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setRotateM(rotX, 0, -angles[0], 1.0f, 0f, 0f);
        Matrix.setRotateM(rotY, 0, angles[1], 0f, 1.0f, 0f);
        Matrix.setRotateM(rotZ, 0, angles[2], 0f, 0f, 1.0f);

        float[] rotXY = new float[16];
        float[] rotXYZ = new float[16];
        Matrix.multiplyMM(rotXY, 0, rotY, 0, rotX, 0);
        Matrix.multiplyMM(rotXYZ, 0, rotZ, 0, rotXY, 0);

        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotXYZ, 0);

        triangle.draw(scratch);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 2, 50);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void updateAnglesDelta(float x, float y, float z) {
        angles[0] += x;
        angles[1] += y;
        angles[2] += z;
    }

    public void updatePosDelta(float x, float y, float z) {
        pos[0] += x;
        pos[1] += y;
        pos[2] += z;
    }

    public void attachViewCalc(ViewCalc viewCalc) { // TODO dirty
        this.viewCalc = viewCalc;
    }
}
