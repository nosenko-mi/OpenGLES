package com.example.opengles_1

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // he renderer code is running on a separate thread from the main user interface thread
    // of your application, you must declare this public variable as volatile
    @Volatile
    var angle: Float = 0f

    private lateinit var mCube: Cube
    private lateinit var mPyramid: Pyramid2

    private val rotationMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        mPyramid = Pyramid2(context.applicationContext)
//        mCube = Cube(context.applicationContext)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//        by default each side of triangle has a texture, next flags make it so only visible part has it
//        GLES20.glEnable(GLES20.GL_CULL_FACE)
//        depending on draw order it is possible to change it (clockwise or counterclockwise)
//        GLES20.glFrontFace(GLES20.GL_CCW)

        createViewMatrix()
    }

    override fun onDrawFrame(unused: GL10) {
        val cubeScratch = FloatArray(16)
        val pyramidScratch = FloatArray(16)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        calculateProjection()
        setRotationMatrix()

        mvpMatrix.copyInto(cubeScratch)
        mvpMatrix.copyInto(pyramidScratch)

//         somehow objects are connected, unexpected behaviour when rendering both

//        Matrix.translateM(cubeScratch, 0, 0f, -0.8f, 0f)
//        Matrix.multiplyMM(cubeScratch, 0, cubeScratch, 0, mCube.mModelMatrix, 0)
//        Matrix.multiplyMM(cubeScratch, 0, cubeScratch, 0, rotationMatrix, 0)
//        mCube.draw(cubeScratch)

        Matrix.translateM(pyramidScratch, 0, 0f, 0.9f, 0f)
        Matrix.multiplyMM(pyramidScratch, 0, pyramidScratch, 0, mPyramid.mModelMatrix, 0)
        Matrix.multiplyMM(pyramidScratch, 0, pyramidScratch, 0, rotationMatrix, 0)

        mPyramid.draw(pyramidScratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
    }

    private fun createProjectionMatrix(width: Int, height: Int) {
        var ratio = 1f
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 12f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun createViewMatrix() {
        Matrix.setLookAtM(viewMatrix, 0, 0f, 2f, 4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }

    private fun calculateProjection(){
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }
    private fun setRotationMatrix() {
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, -1f, 0f)
        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, rotationMatrix, 0)
    }

}
