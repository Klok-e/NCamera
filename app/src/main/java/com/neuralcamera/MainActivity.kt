package com.neuralcamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.infinum.goldeneye.GoldenEye
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.models.Size
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var goldenEye: GoldenEye? = null

    fun takePhoto(@Suppress("UNUSED_PARAMETER") view: View) {
        goldenEye?.takePicture(object : PictureCallback() {
            override fun onError(t: Throwable) {
                println(t.message)
            }

            override fun onPictureTaken(picture: Bitmap) {

            }
        })
    }

    private fun requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            /* Find back camera */
            val backCamera = goldenEye?.availableCameras?.find { it.facing == Facing.BACK }
            /* Open back camera */
            if (backCamera != null) {
                goldenEye?.open(camera_preview, backCamera, object : InitCallback() {
                    override fun onError(t: Throwable) {
                        println("Error: ${t.message}")
                    }
                })
                println(goldenEye?.config?.supportedPictureSizes)
                println(goldenEye?.config?.supportedPreviewSizes)
            } else {
                println("Error: Couldn't find camera")
            }
        } else {
            requestAllPermissions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestAllPermissions()

        goldenEye = GoldenEye.Builder(this).build()
    }

    override fun onResume() {
        super.onResume()
        openCamera()
    }

    override fun onPause() {
        super.onPause()

        goldenEye?.release()
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
