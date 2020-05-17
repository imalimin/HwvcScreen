package com.alimin.hwvc.screen

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.alimin.hwvc.screen.ui.ReqActivity
import com.alimin.hwvc.screen.widget.FloatWindow
import com.lmy.common.model.AlPreference
import com.lmy.hwvcnative.processor.AlDisplayRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AlDisplayService : Service() {
    // 质量
    private var pQuality by AlPreference(MyApplication.instance.applicationContext, "Quality", 2)
    // 效率
    private var pEff by AlPreference(MyApplication.instance.applicationContext, "Eff", 2)
    // 声音
    private var pVoice by AlPreference(MyApplication.instance.applicationContext, "Voice", 0)
    // 编码方式
    private var pCodec by AlPreference(MyApplication.instance.applicationContext, "Codec", 0)
    private var recorder: AlDisplayRecorder? = null
    private var win: FloatWindow? = null
    private lateinit var path: String
    private val displaySize = Point()
    private val formatter = SimpleDateFormat("yyyMMdd-HHmmss")

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
        val dir = File("${Environment.getExternalStorageDirectory().path}/Screen recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val c = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
        path = "${dir.path}/${formatter.format(c.time)}.mp4"
        val wm = baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(displaySize)
        recorder = AlDisplayRecorder(
            mp, displaySize.x, displaySize.y, DisplayMetrics.DENSITY_MEDIUM
        )
        setupParams()
        setupView()
        Log.i("AlDisplayService", "setup success. ${displaySize.x}x${displaySize.y}")
        return true
    }

    fun shutdown() {
        recorder?.release()
        MediaScannerConnection.scanFile(
            this,
            Array(1) { path },
            Array(1) { MediaStore.Video.Media.CONTENT_TYPE }
        ) { _: String, uri: Uri ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = uri
            sendBroadcast(mediaScanIntent)
        }
        showDoneNotify()
        stopSelf()
    }

    private fun setupParams() {
        recorder?.setOutputFilePath(path)
        when (pQuality) {
            0 -> {
                recorder?.setBitrate(1)
            }
            1 -> {
                recorder?.setBitrate(3)
            }
            2 -> {
                recorder?.setBitrate(5)
            }
        }
        when (pEff) {
            0 -> {
                recorder?.setProfile("Baseline")
                recorder?.setPreset("ultrafast")
            }
            1 -> {
                recorder?.setProfile("Baseline")
            }
            2 -> {
                recorder?.setProfile("Main")
            }
            3 -> {
                recorder?.setProfile("High")
            }
        }
        val size = displaySize
        when (pCodec) {
            0 -> {
                recorder?.setEnableHardware(true)
            }
            1 -> {
                recorder?.setEnableHardware(false)
                //软编限制分辨率
                size.x = 720
                size.y = 1280
            }
        }
        when (pVoice) {
            0 -> {
                recorder?.setFormat(size.x, size.y, 0)
            }
            1 -> {
                recorder?.setFormat(size.x, size.y)
            }
        }
    }

    private fun setupView() {
        win = FloatWindow(this)
        win?.show()
        win?.setOnStartListener {
            win?.setClickable(false)
            recorder?.cropOutputSize(win!!.getRect())
            Handler().postDelayed({
                recorder?.start()
            }, 1000)
        }
        win?.setOnCloseListener {
            win?.dismiss()
            shutdown()
        }
        win?.setOnFullListener {
            win?.dismiss()
            showStopNotify()
            recorder?.start()
        }
    }

    private fun showDoneNotify() {
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
            .setContentTitle("录屏成功")
            .setContentText("点击播放")
            .setSmallIcon(R.mipmap.ic_media_play)
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

    private fun showStopNotify() {
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
        val intent = Intent(this, MediaOperateReceiver::class.java).apply {
            action = "media_stop"
        }
        val notification = NotificationCompat.Builder(this, N_CHANNEL_ID)
            .setContentTitle("录屏中")
            .setContentText("点击停止")
            .setSmallIcon(R.drawable.ic_recording)
            .setContentIntent(
                PendingIntent.getBroadcast(
                    baseContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            )
            .build().apply {
                flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_NO_CLEAR or Notification.FLAG_SHOW_LIGHTS
            }
        nm.notify(MEDIA_OPERATE_ID, notification)
    }

    companion object {
        private const val N_CHANNEL_ID = "hwvc_screen_record"
        private var _instance: AlDisplayService? = null
        const val MEDIA_OPERATE_ID = 0x998
        fun instance(): AlDisplayService? = _instance
    }
}

class MediaOperateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val nm = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(AlDisplayService.MEDIA_OPERATE_ID)

        if (intent.action == "media_start") {

        } else if (intent.action == "media_stop") {
            AlDisplayService.instance()?.shutdown()
        } else if (intent.action == "media_pause") {

        }
    }

}