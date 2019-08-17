package com.neuralcamera

import android.Manifest
import android.content.Intent
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
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.Facing
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer


class CameraSwapper(private val cameras: Array<CameraInfo>) {
    private var currentInd = 0

    val currCam: CameraInfo
        get() = cameras[currentInd]


    fun swapCams(): CameraInfo {
        currentInd++
        if (currentInd >= cameras.count())
            currentInd = 0
        return currCam
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var goldenEye: GoldenEye
    private lateinit var usingCamera: CameraSwapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        requestAllPermissions()

        goldenEye = GoldenEye.Builder(this).build()

        val cameraList = ArrayList<CameraInfo>()
        for (facing in arrayOf(Facing.BACK, Facing.FRONT)) {
            val c = goldenEye.availableCameras.find { it.facing == facing }
            if (c != null)
                cameraList.add(c)
        }
        usingCamera = CameraSwapper(cameraList.toArray(arrayOfNulls(cameraList.size)))
    }

    override fun onResume() {
        super.onResume()
        openCamera(usingCamera.currCam)
    }

    override fun onPause() {
        super.onPause()

        goldenEye.release()
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun processImage(img: ByteArray, width: Int, height: Int)

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    private fun requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun openCamera(cam: CameraInfo) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions()
        }

        goldenEye.open(camera_preview, cam, object : InitCallback() {
            override fun onError(t: Throwable) {
                println("Error: ${t.message}")
            }
        })
        //println(goldenEye.config?.supportedPictureSizes)
        //println(goldenEye.config?.supportedPreviewSizes)
    }

    // Event listeners

    fun takePhoto(@Suppress("UNUSED_PARAMETER") view: View) {
        goldenEye.takePicture(object : PictureCallback() {
            override fun onError(t: Throwable) {
                println(t.message)
            }

            override fun onPictureTaken(picture: Bitmap) {
                val buff = ByteBuffer.allocate(picture.allocationByteCount)
                picture.copyPixelsToBuffer(buff)
                processImage(buff.array(), picture.width, picture.height)
                buff.rewind()
                picture.copyPixelsFromBuffer(buff)

                // save image on the device
                val uri = CapturePhotoUtils.insertImage(
                    contentResolver,
                    picture,
                    "got_waifued_${java.util.Calendar.getInstance()}",
                    ""
                )
                println(uri)

                // view in another activity
                val intent = Intent(applicationContext, ViewImageActivity::class.java)
                intent.putExtra("DisplayBitmapUri", uri)
                startActivity(intent)
            }
        })
    }

    fun swapCamera(@Suppress("UNUSED_PARAMETER") view: View) {
        openCamera(usingCamera.swapCams())
    }


}
