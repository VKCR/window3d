package com.spqrxt.window3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
    private static final float ANGLE_SCALE_FACTOR = 180.0f / 320;
    private static final float TRANSLATION_SCALE_FACTOR = 0.01f;

    private final MyGLRenderer renderer;

    private float[] previousCoords = new float[3];

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();
        setRenderer(renderer);
        // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                renderer.updateAnglesDelta(
                        (y - previousCoords[1]) * ANGLE_SCALE_FACTOR,
                        (x - previousCoords[0]) * ANGLE_SCALE_FACTOR,
                        0);
//                renderer.updatePosDelta(
//                        (x - previousCoords[0]) * TRANSLATION_SCALE_FACTOR,
//                        (y - previousCoords[1]) * TRANSLATION_SCALE_FACTOR,
//                        0);

                //requestRender();
        }

        previousCoords[0] = x;
        previousCoords[1] = y;
        return true;
    }

    public void attachViewCalc(ViewCalc viewCalc) { // TODO dirty
        renderer.attachViewCalc(viewCalc);
    }
}
