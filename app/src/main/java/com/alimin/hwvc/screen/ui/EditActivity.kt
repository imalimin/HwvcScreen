package com.alimin.hwvc.screen.ui

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.alimin.hwvc.screen.R
import com.lmy.common.ext.size
import com.lmy.common.ui.BaseActivity
import com.lmy.hwvcnative.tools.AlFFUtils
import kotlinx.android.synthetic.main.activity_edit.*
import java.io.File
import kotlin.concurrent.thread

class EditActivity : BaseActivity() {
    override val layoutResID: Int = R.layout.activity_edit
    private var dialog: AlertDialog? = null
    private var player: MediaPlayer? = null
    private val displaySize = Point()
    private val vSize = Point()
    private var duration = 0
    private var mInputPath: String? = null
    private lateinit var mExportDir: File
    private val mSizeVec = LongArray(3)
    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            player?.setDisplay(holder)
        }

        override fun surfaceDestroyed(p0: SurfaceHolder?) {
            player?.pause()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
        }
    }

    override fun initView() {
        showToolbar(toolbar, R.string.preview, R.mipmap.ic_back_black)
        fillStatusBar()
        setDarkStatusBar()
        mExportDir = File(externalCacheDir, "export")
        mInputPath = intent.getStringExtra("path")
//        mInputPath = "/sdcard/cctv_news_.mp4"
        if (TextUtils.isEmpty(mInputPath) || !File(mInputPath).exists()) {
            Toast.makeText(this, R.string.msg_invalid_path, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val wm = baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(displaySize)
        displaySize.y = displaySize.x
        enterBtn.setOnClickListener {
            export(mInputPath!!)
        }
        setupPlayer(mInputPath!!)
    }

    private fun setupPlayer(path: String) {
        player = MediaPlayer()
        sView.keepScreenOn = true
        sView.holder.addCallback(surfaceCallback)
        player?.setOnInfoListener { mp, what, extra ->
            duration = mp.duration
            vSize.x = mp.videoWidth
            vSize.y = mp.videoHeight
            setupViewSize(vSize.x, vSize.y)
            return@setOnInfoListener true
        }
        player?.setOnPreparedListener { mp ->
            mp.start()
        }
        player?.setDataSource(path)
        player?.isLooping = true
        player?.prepareAsync()
    }

    private fun setupViewSize(w: Int, h: Int) {
        tipView.visibility = if (duration >= 20000) View.VISIBLE else View.GONE
        var width = w
        var height = h
        val r0 = w / h.toFloat()
        val r1 = displaySize.x / displaySize.y.toFloat()
        if (r0 > r1) {
            width = displaySize.x
            height = (width / r0).toInt()
        } else {
            height = displaySize.y
            width = (height * r0).toInt()
        }
        sView.layoutParams.width = width
        sView.layoutParams.height = height
        sView.requestLayout()
    }

    private fun export(src: String) {
        dialog = AlertDialog.Builder(this).setMessage(R.string.msg_calculate_size_doing)
            .setCancelable(false).show()
        thread(start = true) {
            export(src, Quality.LOW)
            export(src, Quality.MEDIUM)
            export(src, Quality.HIGH)
            runOnUiThread {
                exportDone()
            }
        }
    }

    private fun export(src: String, q: Quality) {
        val out = mExportDir
        if (!out.exists()) {
            out.mkdirs()
        }
        val dest = when (q) {
            Quality.MEDIUM -> "${out.path}/medium.gif"
            Quality.HIGH -> "${out.path}/high.gif"
            else -> "${out.path}/low.gif"
        }
        val df = File(dest)
        if (df.exists()) {
            df.delete()
        }
        val size = calculateSize(q)
        val time = System.currentTimeMillis()
        val ret =
            AlFFUtils.exec(
                "ffmpeg -i ${src.replace(
                    " ",
                    "\\ "
                )} -vf scale=${size.x}:${size.y} $dest"
            )
        val cost = System.currentTimeMillis() - time
        Log.i("EditActivity", "exec cost $cost, ret=$ret")
        val file = File(dest)
        mSizeVec[q.ordinal] = if (file.exists()) file.size() else 0L
    }

    private fun calculateSize(q: Quality): Point {
        val dest = when (q) {
            Quality.MEDIUM -> Point(256, 256)
            Quality.HIGH -> Point(512, 512)
            else -> Point(128, 128)
        }
        val point = Point()
        val r0 = vSize.x / vSize.y.toFloat()
        val r1 = dest.x / dest.y.toFloat()
        if (r0 > r1) {
            point.x = dest.x
            point.y = (point.x / r0).toInt()
        } else {
            point.y = dest.y
            point.x = (point.y * r0).toInt()
        }
        return point
    }

    private fun exportDone() {
        dialog?.dismiss()
        val items = resources.getStringArray(R.array.array_export_items)
        items[Quality.LOW.ordinal] += "（${mSizeVec[Quality.LOW.ordinal] * 100 / 1024 / 1024 / 100f}MB）"
        items[Quality.MEDIUM.ordinal] += "（${mSizeVec[Quality.MEDIUM.ordinal] * 100 / 1024 / 1024 / 100f}MB）"
        items[Quality.HIGH.ordinal] += "（${mSizeVec[Quality.HIGH.ordinal] * 100 / 1024 / 1024 / 100f}MB）"
        dialog = AlertDialog.Builder(this)
            .setTitle(R.string.msg_choose_export_quality)
            .setItems(items) { dialog, which ->
                val file = when (which) {
                    Quality.HIGH.ordinal -> File(mExportDir, "high.gif")
                    Quality.MEDIUM.ordinal -> File(mExportDir, "medium.gif")
                    else -> File(mExportDir, "low.gif")
                }
                save(file)
            }
            .show()
    }

    private fun save(file: File) {
        thread(start = true) {
            val dest = File("${mInputPath}.gif")
            dest.delete()
            file.renameTo(dest)
            runOnUiThread {
                MediaScannerConnection.scanFile(
                    this@EditActivity,
                    arrayOf(dest.path),
                    arrayOf("image/gif")
                ) { _: String, uri: Uri ->
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = uri
                    sendBroadcast(mediaScanIntent)
                }
                Toast.makeText(
                    this@EditActivity,
                    "${resources.getString(R.string.saved_to_dcim)}（${dest.path}）",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        player?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
        dialog?.dismiss()
    }

    private enum class Quality {
        LOW,
        MEDIUM,
        HIGH
    }
}