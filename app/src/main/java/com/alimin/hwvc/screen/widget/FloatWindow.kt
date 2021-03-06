package com.alimin.hwvc.screen.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import com.alimin.hwvc.screen.R


class FloatWindow(private val ctx: Context) : View.OnClickListener {
    private var wm: WindowManager? = null
    private var view: View? = null
    private var lp: WindowManager.LayoutParams? = null
    private val size = Point()

    private var cropView: AlWinView? = null
    private var fullBtn: View? = null
    private var recordBtn: CheckBox? = null
    private var closeBtn: View? = null
    private var optLayout: View? = null
    private var adjustSize: Int = 0
    private var animator: ValueAnimator? = null
    private var onChangeListener: ((view: View) -> Unit)? = null

    init {
        wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm?.defaultDisplay?.getRealSize(size)
        adjustSize = (size.x * 0.2f).toInt()
        lp = WindowManager.LayoutParams().apply {
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN
            gravity = Gravity.LEFT.or(Gravity.TOP)
            width = size.x
            height = size.x
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }
        view = LayoutInflater.from(ctx).inflate(R.layout.win_float, null)
        view?.keepScreenOn = true
        view?.isClickable = true
        cropView = view?.findViewById(R.id.cropView)
        fullBtn = view?.findViewById(R.id.fullBtn)
        recordBtn = view?.findViewById(R.id.recordBtn)
        closeBtn = view?.findViewById(R.id.closeBtn)
        optLayout = view?.findViewById(R.id.optLayout)
        fullBtn?.setOnClickListener(this)
        closeBtn?.setOnClickListener(this)
        cropView?.setOnChangeListener {
            val rectF = cropView!!.getCropRectF()
            lp?.x = rectF.left.toInt()
            lp?.y = rectF.top.toInt()
            Log.e("aliminabc", "OnChange ${lp?.x}, ${lp?.y}")
            lp?.width = rectF.width().toInt()
            lp?.height = rectF.height().toInt() + optLayout!!.measuredHeight
            wm?.updateViewLayout(view!!, lp)
            onChangeListener?.invoke(it)
        }
        recordBtn?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                closeBtn?.isClickable = false
                fullBtn?.isClickable = false
                flash()
                onStartListener?.invoke()
            } else {
                onCloseListener?.invoke()
                dismiss()
            }
        }
    }

    private fun flash() {
        animator = ValueAnimator.ofFloat(1f, 0.5f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 10000
            repeatMode = ValueAnimator.REVERSE
            duration = 1000
            addUpdateListener {
                recordBtn?.alpha = it.animatedValue as Float
            }
        }
        animator?.start()
    }

    fun show() {
        view?.visibility = View.VISIBLE
        wm?.addView(view, lp)
    }

    fun dismiss() {
        view?.visibility = View.GONE
        wm?.removeView(view)
        animator?.cancel()
    }

    fun getRect(): RectF = cropView!!.getCropRectFNor()

    override fun onClick(v: View) {
        when (v.id) {
            R.id.closeBtn -> {
                onCloseListener?.invoke()
                dismiss()
            }
            R.id.fullBtn -> {
                onFullListener?.invoke()
            }
        }
    }

    private var onStartListener: (() -> Unit)? = null
    fun setOnStartListener(l: () -> Unit) {
        onStartListener = l
    }

    private var onCloseListener: (() -> Unit)? = null
    fun setOnCloseListener(l: () -> Unit) {
        onCloseListener = l
    }

    private var onFullListener: (() -> Unit)? = null
    fun setOnFullListener(l: () -> Unit) {
        onFullListener = l
    }

    fun setClickable(enable: Boolean) {
        cropView?.isClickable = enable
    }

    fun setOnChangeListener(listener: (view: View) -> Unit) {
        this.onChangeListener = listener
    }

    companion object {
        const val LOC_IDL = 0
        const val LOC_LT = 1
        const val LOC_RT = 2
        const val LOC_RB = 3
        const val LOC_LB = 4
    }
}