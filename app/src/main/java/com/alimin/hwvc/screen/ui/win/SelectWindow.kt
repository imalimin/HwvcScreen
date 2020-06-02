package com.alimin.hwvc.screen.ui.win

import android.content.Context
import android.graphics.RectF
import android.util.Log
import android.view.View
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.ui.base.BaseWindow
import com.alimin.hwvc.screen.widget.CropView

class SelectWindow(ctx: Context) : BaseWindow(ctx) {
    override val layoutResID: Int = R.layout.win_select

    private var cropView: CropView? = null
    private var enterBtn: View? = null
    private var listener: ((rectF: RectF?) -> Unit)? = null
    override fun initView() {
        cropView = findViewById(R.id.cropView)
        enterBtn = findViewById(R.id.enterBtn)
        enterBtn?.setOnClickListener { enter() }
        cropView?.setOnChangeListener {
            //            keepCenter()
        }
    }

    private fun keepCenter() {
        val rectF = cropView!!.getCropRectF()

        enterBtn?.x = (rectF.right - rectF.left) / 2 * cropView!!.width
        enterBtn?.y = (2 - rectF.top + rectF.bottom) / 2 * cropView!!.height
        enterBtn?.requestLayout()
    }

    private fun enter() {
        listener?.invoke(cropView?.getCropRectFInWin())
        dismiss()
    }

    fun setOnEnterListener(listener: (rectF: RectF?) -> Unit) {
        this.listener = listener
    }
}