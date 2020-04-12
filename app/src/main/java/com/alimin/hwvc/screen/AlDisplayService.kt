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
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
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

    fun shutdown() {
        recorder?.release()
        stopSelf()
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
            val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Uri.fromFile(File(path))
            } else {
                FileProvider.getUriForFile(
                    baseContext,
                    applicationContext.packageName + ".provider",
                    File(path)
                )
            }
            setDataAndType(uri, "video/mp4")
        }
        val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                N_CHANNEL_ID, N_CHANNEL_ID,
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
        val notification = NotificationCompat.Builder(this, N_CHANNEL_ID)
            .setContentTitle("Screen record success")
            .setContentText("Click to show or edit.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    baseContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            .build().apply {
                flags = Notification.FLAG_AUTO_CANCEL
            }
        nm.notify(1, notification)
    }

    companion object {
        private const val N_CHANNEL_ID = "hwvc_screen_record"
        private var _instance: AlDisplayService? = null
        fun instance(): AlDisplayService? = _instance
    }
}