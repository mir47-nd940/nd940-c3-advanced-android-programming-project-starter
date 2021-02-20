package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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
    private var _textInactive = context.getString(R.string.button_name)
    private var _textActive = context.getString(R.string.button_loading)
    private var _textDisplay = _textInactive
    private var _currentPercentage: Float = 0F
    private var _targetPercentage: Float = 0F
    private var _loadingAnimationListener: LoadingAnimationListener? = null
    private var _textActiveWidth = 0
    private lateinit var _loadingCircle: RectF

    fun setLoadingAnimationListener(listener: LoadingAnimationListener) {
        _loadingAnimationListener = listener
    }

    private var _buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> _textDisplay = _textInactive
            ButtonState.Loading -> _textDisplay = _textActive
            ButtonState.Completed -> {
                _textDisplay = _textInactive
                _currentPercentage = 0F
                _targetPercentage = 0F
            }
        }
        invalidate()
    }

    private val _drawColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val _drawColorDark = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
    private val _drawColorAccent = ResourcesCompat.getColor(resources, R.color.colorAccent, null)

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

    private val _paintAccent = Paint().apply {
        color = _drawColorAccent
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private val _paintText = Paint().apply {
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
            _textDisplay,
            _widthSize / 2F,
            (_heightSize + _textSize) / 2,
            _paintText
        )

        canvas.drawArc(
            _loadingCircle,
            0F,
            _currentPercentage * 360 / 100,
            true,
            _paintAccent
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

        val bounds = Rect()
        _paintText.getTextBounds(_textActive, 0, _textActive.length, bounds)
        _textActiveWidth = bounds.width()
        val offset = _textActiveWidth + ((_widthSize - _textActiveWidth) / 2F)
        val quarterHeight = _heightSize / 4F
        _loadingCircle = RectF(
            offset,
            quarterHeight,
            offset + (quarterHeight * 2),
            quarterHeight * 3
        )

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
        _buttonState = ButtonState.Loading

        if (percentage.toFloat() == _targetPercentage) {
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
        }
    }

    fun interface LoadingAnimationListener {
        fun onPartialAnimationComplete()
    }
}