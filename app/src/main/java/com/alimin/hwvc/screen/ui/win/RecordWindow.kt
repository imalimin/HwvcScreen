package com.alimin.hwvc.screen.ui.win

import android.content.Context
import android.graphics.RectF
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.ui.base.BaseWindow

class RecordWindow(ctx: Context) : BaseWindow(ctx) {
    override val layoutResID: Int = R.layout.win_record
    override fun layoutType(): Int = ViewGroup.LayoutParams.WRAP_CONTENT
    override fun layoutGravity(): Int = Gravity.END or Gravity.BOTTOM

    private val rectF = RectF(0f, 0f, 0f, 0f)
    private var listener: ((rectF: RectF) -> Unit)? = null
    override fun initView() {
        findViewById<View>(R.id.closeBtn)?.setOnClickListener { dismiss() }
        findViewById<View>(R.id.selectBtn)?.setOnClickListener {
            this@RecordWindow.dismiss()
            SelectWindow(getContext()).apply {
                setOnEnterListener {
                    this@RecordWindow.show()
                    if (null != it) {
                        rectF.set(it)
                    }
                }
            }
        }
        findViewById<View>(R.id.fullBtn)?.setOnClickListener {
            listener?.invoke(RectF(0f, 0f, 0f, 0f))
        }
        findViewById<View>(R.id.recordBtn)?.setOnClickListener {
            listener?.invoke(rectF)
        }
    }

    fun setOnStartListener(listener: (rectF: RectF) -> Unit) {
        this.listener = listener
    }
}