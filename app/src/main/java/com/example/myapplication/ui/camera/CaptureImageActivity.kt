package com.example.myapplication.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityCaptureImageBinding
import com.example.myapplication.ui.base.BaseActivity
import com.example.myapplication.utils.AppUtils
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureImageActivity : BaseActivity() {
    companion object {
        const val TAG = "CameraX"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val REQUEST_CODE_PERMISSIONS = 100
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"

        fun getStartIntent(context: Context): Intent {
            val intent = Intent(context, CaptureImageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            return intent
        }
    }

    private var mImageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mBinding: ActivityCaptureImageBinding
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private var bitmapImage: Bitmap? = null
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mCameraFutureProvider: ListenableFuture<ProcessCameraProvider>
    private lateinit var mPreviewView: PreviewView
    private lateinit var mImagePreview: Preview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCaptureImageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mPreviewView = mBinding.previewView

        mCameraFutureProvider = ProcessCameraProvider.getInstance(this)

        // request camera permissions
        if (isPermissionsGranted()) {
            mPreviewView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        mBinding.captureImgButton.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = getOutputDirectory()

        mBinding.torchButton.setOnClickListener {
            toggleTorch()
        }
        mBinding.confirmButton.setOnClickListener { saveImage() }
        mBinding.cancelButton.setOnClickListener {
            mPreviewView.post { startCamera() }
            hideOnImageReviewButtons()
        }
    }

    private fun isPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        // select back camera as default
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        mImagePreview = Preview.Builder().apply {
            setTargetRotation(mPreviewView.display.rotation)
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
        }.build()

        mImageCapture = ImageCapture.Builder().apply {
            setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        }.build()

        mCameraFutureProvider.addListener({
            // used to bind the lifCycle of camera to lifeCycleOwner
            mCameraProvider = mCameraFutureProvider.get()
            try {
                // unbind use cases before rebinding
                mCameraProvider.unbindAll()

                // bind use cases to camera
                val camera =
                    mCameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        mImagePreview,
                        mImageCapture
                    )
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
                setTorchStateObserver()
                mPreviewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                mImagePreview.setSurfaceProvider(mPreviewView.surfaceProvider)

            } catch (e: Exception) {
                Log.e(TAG, "use case binding failed ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // This will be null If you tap the photo button before image capture is set up.
        val imageCapture = mImageCapture ?: return

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    bitmapImage = AppUtils.toBitmap(image)
                    mPreviewView.post {
                        mCameraProvider.unbindAll()
                        showOnImageReviewButtons()
                    }
                    super.onCaptureSuccess(image)
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast(exception.message!!)
                    super.onError(exception)
                }
            })
    }

    private fun saveImage() {
        //create timestamped photo file to hold the img
        val fileName = SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"

        val imgFile = File(outputDirectory, fileName)
        imgFile.writeBitmap(bitmapImage!!, Bitmap.CompressFormat.JPEG, 90)
        val intent = Intent()
        intent.putExtra("imgUri", imgFile.toUri())
        setResult(RESULT_OK, intent)
        Log.e(TAG, "saveImage: ${imgFile.toUri()}")
        finish()
    }

    private fun File.writeBitmap(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int
    ) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    @SuppressLint("NewApi")
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (isPermissionsGranted()) {
                mPreviewView.post { startCamera() }
            } else {
                showToast("Разрешения не предоставлены пользователем")
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun showOnImageReviewButtons() {
        mBinding.captureImgButton.isVisible = false
        mBinding.torchButton.isVisible = false
        mBinding.buttonWrapper.isVisible = true
    }

    private fun hideOnImageReviewButtons() {
        mBinding.captureImgButton.isVisible = true
        mBinding.torchButton.isVisible = true
        mBinding.buttonWrapper.isVisible = false
    }

    private fun toggleTorch() {
        if (cameraInfo.torchState.value == TorchState.ON) {
            cameraControl.enableTorch(true)
        } else {
            cameraControl.enableTorch(false)
        }

    }

    private fun setTorchStateObserver() {
        cameraInfo.torchState.observe(this, {
            if (it == TorchState.ON) {
                mBinding.torchButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_flash_on_24
                    )
                )
            } else {
                mBinding.torchButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_flash_off_24
                    )
                )
            }
        })
    }
}