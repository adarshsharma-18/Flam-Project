package com.example.cameraapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val cameraPermission = Manifest.permission.CAMERA
    private val requestCode = 1001

    private lateinit var textureView: TextureView
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = surfaceTextureListener

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), requestCode)
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (hasCameraPermission()) {
            if (textureView.isAvailable) {
                startCamera()
            } else {
                textureView.surfaceTextureListener = surfaceTextureListener
            }
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (textureView.isAvailable) startCamera()
            else textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            if (hasCameraPermission()) startCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        backgroundHandler = null
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.first()
        manager.openCamera(cameraId, stateCallback, backgroundHandler)
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun startPreview() {
        val device = cameraDevice ?: return
        val surfaceTexture = textureView.surfaceTexture ?: return

        surfaceTexture.setDefaultBufferSize(
            textureView.width.coerceAtLeast(640),
            textureView.height.coerceAtLeast(480)
        )
        val previewSurface = Surface(surfaceTexture)

        val requestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(previewSurface)
            set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        }

        device.createCaptureSession(listOf(previewSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) { }
            },
            backgroundHandler
        )
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }
}


