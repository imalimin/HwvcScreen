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
import android.util.Log
import android.view.*
import android.view.View.NO_ID
import androidx.annotation.IdRes

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
        Handler(Looper.getMainLooper()).post {
            setup()
        }
    }

    private fun setup() {
        view = LayoutInflater.from(ctx).inflate(layoutResID, null)
        view?.isClickable = true
        initView()
        lp = WindowManager.LayoutParams().apply {
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN
            gravity = layoutGravity()
            width = layoutType()
            height = layoutType()
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(ctx)) {
            ctx.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                )
            )
            return
        }
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
    open fun layoutType(): Int = ViewGroup.LayoutParams.MATCH_PARENT
    open fun layoutGravity(): Int = Gravity.START or Gravity.TOP
}