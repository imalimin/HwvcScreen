package com.alimin.hwvc.screen

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.alimin.hwvc.screen.ui.ReqActivity
import com.alimin.hwvc.screen.widget.FloatWindow
import com.lmy.hwvcnative.processor.AlDisplayRecorder
import java.io.File


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
        path = "${externalCacheDir.path}/camera.mp4"
        val wm = baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        wm.defaultDisplay.getRealSize(size)
        recorder = AlDisplayRecorder(
            mp, size.x, size.y, DisplayMetrics.DENSITY_MEDIUM
        )
        recorder?.setOutputFilePath(path)
        recorder?.setFormat(1088, 1920)
        setupView()

        Log.i("AlDisplayService", "setup success. ${size.x}x${size.y}")
        return true
    }

    private fun setupView() {
        win = FloatWindow(this)
        win?.show()
        win?.setOnStartListener {
            recorder?.cropOutputSize(win!!.getRect())
            Handler().postDelayed({
                recorder?.start()
            }, 1000)
        }
        win?.setOnCloseListener {
            win?.dismiss()
            recorder?.release()
            showNotify()
            stopSelf()
        }
    }

    private fun showNotify() {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(Uri.fromFile(File((path))), "video/mp4")
        }
        val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notification.Builder(this)
            .setContentTitle("Screen record success")
            .setContentText("Click to show or edit.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    baseContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            .build()
        nm.notify(1, notification)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//        } else {
//            showO()
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showO() {
        val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "default", "Screen record",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.setSound(null, null)
        channel.vibrationPattern = null
        nm.createNotificationChannels(
            listOf(
                channel
            )
        )
    }

    companion object {
        private var _instance: AlDisplayService? = null
        fun instance(): AlDisplayService? = _instance
    }
}