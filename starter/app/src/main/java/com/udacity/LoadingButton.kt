package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

private const val STROKE_WIDTH = 12f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    var currentPercentage: Float = 0F
    var targetPercentage: Float = 0F

    private var loadingAnimationListener: LoadingAnimationListener? = null

    fun setLoadingAnimationListener(listener: LoadingAnimationListener) {
        loadingAnimationListener = listener
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val drawColorDark = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)

    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.FILL // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private val paintDark = Paint().apply {
        color = drawColorDark
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.FILL // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
        canvas.drawRect(
            0F,
            0F,
            currentPercentage * widthSize / 100,
            heightSize.toFloat(),
            paintDark
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun updateProgress(percentage: Int) {
        if (percentage <= 0) {
            loadingAnimationListener?.onPartialAnimationComplete()
            return
        }

        targetPercentage = percentage.toFloat()

        println("mmmmm animating: current=$currentPercentage target=$targetPercentage")

        ValueAnimator.ofFloat(currentPercentage, targetPercentage).apply {
            duration = 1000
            addUpdateListener {
                currentPercentage = it.animatedValue as Float
                println("mmmmm currentPercentage=$currentPercentage targetPercentage=$targetPercentage")
                invalidate()

                if (currentPercentage < 100F && currentPercentage >= targetPercentage) {
                    loadingAnimationListener?.onPartialAnimationComplete()
                }
            }
            start()
        }
    }

    fun interface LoadingAnimationListener {
        fun onPartialAnimationComplete()
    }
}