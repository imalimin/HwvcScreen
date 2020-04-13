package com.alimin.hwvc.screen.widget

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
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
    private var optLayout: View? = null
    private var adjustSize: Int = 0

    init {
        wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(size)
        adjustSize = (size.x * 0.2f).toInt()
        lp = WindowManager.LayoutParams().apply {
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN
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
        optLayout = view?.findViewById(R.id.optLayout)
        startBtn?.setOnClickListener(this)
        closeBtn?.setOnClickListener(this)
        cropView?.setFixAlign(true)
        cropView?.setOnChangeListener {
            adjustOptLayout()
        }
    }

    fun show() {
        view?.visibility = View.VISIBLE
        wm?.addView(view, lp)
        view?.post { adjustOptLayout() }
    }

    fun dismiss() {
        view?.visibility = View.GONE
        wm?.removeView(view)
    }

    fun getRect(): RectF {
        val rectF = cropView!!.getCropRectFInDisplay(true)
//        val width = size.x * rectF.width() / 2
//        val height = size.y * rectF.height() / 2
//        rectF.right = rectF.left + width / 16 * 32 / size.x.toFloat()
//        rectF.bottom = rectF.top + height / 16 * 32 / size.y.toFloat()
        return rectF
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.startBtn -> {
//                val loc = IntArray(2)
//                cropView?.getLocationOnScreen(loc)
//                val size = cropView!!.getCropSizeOfPixels()
//                lp?.x = loc[0]
//                lp?.y = loc[1]
//                lp?.width = size.x
//                lp?.height = size.y + optLayout!!.measuredHeight
//                wm?.updateViewLayout(view, lp)
                onStartListener?.invoke()
            }
            R.id.closeBtn -> {
                onCloseListener?.invoke()
                dismiss()
            }
        }
    }

    private fun adjustOptLayout() {
        val rectF = cropView!!.getCropRectF()
        val point = PointF(
            (rectF.right + 1f) * (cropView!!.measuredWidth / 2f),
            (1f - rectF.bottom) * (cropView!!.measuredHeight / 2f)
        )
        point.x = point.x - optLayout!!.measuredWidth
        point.y = point.y + 12
        point.x = Math.max(0f, point.x)
        point.y = Math.max(0f, point.y)
        point.x = Math.min(cropView!!.measuredWidth.toFloat() - optLayout!!.measuredWidth, point.x)
        point.y =
            Math.min(cropView!!.measuredHeight.toFloat() - optLayout!!.measuredHeight, point.y)

        val lp = optLayout!!.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = point.x.toInt()
        lp.topMargin = point.y.toInt()
        optLayout!!.requestLayout()
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