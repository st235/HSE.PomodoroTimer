package st235.com.github.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import st235.com.github.pomodoro.utils.dp

class CircularProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val ANGLE_FULL_CIRCLE = 360F
        const val ANGLE_OFFSET = 90F

        const val ANGLE_TO_DEGREE_RATION = Math.PI / 180F
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bounds = RectF()

    @ColorInt
    private var backgroundProgressColor: Int = Color.BLACK
    set(newValue) {
        field = newValue
        invalidate()
    }

    @ColorInt
    private var foregroundProgressColor: Int = Color.GRAY
    set(newValue) {
        field = newValue
        invalidate()
    }

    @Px
    private var progressIndicatorSize: Float = 0F

    @get:Px
    private var progressBarThickness: Float
    get() {
        return paint.strokeWidth
    }
    set(newValue) {
        paint.strokeWidth = newValue
        invalidate()
    }

    @FloatRange(from = 0.0, to = 1.0)
    public var progress: Float = 0F
    set(newValue) {
        if (newValue < 0 || newValue > 1) {
            throw IllegalArgumentException("$newValue is outside of valid range [0, 1]")
        }
        field = newValue
        invalidate()
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)

        backgroundProgressColor = typedArray.getColor(R.styleable.CircularProgressBar_cpb_progressBackgroundColor, Color.GRAY)
        foregroundProgressColor = typedArray.getColor(R.styleable.CircularProgressBar_cpb_progressBarColor, Color.BLACK)

        progressBarThickness = typedArray.getDimension(R.styleable.CircularProgressBar_cpb_progressBarThickness, 4F.dp)
        progressIndicatorSize = typedArray.getDimension(R.styleable.CircularProgressBar_cpb_progressIndicatorSize, 10F.dp)

        progress = typedArray.getFloat(R.styleable.CircularProgressBar_cpb_progress, 0F)

        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = backgroundProgressColor

        val centerX = bounds.centerX()
        val centerY = bounds.centerY()

        val radius = min(bounds.width(), bounds.height()) / 2f
        canvas.drawCircle(centerX, centerY, radius, paint)

        paint.color = foregroundProgressColor
        val progressAngle = ANGLE_FULL_CIRCLE * progress

        canvas.drawArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            ANGLE_FULL_CIRCLE - ANGLE_OFFSET, progressAngle, false, paint
        )

        val finalAngle = (ANGLE_FULL_CIRCLE - progressAngle + ANGLE_OFFSET) % ANGLE_FULL_CIRCLE

        val finalX = radius * cos(finalAngle * ANGLE_TO_DEGREE_RATION).toFloat()
        val finalY = radius * sin(finalAngle * ANGLE_TO_DEGREE_RATION).toFloat()

        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(centerX + finalX, centerY - finalY, progressIndicatorSize, paint)
    }
}
