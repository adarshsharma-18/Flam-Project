package com.example.cameraapp

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.graphics.Bitmap
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class MyGLRenderer : GLSurfaceView.Renderer {
    companion object {
        private const val TAG = "MyGLRenderer"
        
        // Vertex shader for rendering texture
        private const val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec2 vTexCoord;
            varying vec2 texCoord;
            void main() {
                gl_Position = vPosition;
                texCoord = vTexCoord;
            }
        """
        
        // Fragment shader for rendering texture with effects
        private const val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform int uEffect;
            varying vec2 texCoord;
            void main() {
                vec4 color = texture2D(uTexture, texCoord);
                
                if (uEffect == 0) {
                    // Normal
                    gl_FragColor = color;
                } else if (uEffect == 1) {
                    // Invert colors
                    gl_FragColor = vec4(1.0 - color.rgb, color.a);
                } else if (uEffect == 2) {
                    // Grayscale
                    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                    gl_FragColor = vec4(gray, gray, gray, color.a);
                } else if (uEffect == 3) {
                    // Sepia
                    float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
                    float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
                    float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
                    gl_FragColor = vec4(r, g, b, color.a);
                } else {
                    // Default
                    gl_FragColor = color;
                }
            }
        """
    }
    
    private var textureId = 0
    private var bitmap: Bitmap? = null
    private val lock = Any()
    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var effectHandle = 0
    private var currentEffect = 0 // 0=normal, 1=invert, 2=grayscale, 3=sepia
    
    // Quad vertices (position + texture coordinates)
    private val quadVertices = floatArrayOf(
        // positions     // texture coords
        -1.0f,  1.0f,    0.0f, 0.0f,  // top left
        -1.0f, -1.0f,    0.0f, 1.0f,  // bottom left
         1.0f, -1.0f,    1.0f, 1.0f,  // bottom right
         1.0f,  1.0f,    1.0f, 0.0f   // top right
    )
    
    private val quadIndices = shortArrayOf(0, 1, 2, 0, 2, 3)
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ByteBuffer

    fun updateBitmap(bmp: Bitmap) {
        synchronized(lock) {
            bitmap?.recycle() // Clean up previous bitmap
            bitmap = bmp.copy(bmp.config ?: Bitmap.Config.ARGB_8888, false) // Make a copy
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        
        // Initialize buffers
        initBuffers()
        
        // Create shader program
        program = createShaderProgram()
        
        // Create texture
        textureId = createTexture()
        
        // Get shader handles
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        effectHandle = GLES20.glGetUniformLocation(program, "uEffect")
        
        Log.d(TAG, "OpenGL setup complete")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        synchronized(lock) {
            bitmap?.let { bmp ->
                // Update texture with new bitmap
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
                
                // Draw the quad
                drawQuad()
                
                // Clean up
                bmp.recycle()
                bitmap = null
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ${width}x${height}")
        GLES20.glViewport(0, 0, width, height)
    }
    
    private fun initBuffers() {
        // Initialize vertex buffer
        val bb = ByteBuffer.allocateDirect(quadVertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(quadVertices)
        vertexBuffer.position(0)
        
        // Initialize index buffer
        indexBuffer = ByteBuffer.allocateDirect(quadIndices.size * 2)
        indexBuffer.order(ByteOrder.nativeOrder())
        for (index in quadIndices) {
            indexBuffer.putShort(index)
        }
        indexBuffer.position(0)
    }
    
    private fun drawQuad() {
        // Use shader program
        GLES20.glUseProgram(program)
        
        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        // Set vertex data
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        
        vertexBuffer.position(2)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        
        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        
        // Set effect
        GLES20.glUniform1i(effectHandle, currentEffect)
        
        // Draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, quadIndices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        
        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun createTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return tex[0]
    }
    
    private fun createShaderProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // Check link status
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error linking program: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            return 0
        }
        
        return program
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        // Check compile status
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    fun setEffect(effect: Int) {
        currentEffect = effect
        Log.d(TAG, "Effect changed to: $effect")
    }
    
    fun getEffectName(): String {
        return when (currentEffect) {
            0 -> "Normal"
            1 -> "Invert"
            2 -> "Grayscale"
            3 -> "Sepia"
            else -> "Unknown"
        }
    }
}
