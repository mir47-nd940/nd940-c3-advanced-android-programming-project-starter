package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val STROKE_WIDTH = 12f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonTextSize = 60F
    private var textInactive = context.getString(R.string.button_name)
    private var textActive = context.getString(R.string.button_loading)
    private var textDisplay = textInactive
    private var currentPercentage: Float = 0F
    private var targetPercentage: Float = 0F
    private var loadingAnimationListener: LoadingAnimationListener? = null
    private var textActiveWidth = 0
    private var loadingCircle = RectF()
    private var loadingBackgroundColor = 0
    private var loadingProgressColor = 0
    private var loadingCircleColor = 0
    private val bounds = Rect()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> textDisplay = textActive
            ButtonState.Loading -> textDisplay = textActive
            ButtonState.Completed -> {
                textDisplay = textInactive
                currentPercentage = 0F
                targetPercentage = 0F
            }
        }
        invalidate()
    }

    private val _paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
        textSize = buttonTextSize
        textAlign = Paint.Align.CENTER
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            loadingProgressColor = getColor(R.styleable.LoadingButton_loadingProgressColor, 0)
            loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        _paint.color = loadingBackgroundColor
        canvas.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), _paint)
        _paint.color = loadingProgressColor
        canvas.drawRect(0F, 0F, currentPercentage * widthSize / 100, heightSize.toFloat(), _paint)
        _paint.color = loadingCircleColor
        canvas.drawArc(loadingCircle, 0F, currentPercentage * 360 / 100, true, _paint)
        _paint.color = Color.WHITE
        canvas.drawText(textDisplay, widthSize / 2F, (heightSize + buttonTextSize) / 2, _paint)
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

        _paint.getTextBounds(textActive, 0, textActive.length, bounds)
        textActiveWidth = bounds.width()
        val offset = textActiveWidth + ((widthSize - textActiveWidth) / 2F)
        val quarterHeight = heightSize / 4F
        loadingCircle.left = offset
        loadingCircle.top = quarterHeight
        loadingCircle.right = offset + (quarterHeight * 2)
        loadingCircle.bottom = quarterHeight * 3
    }

    override fun performClick(): Boolean {
        buttonState = when (buttonState) {
            ButtonState.Completed -> ButtonState.Clicked
            else -> buttonState
        }
        return super.performClick()
    }

    fun animateTo(percentage: Int) {
        buttonState = ButtonState.Loading

        if (percentage.toFloat() == targetPercentage) {
            loadingAnimationListener?.onTargetReached()
            return
        }

        targetPercentage = percentage.toFloat()

        ValueAnimator.ofFloat(currentPercentage, targetPercentage).apply {
            duration = 1000
            addUpdateListener {
                currentPercentage = it.animatedValue as Float
                invalidate()

                if (currentPercentage >= 100F) {
                    buttonState = ButtonState.Completed
                    return@addUpdateListener
                }

                if (currentPercentage >= targetPercentage) {
                    loadingAnimationListener?.onTargetReached()
                }
            }
            start()
        }
    }

    fun reset() {
        buttonState = ButtonState.Completed
    }

    fun setLoadingAnimationListener(listener: LoadingAnimationListener) {
        loadingAnimationListener = listener
    }

    fun interface LoadingAnimationListener {
        fun onTargetReached()
    }
}