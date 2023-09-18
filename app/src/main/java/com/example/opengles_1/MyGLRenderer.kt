package com.example.opengles_1

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // he renderer code is running on a separate thread from the main user interface thread
    // of your application, you must declare this public variable as volatile
    @Volatile
    var angle: Float = 0f

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)

        val squareTextureBitmap = BitmapFactory.decodeStream(context.assets.open("models/leonardo.png"))

        // initialize a square
        mSquare = Square()
        mSquare.setTexture(squareTextureBitmap)

    }

    override fun onDrawFrame(unused: GL10) {
        val scratch = FloatArray(16)
        val triangleMatrix = FloatArray(16)
        val squareMatrix = FloatArray(16)
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Square

        vPMatrix.copyInto(squareMatrix)
        Matrix.translateM(squareMatrix, 0, 0f, -0.8f, 0f)
        Matrix.multiplyMM(scratch, 0, squareMatrix, 0, mSquare.mModelMatrix, 0)

        mSquare.draw(scratch)

    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

//        // this projection matrix is applied to object coordinates
//        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 5f)
    }
}
