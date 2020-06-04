package com.alimin.hwvc.screen.ui.win

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.ui.base.BaseWindow
import com.lmy.common.ext.getNavigationBarHeight
import com.lmy.common.ext.getStatusBarHeight

class StopWindow(ctx: Context) : BaseWindow(ctx) {
    override val layoutResID: Int = R.layout.win_record_stop
    override fun layoutType(): Int = ViewGroup.LayoutParams.WRAP_CONTENT
    override fun layoutGravity(): Int = Gravity.END or Gravity.BOTTOM

    private var rectF = RectF()
    private var animator: ValueAnimator? = null
    private var stopBtn: CheckBox? = null
    private var stopLayout: View? = null
    private var listener: (() -> Unit)? = null
    override fun initView() {
        stopLayout = findViewById(R.id.stopLayout)
        stopBtn = findViewById(R.id.stopBtn)
        stopBtn?.isChecked = false
        flash()
        stopBtn?.setOnClickListener {
            listener?.invoke()
        }
        stopLayout?.post { location() }
    }

    private fun location() {
        val lt = PointF(
            (1f + rectF.left) * getScreenSize().x / 2,
            (1f - rectF.top) * getScreenSize().y / 2 - getContext().getStatusBarHeight()
        )
        val rb = PointF(
            (1f - rectF.right) * getScreenSize().x / 2,
            (1f + rectF.bottom) * getScreenSize().y / 2 - getContext().getNavigationBarHeight()
        )
        val lb = PointF(lt.x, rb.y)
        val rt = PointF(rb.x, lt.y)
        var gravity = layoutGravity()
        if (rb.x < stopLayout!!.measuredWidth && rb.y < stopLayout!!.measuredHeight) {
            gravity = Gravity.START or Gravity.BOTTOM
            if (lb.x < stopLayout!!.measuredWidth && lb.y < stopLayout!!.measuredHeight) {
                gravity = Gravity.END or Gravity.TOP
                if (rt.x < stopLayout!!.measuredWidth && rt.y < stopLayout!!.measuredHeight) {
                    gravity = Gravity.START or Gravity.TOP
                    if (lt.x < stopLayout!!.measuredWidth && lt.y < stopLayout!!.measuredHeight) {
                        gravity = Gravity.END or Gravity.BOTTOM
                    }
                }
            }
        }
        setGravity(gravity)
    }

    private fun flash() {
        animator = ValueAnimator.ofFloat(1f, 0.5f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 10000
            repeatMode = ValueAnimator.REVERSE
            duration = 1000
            addUpdateListener {
                stopBtn?.alpha = it.animatedValue as Float
            }
        }
        animator?.start()
    }

    fun setOnStopListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun setRectF(rectF: RectF) {
        this.rectF.set(rectF)
    }
}