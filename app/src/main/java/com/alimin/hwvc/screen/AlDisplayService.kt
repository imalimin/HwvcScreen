package com.alimin.hwvc.screen

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.alimin.hwvc.screen.ui.EditActivity
import com.alimin.hwvc.screen.ui.ReqActivity
import com.alimin.hwvc.screen.ui.win.RecordWindow
import com.alimin.hwvc.screen.ui.win.StopWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
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
    private lateinit var path: String
    private val displaySize = Point()
    private val formatter = SimpleDateFormat("yyyMMdd-HHmmss")
    private var isRecording = false

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
        val dir = File("${Environment.getExternalStorageDirectory().path}/DCIM/Screen recordings")
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
        mediaNotify(applicationContext, path)
        showDoneNotify()
        isRecording = false
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
        RecordWindow(applicationContext).apply {
            setOnStartListener {
                val isFull = 0f == it.left && 0f == it.top && 0f == it.right && 0f == it.bottom
                if (isFull) {
                    showStopNotify()
                } else {
                    recorder?.cropOutputSize(it)
                }
                isRecording = true
                recorder?.start()
                dismiss()
                if (!isFull) {
                    StopWindow(applicationContext).apply {
                        setRectF(it)
                        setOnStopListener {
                            dismiss()
                            shutdown()
                        }
                    }
                }
            }
        }
    }

    private fun showDoneNotify(bitmap: Bitmap) {
        Toast.makeText(
            applicationContext,
            "${resources.getString(R.string.msg_record_success)}，文件保存在（${path}）",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(applicationContext, EditActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("path", path)
        }
        val pendingIntent = PendingIntent.getActivity(
            baseContext, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                resources.getString(R.string.id_handle_on_notification),
                resources.getString(R.string.handle_on_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            channel.vibrationPattern = null
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(
            this,
            resources.getString(R.string.id_handle_on_notification)
        )
            .setContentTitle("录屏成功")
            .setContentText("点击进行编辑")
            .setSmallIcon(R.mipmap.ic_media_play)
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            .setContentIntent(pendingIntent)
            .addAction(newEditAction(pendingIntent))
            .addAction(newDeleteAction())
            .addAction(newShareAction())
            .build().apply {
                flags = Notification.FLAG_AUTO_CANCEL
            }
        nm.notify(NOTIFY_DONE_ID, notification)
    }

    private fun showDoneNotify() {
        val target = object : SimpleTarget<Bitmap>(displaySize.x, displaySize.x) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                showDoneNotify(resource)
            }
        }
        Glide.with(applicationContext)
            .setDefaultRequestOptions(RequestOptions().apply {
                frame(0)
                centerCrop()
            })
            .asBitmap()
            .load(path)
            .into(target)
    }

    private fun showStopNotify() {
        val nm =
            baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                resources.getString(R.string.id_handle_on_notification),
                resources.getString(R.string.handle_on_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = resources.getString(R.string.id_handle_on_notification)
                setSound(null, null)
                vibrationPattern = null
            }
            nm.createNotificationChannel(channel)
        }
        val intent = Intent(this, MediaOperateReceiver::class.java).apply {
            action = "media_stop"
        }
        val notification = NotificationCompat.Builder(
            this,
            resources.getString(R.string.id_handle_on_notification)
        )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
                flags =
                    Notification.FLAG_AUTO_CANCEL or Notification.FLAG_NO_CLEAR or Notification.FLAG_SHOW_LIGHTS
            }
        nm.notify(NOTIFY_RECORDING_ID, notification)
    }

    private fun newEditAction(intent: PendingIntent): NotificationCompat.Action =
        NotificationCompat.Action(0, resources.getString(R.string.action_edit), intent)

    private fun newDeleteAction(): NotificationCompat.Action = NotificationCompat.Action(
        0, resources.getString(R.string.action_delete),
        PendingIntent.getBroadcast(
            baseContext, 0, Intent(this, MediaOperateReceiver::class.java).apply {
                action = "media_delete"
                data = Uri.fromFile(File(path))
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    private fun newShareAction(): NotificationCompat.Action = NotificationCompat.Action(
        0, resources.getString(R.string.action_share),
        PendingIntent.getActivity(
            baseContext, 0, Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(
                    Intent.EXTRA_STREAM, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            applicationContext,
                            "${applicationContext.packageName}.provider",
                            File(path)
                        )
                    } else {
                        Uri.fromFile(File(path))
                    }
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, applicationContext.resources.getString(R.string.action_share)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    )

    companion object {
        private var _instance: AlDisplayService? = null
        const val NOTIFY_RECORDING_ID = 0x998
        const val NOTIFY_DONE_ID = NOTIFY_RECORDING_ID - 1
        fun instance(): AlDisplayService? {
            return _instance
        }

        fun isRecording(): Boolean = null != instance() && instance()!!.isRecording

        fun mediaNotify(context: Context, path: String) {
            MediaScannerConnection.scanFile(
                context,
                Array(1) { path },
                Array(1) { MediaStore.Video.Media.CONTENT_TYPE }
            ) { _: String, uri: Uri ->
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                context.sendBroadcast(mediaScanIntent)
            }
        }
    }
}

class MediaOperateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val nm = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (intent.action == "media_start") {

        } else if (intent.action == "media_stop") {
            nm.cancel(AlDisplayService.NOTIFY_RECORDING_ID)
            AlDisplayService.instance()?.shutdown()
        } else if (intent.action == "media_pause") {

        } else {
            nm.cancel(AlDisplayService.NOTIFY_DONE_ID)
            val path = intent.data?.path
            if (!TextUtils.isEmpty(path)) {
                val file = File(path)
                if (intent.action == "media_delete") {
                    val ret = context.contentResolver.delete(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Audio.Media.DATA + "= \"${path}\"", null
                    )
                    if (ret > 0) {
                        file.delete()
                    }
                }
            }
        }
    }
}