package com.alimin.hwvc.screen.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.microsoft.officeuifabric.util.statusBarHeight

class AlWinView : View {
    enum class Loc { LT, LB, RB, RT, C }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var strokeWidth: Float = 3f
    private var loc: Loc? = null
    private var onChangeListener: ((view: View) -> Unit)? = null
    private var adjustSize: Int = 0
    private val lastTouchPointF = PointF()
    private val lt = PointF()
    private val rb = PointF()
    private val dispSize = Point()
    private var minSize: Int = 128
    private var statusBarHeight: Int = 0

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize()
    }

    private fun align16(v: Int): Int {
        return (v shr 4) + (v and 0xF shr 3) shl 4
    }

    private fun initialize() {
        statusBarHeight = context.statusBarHeight
        paint.color = Color.LTGRAY
        paint.strokeWidth = strokeWidth

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(dispSize)
        adjustSize = dispSize.x / 10
        minSize = adjustSize * 3
        post {
            queryLoc()
        }
    }

    private fun queryLoc() {
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        lt.x = loc[0].toFloat()
        lt.y = loc[1].toFloat()
        rb.x = lt.x + measuredWidth
        rb.y = lt.y + measuredHeight
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val ret = super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                loc = null
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                lastTouchPointF.set(event.rawX, event.rawY)
                loc = getLoc(this, event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (null != loc) {
                    val minSize = this.minSize
                    val delta = PointF(
                        event.rawX - lastTouchPointF.x,
                        event.rawY - lastTouchPointF.y
                    )
                    lastTouchPointF.set(event.rawX, event.rawY)
                    checkDelta(delta)
                    val dx = delta.x
                    val dy = delta.y
                    when (loc) {
                        Loc.LT -> {
                            lt.offset(dx, dy)
                            lt.x = Math.min(lt.x, rb.x - minSize)
                            lt.y = Math.min(lt.y, rb.y - minSize)
                        }
                        Loc.RT -> {
                            rb.offset(dx, 0f)
                            lt.offset(0f, dy)
                            rb.x = Math.max(lt.x + minSize, rb.x)
                            lt.y = Math.min(lt.y, rb.y - minSize)
                        }
                        Loc.RB -> {
                            rb.offset(dx, dy)
                            rb.x = Math.max(lt.x + minSize, rb.x)
                            rb.y = Math.max(lt.y + minSize, rb.y)
                        }
                        Loc.LB -> {
                            lt.offset(dx, 0f)
                            rb.offset(0f, dy)
                            lt.x = Math.min(lt.x, rb.x - minSize)
                            rb.y = Math.max(lt.y + minSize, rb.y)
                        }
                        else -> {
                            lt.offset(dx, dy)
                            rb.offset(dx, dy)
                        }
                    }

                    onChangeListener?.invoke(this)
                    return true
                }
            }
        }
        return ret
    }

    private fun checkDelta(delta: PointF) {
        val rect = RectF()
        if (lt.x + delta.x < 0) {
            rect.left = -lt.x
        }
        if (rb.x + delta.x > dispSize.x) {
            rect.right = dispSize.x - rb.x
        }
        if (lt.y + delta.x < statusBarHeight) {
            rect.top = statusBarHeight - lt.y
        }
        if (rb.y + delta.x > dispSize.y) {
            rect.bottom = dispSize.y - rb.y
        }
        delta.x =
            if (0f != rect.left) rect.left else if (0f != rect.right) rect.right else delta.x
        delta.y =
            if (0f != rect.top) rect.top else if (0f != rect.bottom) rect.bottom else delta.y
    }

    private fun getLoc(v: View, event: MotionEvent): Loc {
        if (event.x >= 0 && event.x <= adjustSize && event.y >= 0 && event.y <= adjustSize) {
            return Loc.LT
        }
        if (event.x <= v.width && v.width - event.x <= adjustSize && event.y >= 0 && event.y <= adjustSize) {
            return Loc.RT
        }
        if (event.x <= v.width && v.width - event.x <= adjustSize
            && event.y <= v.height && v.height - event.y <= adjustSize
        ) {
            return Loc.RB
        }
        if (event.x >= 0 && event.x <= adjustSize
            && event.y <= v.height && v.height - event.y <= adjustSize
        ) {
            return Loc.LB
        }
        return Loc.C
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(0f, 0f, measuredWidth.toFloat(), 0f, paint)
        canvas?.drawLine(
            measuredWidth.toFloat(),
            0f,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            paint
        )
        canvas?.drawLine(
            0f,
            measuredHeight.toFloat(),
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            paint
        )
        canvas?.drawLine(
            0f,
            0f, 0f,
            measuredHeight.toFloat(),
            paint
        )
    }

    private fun getRawCropRectF(): RectF = RectF(
        lt.x,
        lt.y,
        rb.x,
        rb.y
    )

    fun getCropRectF(): RectF {
        val rectF = getRawCropRectF()
        rectF.top -= statusBarHeight
        rectF.bottom -= statusBarHeight
        return rectF
    }

    fun getCropRectFNor(): RectF {
        val rectF = getRawCropRectF()
        Log.i("getCropRectFNor", "$rectF")
        rectF.left = (rectF.left + strokeWidth / 2.0f) / dispSize.x.toFloat()
        rectF.top = (rectF.top + strokeWidth / 2.0f) / dispSize.y.toFloat()
        rectF.right = (rectF.right - strokeWidth / 2.0f) / dispSize.x.toFloat()
        rectF.bottom = (rectF.bottom - strokeWidth / 2.0f) / dispSize.y.toFloat()

        rectF.left = rectF.left * 2 - 1
        rectF.top = 1 - rectF.top * 2
        rectF.right = rectF.right * 2 - 1
        rectF.bottom = 1 - rectF.bottom * 2
        return rectF
    }

    fun setOnChangeListener(listener: (view: View) -> Unit) {
        this.onChangeListener = listener
    }
}