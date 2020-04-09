package com.alimin.hwvc.screen.ui

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alimin.hwvc.screen.AlDisplayService
import com.alimin.hwvc.screen.R

class ReqActivity : AppCompatActivity() {
    private var mpm: MediaProjectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_req)

        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mpm?.createScreenCaptureIntent()
        startActivityForResult(intent, REQ_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK == resultCode) {
            if (data != null && REQ_PROJECTION == requestCode) {
                AlDisplayService.instance()?.setup(mpm?.getMediaProjection(resultCode, data))
            }
        }
        finish()
    }

    companion object {
        const val REQ_PROJECTION = 0x01
    }
}