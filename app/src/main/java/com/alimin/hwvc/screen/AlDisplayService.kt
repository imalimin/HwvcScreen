package com.alimin.hwvc.screen

import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.IBinder
import android.util.Log
import com.alimin.hwvc.screen.ui.ReqActivity
import com.alimin.hwvc.screen.widget.FloatWindow
import com.lmy.hwvcnative.processor.AlDisplayRecorder

class AlDisplayService : Service() {
    private var recorder: AlDisplayRecorder? = null
    private var win: FloatWindow? = null
    private lateinit var path: String

    override fun onCreate() {
        _instance = this
        super.onCreate()
        Log.i("AlDisplayService", "onCreate.")
        startActivity(Intent(baseContext, ReqActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        win?.dismiss()
        recorder?.release()
        Log.e("AlDisplayService", "onDestroy.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun setup(mp: MediaProjection?): Boolean {
        if (null == mp) {
            Log.e("AlDisplayService", "setup failed.")
            return false
        }
        recorder?.release()
        val metrics = resources.displayMetrics
        path = "${externalCacheDir.path}/camera.mp4"
        recorder = AlDisplayRecorder(
            mp, metrics.widthPixels, metrics.heightPixels,
            metrics.densityDpi
        )
        recorder?.setOutputFilePath(path)
        recorder?.setFormat(1088, 1792)
        setupView()
        Log.i("AlDisplayService", "setup success.")
        return true
    }

    private fun setupView() {
        win = FloatWindow(this)
        win?.show()
        win?.setOnStartListener {
            recorder?.cropOutputSize(win!!.getRect())
            recorder?.start()
        }
        win?.setOnCloseListener { stopSelf() }
    }

    companion object {
        private var _instance: AlDisplayService? = null
        fun instance(): AlDisplayService? = _instance
    }
}