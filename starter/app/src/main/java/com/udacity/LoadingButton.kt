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
    private var _loadingCircle = RectF()
    private var loadingBackgroundColor = 0
    private var loadingProgressColor = 0
    private var loadingCircleColor = 0
    private val bounds = Rect()

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

    private val _paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
        textSize = _textSize
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
        canvas.drawRect(0F, 0F, _widthSize.toFloat(), _heightSize.toFloat(), _paint)
        _paint.color = loadingProgressColor
        canvas.drawRect(0F, 0F, _currentPercentage * _widthSize / 100, _heightSize.toFloat(), _paint)
        _paint.color = loadingCircleColor
        canvas.drawArc(_loadingCircle, 0F, _currentPercentage * 360 / 100, true, _paint)
        _paint.color = Color.WHITE
        canvas.drawText(_textDisplay, _widthSize / 2F, (_heightSize + _textSize) / 2, _paint)
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

        _paint.getTextBounds(_textActive, 0, _textActive.length, bounds)
        _textActiveWidth = bounds.width()
        val offset = _textActiveWidth + ((_widthSize - _textActiveWidth) / 2F)
        val quarterHeight = _heightSize / 4F
        _loadingCircle.left = offset
        _loadingCircle.top = quarterHeight
        _loadingCircle.right = offset + (quarterHeight * 2)
        _loadingCircle.bottom = quarterHeight * 3
    }

    override fun performClick(): Boolean {
        _buttonState = when (_buttonState) {
            ButtonState.Completed -> ButtonState.Clicked
            else -> _buttonState
        }
        return super.performClick()
    }

    fun animateTo(percentage: Int) {
        _buttonState = ButtonState.Loading

        if (percentage.toFloat() == _targetPercentage) {
            _loadingAnimationListener?.onTargetReached()
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
                    _loadingAnimationListener?.onTargetReached()
                }
            }
            start()
        }
    }

    fun interface LoadingAnimationListener {
        fun onTargetReached()
    }
}