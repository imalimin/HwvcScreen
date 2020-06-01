package com.alimin.hwvc.screen.ui.base

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.WindowManager
import androidx.annotation.IdRes
import com.alimin.hwvc.screen.AlDisplayService

abstract class BaseWindow(private val ctx: Context) {
    protected abstract val layoutResID: Int
    protected abstract fun initView()

    private var wm: WindowManager? = null
    private val wSize = Point()
    private var view: View? = null
    private var lp: WindowManager.LayoutParams? = null

    init {
        wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm?.defaultDisplay?.getRealSize(wSize)

        lp = WindowManager.LayoutParams().apply {
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN
            gravity = Gravity.LEFT.or(Gravity.TOP)
            width = wSize.x
            height = wSize.y
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }
        Handler(Looper.getMainLooper()).post {
            setup()
        }
    }

    private fun setup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
            ctx.startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                ))
            return
        }
        view = LayoutInflater.from(ctx).inflate(layoutResID, null)
        initView()
        view?.isClickable = true
        show()
    }

    fun show() {
        view?.visibility = View.VISIBLE
        wm?.addView(view, lp)
    }

    fun dismiss() {
        view?.visibility = View.GONE
        wm?.removeView(view)
    }

    fun <T : View> findViewById(@IdRes id: Int): T? {
        return if (id == NO_ID) {
            null
        } else view?.findViewById(id)
    }

    fun getContext(): Context = ctx
}