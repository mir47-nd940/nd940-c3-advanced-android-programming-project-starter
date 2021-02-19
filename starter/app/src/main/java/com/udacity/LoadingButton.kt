package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

private const val STROKE_WIDTH = 12f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var _widthSize = 0
    private var _heightSize = 0
    private var _textSize = 60F
    private var _text = context.getString(R.string.button_name)

    private var _currentPercentage: Float = 0F
    private var _targetPercentage: Float = 0F

    private var _loadingAnimationListener: LoadingAnimationListener? = null

    fun setLoadingAnimationListener(listener: LoadingAnimationListener) {
        _loadingAnimationListener = listener
    }

    private var _buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> _text = context.getString(R.string.button_loading)
            ButtonState.Loading -> _text = context.getString(R.string.button_loading)
            ButtonState.Completed -> {
                _text = context.getString(R.string.button_name)
                _currentPercentage = 0F
                _targetPercentage = 0F
            }
        }
        invalidate()
    }

    private val _drawColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val _drawColorDark = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)

    private val _paint = Paint().apply {
        color = _drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private val _paintDark = Paint().apply {
        color = _drawColorDark
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private val _paintWhite = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textSize = _textSize
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0F, 0F, _widthSize.toFloat(), _heightSize.toFloat(), _paint)

        canvas.drawRect(
            0F,
            0F,
            _currentPercentage * _widthSize / 100,
            _heightSize.toFloat(),
            _paintDark
        )

        canvas.drawText(
            _text,
            _widthSize / 2F,
            (_heightSize + _textSize) / 2,
            _paintWhite
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
        _widthSize = w
        _heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        _buttonState = when (_buttonState) {
            ButtonState.Completed -> ButtonState.Clicked
            else -> _buttonState
        }
        return super.performClick()
    }

    fun updateProgress(percentage: Int) {
        if (percentage <= 0) {
            _loadingAnimationListener?.onPartialAnimationComplete()
            return
        }

        _targetPercentage = percentage.toFloat()

        ValueAnimator.ofFloat(_currentPercentage, _targetPercentage).apply {
            duration = 1000
            addUpdateListener {
                _currentPercentage = it.animatedValue as Float
                invalidate()

                if (_currentPercentage >= 100F) {
                    _buttonState = ButtonState.Completed
                    return@addUpdateListener
                }

                if (_currentPercentage >= _targetPercentage) {
                    _loadingAnimationListener?.onPartialAnimationComplete()
                }
            }
            start()
            _buttonState = ButtonState.Loading
        }
    }

    fun interface LoadingAnimationListener {
        fun onPartialAnimationComplete()
    }
}