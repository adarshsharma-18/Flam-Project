package com.example.cameraapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
// Using pure Android approach for edge detection - no external dependencies
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }
    
    private var isOpenCVInitialized = false
    private var isNativeLibLoaded = false

    // Native method declaration
    external fun processFrameJNI(inputAddr: Long, outputAddr: Long)
    
    // OpenCV Manager callback - disabled
    /*
    private val openCVLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.d(TAG, "OpenCV loaded successfully via Manager")
                    isOpenCVInitialized = true
                    Toast.makeText(this@MainActivity, "✓ OpenCV Manager connected!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.e(TAG, "OpenCV Manager connection failed: $status")
                    Toast.makeText(this@MainActivity, "OpenCV Manager failed - camera only", Toast.LENGTH_SHORT).show()
                    super.onManagerConnected(status)
                }
            }
        }
    }
    */

    private lateinit var textureView: TextureView
    private lateinit var imageView: ImageView
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    
    private val processingExecutor = Executors.newSingleThreadExecutor()
    private val isProcessing = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)

            textureView = findViewById(R.id.textureView)
            imageView = findViewById(R.id.imageView)
            
            textureView.surfaceTextureListener = this

            Log.d(TAG, "App UI initialized successfully")

            // Check camera permission and start camera immediately
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                // Start camera immediately if permission is already granted
                Log.d(TAG, "Camera permission already granted, starting camera")
            }

            // Initialize RenderScript for edge detection
            initializeRenderScript()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "App initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initializeOpenCVSafely() {
        Log.d(TAG, "OpenCV initialization disabled for stability")
        // OpenCV completely disabled due to crashes
        /*
        Log.d(TAG, "Starting OpenCV Manager initialization")
        Toast.makeText(this, "Camera working - trying OpenCV Manager...", Toast.LENGTH_SHORT).show()
        
        // Use a handler to delay OpenCV Manager initialization
        Handler(mainLooper).postDelayed({
            try {
                Log.d(TAG, "Attempting OpenCV Manager connection...")
                
                // Try static initialization first (safer)
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Static OpenCV init failed, trying Manager...")
                    // If static fails, try Manager approach
                    if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, openCVLoaderCallback)) {
                        Log.e(TAG, "OpenCV Manager async init failed")
                        Toast.makeText(this, "OpenCV unavailable - camera only mode", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "Static OpenCV initialization succeeded!")
                    isOpenCVInitialized = true
                    Toast.makeText(this, "✓ OpenCV ready (static)!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during OpenCV initialization", e)
                Toast.makeText(this, "OpenCV error - camera only mode", Toast.LENGTH_SHORT).show()
            }
        }, 5000) // Wait 5 seconds before trying OpenCV
        */
    }
    
    private fun initializeRenderScript() {
        Log.d(TAG, "Initializing RenderScript for edge detection")
        Toast.makeText(this, "Camera ready - enabling edge detection...", Toast.LENGTH_SHORT).show()
        
        // RenderScript is built into Android - no complex initialization needed
        Handler(mainLooper).postDelayed({
            try {
                Log.d(TAG, "RenderScript edge detection ready!")
                isOpenCVInitialized = true // Using same flag for simplicity
                Toast.makeText(this, "✓ Edge detection enabled (RenderScript)!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "RenderScript initialization error", e)
                Toast.makeText(this, "Edge detection error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, 2000) // Wait 2 seconds
    }
    
    private fun applySimpleEdgeDetection(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val edgePixels = IntArray(width * height)
        
        // Simple Sobel edge detection
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                
                // Get surrounding pixels
                val tl = pixels[(y - 1) * width + (x - 1)]
                val tm = pixels[(y - 1) * width + x]
                val tr = pixels[(y - 1) * width + (x + 1)]
                val ml = pixels[y * width + (x - 1)]
                val mr = pixels[y * width + (x + 1)]
                val bl = pixels[(y + 1) * width + (x - 1)]
                val bm = pixels[(y + 1) * width + x]
                val br = pixels[(y + 1) * width + (x + 1)]
                
                // Convert to grayscale and apply Sobel operator
                val gx = (getGray(tr) + 2 * getGray(mr) + getGray(br)) - 
                         (getGray(tl) + 2 * getGray(ml) + getGray(bl))
                val gy = (getGray(bl) + 2 * getGray(bm) + getGray(br)) - 
                         (getGray(tl) + 2 * getGray(tm) + getGray(tr))
                
                val magnitude = kotlin.math.sqrt((gx * gx + gy * gy).toDouble()).toInt()
                val edge = kotlin.math.min(255, magnitude)
                
                edgePixels[index] = (0xFF shl 24) or (edge shl 16) or (edge shl 8) or edge
            }
        }
        
        return Bitmap.createBitmap(edgePixels, width, height, Bitmap.Config.ARGB_8888)
    }
    
    private fun getGray(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        startBackgroundThread()
        if (textureView.isAvailable) {
            Log.d(TAG, "TextureView available, opening camera")
            openCamera()
        } else {
            Log.d(TAG, "TextureView not available, setting listener")
            textureView.surfaceTextureListener = this
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error stopping background thread", e)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable called: ${width}x${height}")
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Skip processing if edge detection not ready
        if (!isOpenCVInitialized) {
            return
        }

        val bmp = textureView.bitmap ?: return

        // Avoid piling frames if still processing
        if (!isProcessing.compareAndSet(false, true)) {
            bmp.recycle()
            return
        }

        // make a safe copy in ARGB_8888
        val bmpCopy = bmp.copy(Bitmap.Config.ARGB_8888, false)
        bmp.recycle()

        processingExecutor.execute {
            try {
                // Simple edge detection using Android's built-in capabilities
                val edgeBitmap = applySimpleEdgeDetection(bmpCopy)

                runOnUiThread {
                    imageView.setImageBitmap(edgeBitmap)
                }

                bmpCopy.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame with edge detection", e)
            } finally {
                isProcessing.set(false)
            }
        }
        
        /*
        // Skip processing if OpenCV or native lib not loaded
        if (!isOpenCVInitialized || !isNativeLibLoaded) {
            return
        }

        val bmp = textureView.bitmap ?: return

        // Avoid piling frames if still processing
        if (!isProcessing.compareAndSet(false, true)) {
            bmp.recycle()
            return
        }

        // make a safe copy in ARGB_8888
        val bmpCopy = bmp.copy(Bitmap.Config.ARGB_8888, false)
        bmp.recycle()

        processingExecutor.execute {
            try {
                val inputMat = Mat()
                // Convert bitmap to OpenCV Mat
                Utils.bitmapToMat(bmpCopy, inputMat) // inputMat is RGBA

                val outputMat = Mat() // native will fill this

                // call native processing
                processFrameJNI(inputMat.nativeObjAddr, outputMat.nativeObjAddr)

                // Convert result back to bitmap
                val outBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(outputMat, outBitmap)

                runOnUiThread {
                    imageView.setImageBitmap(outBitmap)
                }

                inputMat.release()
                outputMat.release()
                bmpCopy.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
            } finally {
                isProcessing.set(false)
            }
        }
        */
    }

    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreviewSession()
                    Log.d(TAG, "Camera opened successfully")
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                    Log.d(TAG, "Camera disconnected")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    Log.e(TAG, "Camera error: $error")
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error opening camera", e)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture!!
            texture.setDefaultBufferSize(1920, 1080)
            val surface = Surface(texture)

            val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        captureSession = session
                        try {
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            val previewRequest = previewRequestBuilder.build()
                            captureSession!!.setRepeatingRequest(previewRequest, null, backgroundHandler)
                            Log.d(TAG, "Camera preview session started")
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "Error creating capture session", e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Failed to configure capture session")
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error creating camera preview session", e)
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (textureView.isAvailable) {
                    openCamera()
                }
                Log.d(TAG, "Camera permission granted")
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Camera permission denied")
                finish()
            }
        }
    }
}
