import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.example.opengles_1.Square
import com.example.opengles_1.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

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
    private val rotationMatrix = FloatArray(16)

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        mSquare = Square()
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

        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        vPMatrix.copyInto(squareMatrix)
        Matrix.translateM(squareMatrix, 0, 0f, -0.8f, 0f)
        Matrix.multiplyMM(scratch, 0, squareMatrix, 0, mSquare.mModelMatrix, 0)
        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrix, 0)

        mSquare.draw(scratch)

        // Triangle

        Matrix.setRotateM(rotationMatrix, 0, angle, 1f, 0f, -1.0f)

        vPMatrix.copyInto(triangleMatrix)
        Matrix.translateM(triangleMatrix, 0, 0f, 0.8f, 0f)
        Matrix.multiplyMM(scratch, 0, triangleMatrix, 0, mTriangle.mModelMatrix, 0)
        Matrix.multiplyMM(scratch, 0, scratch, 0, rotationMatrix, 0)

        mTriangle.draw(scratch)

    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

//        // this projection matrix is applied to object coordinates
//        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 5f)
    }
}
