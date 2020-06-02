package com.alimin.hwvc.screen.ui.win

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.CheckBox
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.ui.base.BaseWindow

class StopWindow(ctx: Context) : BaseWindow(ctx) {
    override val layoutResID: Int = R.layout.win_record_stop
    override fun layoutType(): Int = ViewGroup.LayoutParams.WRAP_CONTENT
    override fun layoutGravity(): Int = Gravity.END or Gravity.BOTTOM

    private var stopBtn: CheckBox? = null
    private var listener: (() -> Unit)? = null
    override fun initView() {
        stopBtn = findViewById(R.id.stopBtn)
        stopBtn?.isChecked = true
        stopBtn?.setOnClickListener {
            listener?.invoke()
        }
    }

    fun setOnStopListener(listener: () -> Unit) {
        this.listener = listener
    }
}