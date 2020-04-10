package com.alimin.hwvc.screen.widget

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.*
import com.alimin.hwvc.screen.R
import com.lmy.hwvcnative.widget.AlCropView


class FloatWindow(private val ctx: Context) : View.OnClickListener {
    private var wm: WindowManager? = null
    private var view: View? = null
    private var lp: WindowManager.LayoutParams? = null
    private val size = Point()

    private var cropView: AlCropView? = null
    private var startBtn: View? = null
    private var closeBtn: View? = null
    private var adjustSize: Int = 0

    init {
        wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(size)
        adjustSize = (size.x * 0.2f).toInt()
        lp = WindowManager.LayoutParams().apply {
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            gravity = Gravity.LEFT.or(Gravity.TOP)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }
        view = LayoutInflater.from(ctx).inflate(R.layout.win_float, null)
        cropView = view?.findViewById(R.id.cropView)
        startBtn = view?.findViewById(R.id.startBtn)
        closeBtn = view?.findViewById(R.id.closeBtn)
        startBtn?.setOnClickListener(this)
        closeBtn?.setOnClickListener(this)
        cropView?.setFixAlign(true)
    }

    fun show() {
        view?.visibility = View.VISIBLE
        wm?.addView(view, lp)
    }

    fun dismiss() {
        view?.visibility = View.GONE
        wm?.removeView(view)
    }

    fun getRect(): RectF {
        val rectF = cropView!!.getCropRectFInDisplay()
        val width = size.x * rectF.width() / 2
        val height = size.y * rectF.height() / 2
        rectF.right = rectF.left + width / 16 * 32 / size.x.toFloat()
        rectF.bottom = rectF.top + height / 16 * 32 / size.y.toFloat()
        return rectF
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.startBtn -> {
                onStartListener?.invoke()
            }
            R.id.closeBtn -> {
                onCloseListener?.invoke()
                dismiss()
            }
        }
    }

    private fun getLoc(v: View, event: MotionEvent): Int {
        if (event.x >= 0 && event.x <= adjustSize && event.y >= 0 && event.y <= adjustSize) {
            return LOC_LT
        }
        if (event.x <= v.width && v.width - event.x <= adjustSize && event.y >= 0 && event.y <= adjustSize) {
            return LOC_RT
        }
        if (event.x <= v.width && v.width - event.x <= adjustSize
            && event.y <= v.height && v.height - event.y <= adjustSize
        ) {
            return LOC_RB
        }
        if (event.x >= 0 && event.x <= adjustSize
            && event.y <= v.height && v.height - event.y <= adjustSize
        ) {
            return LOC_LB
        }
        return LOC_IDL
    }

    private var onStartListener: (() -> Unit)? = null
    fun setOnStartListener(l: () -> Unit) {
        onStartListener = l
    }

    private var onCloseListener: (() -> Unit)? = null
    fun setOnCloseListener(l: () -> Unit) {
        onCloseListener = l
    }

    companion object {
        const val LOC_IDL = 0
        const val LOC_LT = 1
        const val LOC_RT = 2
        const val LOC_RB = 3
        const val LOC_LB = 4
    }
}