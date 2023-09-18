package com.example.opengles_1

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// number of coordinates per vertex in this array
private const val COORDS_PER_VERTEX = 3
var squareCoords = floatArrayOf(
    -0.5f,  0.5f, 0.0f,      // top left
    -0.5f, -0.5f, 0.0f,      // bottom left
    0.5f, -0.5f, 0.0f,      // bottom right
    0.5f,  0.5f, 0.0f       // top right
)
private const val COORDS_PER_TEXTURE_VERTEX = 2
private val TEXTURE_COORDINATES = floatArrayOf(
    //x,    y
    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f,
    1.0f, 1.0f,
)


class Square {

    companion object {
        val TAG = Square::class.simpleName

    }

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val textureVertexStride: Int = COORDS_PER_TEXTURE_VERTEX * 4 // 4 bytes per vertex

    private var quadPositionHandle = 0
    private var texPositionHandle = 0
    private var textureUniformHandle: Int = 0
    private var viewProjectionMatrixHandle: Int = 0
    private val textureUnit = IntArray(1)

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    val vertexShaderCode2 = """
        uniform mat4 uVPMatrix;
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        
        void main(void)
        {
            gl_Position = uVPMatrix * a_Position;
            v_TexCoord = vec2(a_TexCoord.x, (1.0 - (a_TexCoord.y)));
        }
    """

    val fragmentShaderCode2 = """
        precision highp float;
        
        uniform sampler2D u_Texture;
        varying vec2 v_TexCoord;
        
        void main(void){
            gl_FragColor = texture2D(u_Texture, v_TexCoord);
        }
    """

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    private val textureBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(TEXTURE_COORDINATES)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    // Use to access and set the view transformation
    var mModelMatrix = FloatArray(16)
    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode2)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode2)

        Matrix.setIdentityM(mModelMatrix, 0)
        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {
            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)
            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)
            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
        GLES20.glUseProgram(mProgram)

        getHandlers()

        bindData()

        //Enable blend
        GLES20.glEnable(GLES20.GL_BLEND)
        //Uses to prevent transparent area to turn in black
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

    }

    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun setTexture(texture: Bitmap){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(textureUnit.size, textureUnit, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureUnit[0])

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
    }

    fun draw(mvpMatrix: FloatArray) {
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(viewProjectionMatrixHandle, 1, false, mvpMatrix, 0)

        // Enable attribute handlers
        GLES20.glEnableVertexAttribArray(quadPositionHandle)
        GLES20.glEnableVertexAttribArray(texPositionHandle)

        //Draw shape
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(quadPositionHandle)
        GLES20.glDisableVertexAttribArray(texPositionHandle)

    }

    private fun bindData(){
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            quadPositionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        //Pass texture position to shader
        GLES20.glVertexAttribPointer(
            texPositionHandle,
            COORDS_PER_TEXTURE_VERTEX,
            GLES20.GL_FLOAT,
            false,
            textureVertexStride,
            textureBuffer
        )

    }

    private fun handleError(){
        // Check for errors
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            when (error) {
                GLES20.GL_INVALID_ENUM -> {
                    Log.d(TAG, "GLES20.glGetError(): GL_INVALID_ENUM")
                }
                GLES20.GL_INVALID_VALUE -> {
                    Log.d(TAG, "GLES20.glGetError(): GL_INVALID_VALUE")

                }
                GLES20.GL_INVALID_OPERATION -> {
                    Log.d(TAG, "GLES20.glGetError(): GL_INVALID_OPERATION)")

                }
                GLES20.GL_OUT_OF_MEMORY -> {
                    Log.d(TAG, "GLES20.glGetError(): GL_INVALID_OPERATION")
                }
                else -> {
                    Log.d(TAG, "Something went wrong...")
                }
            }
        }
    }

    private fun getHandlers(){
        //Quadrant position handler
        quadPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position")
        //Texture position handler
        texPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoord")
        //Texture uniform handler
        textureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture")
        // View projection transformation matrix handler
        viewProjectionMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix")

        Log.d(TAG, "getHandlers(): indices => quadPositionHandle=$quadPositionHandle " +
                "texPositionHandle=$texPositionHandle " +
                "textureUniformHandle=$textureUniformHandle " +
                "viewProjectionMatrixHandle=$viewProjectionMatrixHandle")
    }

    fun draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            // Draw the square
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.size,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}