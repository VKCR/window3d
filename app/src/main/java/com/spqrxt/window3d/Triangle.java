package com.spqrxt.window3d;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// Create three triangles at different positions
public class Triangle {
    // shader code
    private final int mProgram;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 vColor;" +
            "varying vec4 int_Color;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  int_Color = vColor;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 int_Color;" +
            "void main() {" +
            "  gl_FragColor = int_Color;" +
            "}";

    // scaling all values by a factor 3 to get bigger triangles
    // defining 3 triangles at different depths, slightly offset from each other on the X axis
    static float triangleCoords[] = {   // in counterclockwise order:
            3*0.5f,  3*0.622008459f, 3*2.0f, // top
            3*0.0f, -3*0.311004243f, 3*2.0f, // bottom left
            3*1.0f, -3*0.311004243f, 3*2.0f,  // bottom right
            -3*0.5f,  3*0.622008459f, 3*1.0f, // top
            -3*1.0f, -3*0.311004243f, 3*1.0f, // bottom left
            3*0.0f, -3*0.311004243f, 3*1.0f,  // bottom right
            3*0.0f,  3*0.622008459f, 0.0f, // top
            -3*0.5f, -3*0.311004243f, 0.0f, // bottom left
            3*0.5f, -3*0.311004243f, 0.0f  // bottom right
    };
    static final int COORDS_PER_VERTEX = 3;
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private FloatBuffer vertexBuffer;
    private int positionHandle;

    // defining the colors for the 3 triangles
    float colorCoords[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f };
    static final int COLORS_PER_VERTEX = 4;
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per vertex
    private FloatBuffer colorBuffer;
    private int colorHandle;

    // handle for the model view projection matrix
    private int vPMatrixHandle;

    public Triangle() {
        // load shaders
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        // initialize vertex buffer
        ByteBuffer vertexBB = ByteBuffer.allocateDirect(
                triangleCoords.length * 4);
        vertexBB.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexBB.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        // do the same for color
        ByteBuffer colorBB = ByteBuffer.allocateDirect(
                colorCoords.length * 4);
        colorBB.order(ByteOrder.nativeOrder());
        colorBuffer = colorBB.asFloatBuffer();
        colorBuffer.put(colorCoords);
        colorBuffer.position(0);
    }

    public void draw(float[] vPMatrix) {
        // load program
        GLES20.glUseProgram(mProgram);

        // prepare vertex positions
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // prepare vertex colors
        colorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                colorStride, colorBuffer);

        // prepare model view projection matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0);

        // draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
