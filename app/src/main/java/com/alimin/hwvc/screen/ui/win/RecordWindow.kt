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

    private val rectF = RectF()
    private var closeBtn: View? = null
    override fun initView() {
        closeBtn = findViewById(R.id.closeBtn)
        closeBtn?.setOnClickListener { dismiss() }
    }

    fun setRectF(rectF: RectF) {
        rectF.set(rectF)
    }

    override fun layoutType(): Int = ViewGroup.LayoutParams.WRAP_CONTENT
    override fun layoutGravity(): Int =  Gravity.END or Gravity.BOTTOM
}