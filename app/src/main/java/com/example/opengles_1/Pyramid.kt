package com.example.opengles_1

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// uses the same approach as cube -> wrong textures
class Pyramid(
    private val context: Context
) {

    private val TAG = this::class.simpleName

    private val POSITION_COUNT = 3
    private val INDICES_COUNT = 18

    private var vertexData: FloatBuffer? = null
    private var indexArray: ByteBuffer? = null

    private var aPositionLocation = 0
    private var uTextureUnitLocation = 0
    private var uMatrixLocation = 0

    private var programId = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    var mModelMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    private var texture = 0

    private val vertexShader2 = """
        attribute vec4 a_PositionPyramid;
        uniform mat4 u_MatrixPyramid;
        varying vec3 v_PositionPyramid;
        
//        attribute vec2 a_TextureCoordinates; // New attribute for texture coordinates
//        varying vec2 v_TextureCoordinates; // Pass texture coordinates to fragment shader
        

        void main()
        {
//            v_TextureCoordinates = a_TextureCoordinates;
            v_PositionPyramid = a_PositionPyramid.xyz;
            gl_Position = u_MatrixPyramid * a_PositionPyramid;
        }
    """

    private val fragmentShader2 = """
        precision mediump float;

        uniform sampler2D u_TextureUnitPyramid;
        varying vec3 v_PositionPyramid;
        
//        varying vec2 v_TextureCoordinates; // Receive texture coordinates from vertex shader
        
        void main()
        {
            gl_FragColor = texture2D(u_TextureUnitPyramid, v_PositionPyramid.xz);
        }
    """

    init {
        createAndUseProgram()
        getLocations()
        prepareData()
        bindData()
//        createViewMatrix()
        Matrix.setIdentityM(mModelMatrix, 0)
        Log.d("Pyramid", "programId: $programId")
    }

    fun draw(mvpMatrix: FloatArray) {
//        GLES20.glEnableVertexAttribArray(aPositionLocation)

        GLES20.glUseProgram(programId)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mvpMatrix, 0)

//        Matrix.setIdentityM(mvpMatrix, 0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            INDICES_COUNT,
            GLES20.GL_UNSIGNED_BYTE,
            indexArray
        )
        handleError("glDrawElements")
    }

    private fun prepareData() {
        val vertices = floatArrayOf(
            -1f, -1f, 1f, // 0 Bottom-left
            1f, -1f, 1f, // 1 Bottom-right
            1f, -1f, -1f, // 2 Top-right
            -1f, -1f, -1f, // 3 Top-left

            // Apex
            0.0f, 1f, 0.0f  // 4 Apex
        )
        vertexData = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }
        indexArray = ByteBuffer.allocateDirect(INDICES_COUNT)
            .put(
                byteArrayOf(
                    // Base
                    0, 1, 3,
                    3, 1, 2,

                    // Side 1
                    0, 1, 4,

                    // Side 2
                    1, 2, 4,

                    // Side 3
                    2, 3, 4,

                    // Side 4
                    3, 0, 4
                )
            )
        indexArray!!.position(0)
        texture = TextureUtils.loadTexturePyramid(
            context,
            intArrayOf(R.drawable.brick_wall)
        )
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
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
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_PositionPyramid")
        uTextureUnitLocation = GLES20.glGetUniformLocation(programId, "u_TextureUnitPyramid")
        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_MatrixPyramid")
    }

    private fun bindData() {
        // координаты вершин
        vertexData!!.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            POSITION_COUNT,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexData
        )
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        // помещаем текстуру в target CUBE_MAP юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)

        // юнит текстуры
        GLES20.glUniform1i(uTextureUnitLocation, 0)

    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    private fun handleError(operation: String) {
        // Check for errors
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            when (error) {
                GLES20.GL_INVALID_ENUM -> {
                    Log.d(TAG, "glGetError($operation): GL_INVALID_ENUM")
                }

                GLES20.GL_INVALID_VALUE -> {
                    Log.d(TAG, "glGetError($operation): GL_INVALID_VALUE")

                }

                GLES20.GL_INVALID_OPERATION -> {
                    Log.d(TAG, "glGetError($operation): GL_INVALID_OPERATION)")

                }

                GLES20.GL_OUT_OF_MEMORY -> {
                    Log.d(TAG, "glGetError($operation): GL_INVALID_OPERATION")
                }

                else -> {
                    Log.d(TAG, "glGetError($operation): Something went wrong...")
                }
            }
        }
    }
}