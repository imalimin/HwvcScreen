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
        paint.color = Color.LTGRAY
        paint.strokeWidth = strokeWidth

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(dispSize)
        adjustSize = (dispSize.x * 0.2f).toInt()
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
                    val dx = event.rawX - lastTouchPointF.x
                    val dy = event.rawY - lastTouchPointF.y
                    lastTouchPointF.set(event.rawX, event.rawY)
                    when (loc) {
                        Loc.LT -> {
                            lt.offset(dx, dy)
                        }
                        Loc.RT -> {
                            rb.offset(dx, 0f)
                            lt.offset(0f, dy)
                        }
                        Loc.RB -> {
                            rb.offset(dx, dy)
                        }
                        Loc.LB -> {
                            lt.offset(dx, 0f)
                            rb.offset(0f, dy)
                        }
                        else -> {
                            lt.offset(dx, dy)
                            rb.offset(dx, dy)
                        }
                    }

//                    checkBound()
                    onChangeListener?.invoke(this)
                    return true
                }
            }
        }
        return ret
    }

    private fun checkBound() {
        lt.x = Math.max(0f, lt.x)
        lt.y = Math.max(0f, lt.y)

        rb.x = Math.min(measuredWidth.toFloat(), rb.x)
        rb.y = Math.min(measuredHeight.toFloat(), rb.y)
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
        align16(lt.x.toInt()).toFloat(),
        align16(lt.y.toInt()).toFloat(),
        align16(rb.x.toInt()).toFloat(),
        align16(rb.y.toInt()).toFloat()
    )

    fun getCropRectF(): RectF {
        val rectF = getRawCropRectF()
        val height = context.statusBarHeight
        rectF.top -= height
        rectF.bottom -= height
        return rectF
    }

    fun getCropRectFNor(): RectF {
        val rectF = getRawCropRectF()
        Log.i("getCropRectFNor", "$rectF")
        rectF.left = rectF.left / dispSize.x.toFloat()
        rectF.top = rectF.top / dispSize.y.toFloat()
        rectF.right = rectF.right / dispSize.x.toFloat()
        rectF.bottom = rectF.bottom / dispSize.y.toFloat()

        rectF.left = rectF.left * 2 - 1
        rectF.top = 1 - rectF.top * 2
        rectF.right = rectF.right * 2 - 1
        rectF.bottom = 1 - rectF.bottom * 2
        return rectF
    }

    fun setOnChangeListener(listener: (view: View) -> Unit) {
        this.onChangeListener = listener
    }

    companion object {
        const val MIN_CROP_SIZE = 64
    }
}