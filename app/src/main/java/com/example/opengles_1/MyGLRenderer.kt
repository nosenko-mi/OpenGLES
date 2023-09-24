package com.example.opengles_1

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // he renderer code is running on a separate thread from the main user interface thread
    // of your application, you must declare this public variable as volatile
    @Volatile
    var angle: Float = 0f

    private lateinit var mCube: Cube
    private lateinit var mPyramid: Pyramid

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    // tutorial start

    private val POSITION_COUNT = 3

    private var vertexData: FloatBuffer? = null
    private var indexArray: ByteBuffer? = null

    private var aPositionLocation = 0
    private var uTextureUnitLocation = 0
    private var uMatrixLocation = 0

    private var programId = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    private var texture = 0

    private val vertexShader2 = """
        attribute vec4 a_Position;
        uniform mat4 u_Matrix;
        varying vec3 v_Position;

        void main()
        {
            v_Position = a_Position.xyz;
            gl_Position = u_Matrix * a_Position;
        }
    """

    private val fragmentShader2 = """
        precision mediump float;

        uniform samplerCube u_TextureUnit;
        varying vec3 v_Position;

        void main()
        {
            gl_FragColor = textureCube(u_TextureUnit, v_Position);
        }
    """
    // tutorial end

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
//        GLES20.glClearColor(0f, 0f, 0f, 1f);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//
        mCube = Cube(context.applicationContext)
//        mPyramid = Pyramid(context.applicationContext)


        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//        GLES20.glEnable(GLES20.GL_CULL_FACE); //so you can see the diffirence
//        GLES20.glFrontFace(GLES20.GL_CCW);    //counter-clockwise
//        createAndUseProgram()
//        getLocations()
//        prepareData()
//        bindData()
        createViewMatrix()
//        Matrix.setIdentityM(mModelMatrix, 0)


    }

    override fun onDrawFrame(unused: GL10) {

        // old

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        Matrix.setIdentityM(mModelMatrix, 0)

        setModelMatrix()
        val scratch = FloatArray(16)
        mMatrix.copyInto(scratch)
        Matrix.translateM(scratch, 0, 0f, -2f, 0f)
        mCube.draw(scratch)
//        mPyramid.draw(scratch)


        // new

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray)

//        // old
//        val scratch = FloatArray(16)
//        val cubeMatrix = FloatArray(16)
////        // Redraw background color
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
////
//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//        // Calculate the projection and view transformation
//        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
//        val time = SystemClock.uptimeMillis() % 4000L
//        val angle = 0.090f * time.toInt()
//        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 1f, 0f)
//
//
//        vPMatrix.copyInto(cubeMatrix)
//        Matrix.translateM(cubeMatrix, 0, 0f, -0.2f, 0f)
//        Matrix.multiplyMM(scratch, 0, cubeMatrix, 0, mCube.mModelMatrix, 0)
//        Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrix, 0)
//

//        val scratch = FloatArray(16)
//        mMatrix.copyInto(scratch)
//        Matrix.translateM(scratch, 0, 0f, -2f, 0f)
//        mCube.draw(scratch)
////        mPyramid.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
        bindMatrix()

//        GLES20.glViewport(0, 0, width, height)
//        val ratio: Float = width.toFloat() / height.toFloat()
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 5f)
    }

    private fun prepareData() {
        val vertices = floatArrayOf( // вершины куба
            -1f, 1f, 1f,  // верхняя левая ближняя
            1f, 1f, 1f,  // верхняя правая ближняя
            -1f, -1f, 1f,  // нижняя левая ближняя
            1f, -1f, 1f,  // нижняя правая ближняя
            -1f, 1f, -1f,  // верхняя левая дальняя
            1f, 1f, -1f,  // верхняя правая дальняя
            -1f, -1f, -1f,  // нижняя левая дальняя
            1f, -1f, -1f // нижняя правая дальняя
        )
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }
        indexArray = ByteBuffer.allocateDirect(36)
            .put(
                byteArrayOf( // грани куба
                    // ближняя
                    1, 3, 0,
                    0, 3, 2,  // дальняя
                    4, 6, 5,
                    5, 6, 7,  // левая
                    0, 2, 4,
                    4, 2, 6,  // правая
                    5, 7, 1,
                    1, 7, 3,  // верхняя
                    5, 1, 4,
                    4, 1, 0,  // нижняя
                    6, 2, 7,
                    7, 2, 3
                )
            )
        indexArray!!.position(0);
        texture = TextureUtils.loadTextureCube(
            context, IntArray(6) { R.drawable.leonardo }
        )
    }

    private fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun createAndUseProgram() {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader2)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader2)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
        GLES20.glUseProgram(programId)
    }

    private fun getLocations() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit")
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix")
    }

    private fun bindData() {
        // координаты вершин
        vertexData!!.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GLES20.GL_FLOAT,
            false, 0, vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)

        // помещаем текстуру в target CUBE_MAP юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, texture)

        // юнит текстуры
        glUniform1i(uTextureUnitLocation, 0)
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
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun createViewMatrix() {
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 2f, 4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    private fun setModelMatrix() {
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        bindMatrix()
    }
}
