package com.alimin.hwvc.screen

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var mpm: MediaProjectionManager? = null
    private val callback = object : MediaProjection.Callback() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mpm?.createScreenCaptureIntent()
        startActivityForResult(intent, REQ_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data) {
            if (REQ_PROJECTION == requestCode && RESULT_OK == resultCode) {
//                mp = mpm?.getMediaProjection(resultCode, data)
            }
        }
    }

    companion object {
        const val REQ_PROJECTION = 0x01
    }
}
