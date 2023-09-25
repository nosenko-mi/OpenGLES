package com.example.opengles_1

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube(
    private val context: Context
) {

    private val POSITION_COUNT = 3

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

    init {
        createAndUseProgram()
        getLocations()
        prepareData()
        bindData()
        Matrix.setIdentityM(this.mModelMatrix, 0)
        Log.d("Cube", "programId: $programId")
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mvpMatrix, 0)

//        Matrix.setIdentityM(mvpMatrix, 0)
        GLES20.glUseProgram(programId)
        // вращение
//        setModelMatrix()
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray)
        GLES20.glDisableVertexAttribArray(aPositionLocation)

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
        indexArray!!.position(0)
        texture = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.sphynx, R.drawable.sphynx2, R.drawable.sphynx3,
                R.drawable.sphynx4, R.drawable.sphynx5, R.drawable.sphynx6
            )
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
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        uTextureUnitLocation = GLES20.glGetUniformLocation(programId, "u_TextureUnit")
        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix")
    }

    private fun bindData() {
        // координаты вершин
        vertexData!!.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GLES20.GL_FLOAT,
            false, 0, vertexData
        )
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        // помещаем текстуру в target CUBE_MAP юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, texture)

        // юнит текстуры
        GLES20.glUniform1i(uTextureUnitLocation, 0)
    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }
}