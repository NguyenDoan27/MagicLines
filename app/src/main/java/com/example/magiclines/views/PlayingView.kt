package com.example.magiclines.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.graphics.PathParser
import com.example.magiclines.R
import org.xmlpull.v1.XmlPullParser
import androidx.core.graphics.toColorInt
import com.example.magiclines.models.Level
import kotlin.math.abs
import kotlin.random.Random
import androidx.core.graphics.drawable.toDrawable

class PlayingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val paths = mutableListOf<PathInfo>()
    private var isTouchEnabled = true
    private var listener: OnProcessingCompleteListener? = null

    private var relativeX: Float = 0f
    private var relativeY: Float = 0f
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private val matrix = Matrix()

    private var dialog: Dialog
    private var startTime: Long = 0
    private var directX = -1
    private var directY = -1
    private var star = 0
    private var level: Level? = null

    data class PathInfo(
        val originalPath: Path,
        val originalPathX: Float,
        val originalPathY: Float,
        val path: Path,
        val fillPaint: Paint,
        val strokePaint: Paint?,
        val glowPaint: Paint?,
        val relativeBounds: RectF,
        var initialX: Float,
        var initialY: Float,
        var width: Float,
        var height: Float,
        var directionX: Int = 1,
        var directionY: Int = 1,
        var offsetX: Float = 0f,
        var offsetY: Float = 0f,
        var centerX: Float = 0f,
        var centerY: Float = 0f,
        var x: Float = 0f,
        var y: Float = 0f
    )

    init {
        // Initialize dialog
        dialog = Dialog(context).apply {
            setContentView(R.layout.finishing_dialog)
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }


        isClickable = true
        isFocusable = true
        startTime = System.nanoTime()
    }

     fun parseVectorDrawable( drawableId: Int) {
        val parser = resources.getXml(drawableId)
        var vectorWidth = 0f
        var vectorHeight = 0f

        var eventType = parser.eventType
        val namespace = "http://schemas.android.com/apk/res/android"
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "vector") {
                vectorWidth = parser.getAttributeValue(namespace, "viewportWidth")?.toFloat() ?: vectorWidth
                vectorHeight = parser.getAttributeValue(namespace, "viewportHeight")?.toFloat() ?: vectorHeight
            }
            if (eventType == XmlPullParser.START_TAG && parser.name == "path") {
                val pathData = parser.getAttributeValue(namespace, "pathData")
                val fillColor = parser.getAttributeValue(namespace, "fillColor")?.toColorInt() ?: Color.BLACK
                val strokeColor = parser.getAttributeValue(namespace, "strokeColor")?.toColorInt()
                val strokeWidth = parser.getAttributeValue(namespace, "strokeWidth")?.toFloat() ?: 0f
                val fillType = parser.getAttributeValue(namespace, "fillType")
                if (pathData != null) {
                    val bounds = RectF()
                    val originalPath = PathParser.createPathFromPathData(pathData)
                    val path = Path(originalPath)
                    if (fillType == "evenOdd") {
                        path.setFillType(Path.FillType.EVEN_ODD)
                    } else {
                        path.setFillType(Path.FillType.WINDING)
                    }
                    path.computeBounds(bounds, true)
                    val relativeBounds = RectF(
                        bounds.left - (vectorWidth / 2f),
                        bounds.top - (vectorHeight / 2f),
                        bounds.right - (vectorWidth / 2f),
                        bounds.bottom - (vectorHeight / 2f)
                    )
                    val initialX = bounds.centerX() - (vectorWidth / 2f)
                    val initialY = bounds.centerY() - (vectorHeight / 2f)

                    val fillPaint = Paint().apply {
                        color = fillColor
                        isAntiAlias = true
                        style = if (fillColor != Color.TRANSPARENT) Paint.Style.FILL else Paint.Style.STROKE
                    }

                    val strokePaint = if (strokeColor != null && strokeWidth > 0) {
                        Paint().apply {
                            color = strokeColor
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            this.strokeWidth = strokeWidth
                        }
                    } else {
                        null
                    }

                    val glowPaint = if (fillColor != Color.TRANSPARENT || strokeColor != null) {
                        Paint().apply {
                            color = strokeColor ?: fillColor
                            isAntiAlias = true
                            style = if (strokeColor != null) Paint.Style.STROKE else Paint.Style.FILL
                            this.strokeWidth = if (strokeColor != null) strokeWidth + 10f else 0f
                            setMaskFilter(BlurMaskFilter(36f, BlurMaskFilter.Blur.NORMAL))
                            alpha = 250
                        }
                    } else {
                        null
                    }

                    paths.add(
                        PathInfo(
                            originalPath, initialX, initialY, path, fillPaint, strokePaint, glowPaint,
                            relativeBounds, initialX, initialY, vectorWidth, vectorHeight
                        )
                    )
                }
            }
            eventType = parser.next()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { path ->
            canvas.save()
            matrix.reset()
            matrix.postTranslate(
                path.offsetX - path.initialX - path.width / 2,
                path.offsetY - path.initialY - path.height / 2
            )
            canvas.concat(matrix)
            path.glowPaint?.let { canvas.drawPath(path.path, it) }
            canvas.drawPath(path.path, path.fillPaint)
            path.strokePaint?.let { canvas.drawPath(path.path, it) }
            canvas.restore()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        relativeX = w / 2f
        relativeY = h / 2f

        post {
            (layoutParams as? FrameLayout.LayoutParams)?.let {
                it.gravity = Gravity.CENTER
                layoutParams = it
            }
        }
        paths.forEach { path ->
            path.directionX = if (Random.nextBoolean()) 1 else -1
            path.directionY = if (Random.nextBoolean()) 1 else -1
            directX *= -1
            directY *= -1
            path.centerX = relativeX
            path.centerY = relativeY
            path.offsetX = path.centerX + path.initialX
            path.offsetY = path.centerY + path.initialY
            path.x = relativeX + path.initialX
            path.y = relativeY + path.initialY
        }
        scramblePaths()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (!isTouchEnabled) return false
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = it.x
                    touchY = it.y
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = it.x
                    val newY = it.y
                    if (abs(newX - touchX) > 5f || abs(newY - touchY) > 5f) {
                        paths.forEach { path ->
                            movePath(path, newX, newY)
                        }
                        touchX = newX
                        touchY = newY
                        invalidate()
                        checkAndSnapIfAligned()
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    checkAndSnapIfAligned()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun movePath(path: PathInfo, x: Float, y: Float) {
        val dx = (x - touchX) * path.directionX
        val dy = (y - touchY) * path.directionY

        var newOffsetX = path.offsetX + dx
        var newOffsetY = path.offsetY + dy

        val maxOffset = 100f

        if (newOffsetX <= path.x - maxOffset || newOffsetX >= path.x + maxOffset) {
            path.directionX *= -1
        }

        if (newOffsetY <= path.y - maxOffset || newOffsetY >= path.y + maxOffset) {
            path.directionY *= -1
        }

        path.offsetX = newOffsetX.coerceIn(path.x - maxOffset, path.x + maxOffset)
        path.offsetY = newOffsetY.coerceIn(path.y - maxOffset, path.y + maxOffset)
    }

    private fun snapToOriginalPosition() {
        paths.forEach { path ->
            val targetOffsetX = path.centerX + path.initialX
            val targetOffsetY = path.centerY + path.initialY

            val animatorX = ValueAnimator.ofFloat(path.offsetX, targetOffsetX).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    path.offsetX = animation.animatedValue as Float
                    invalidate()
                }
            }

            val animatorY = ValueAnimator.ofFloat(path.offsetY, targetOffsetY).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    path.offsetY = animation.animatedValue as Float
                    invalidate()
                }
            }

            path.directionX = if (Random.nextBoolean()) 1 else -1
            path.directionY = if (Random.nextBoolean()) 1 else -1

            animatorX.start()
            animatorY.start()
        }
    }

    private fun checkAndSnapIfAligned() {
        val threshold = 5f
        val allAligned = paths.all { path ->
            val targetOffsetX = path.centerX + path.initialX
            val targetOffsetY = path.centerY + path.initialY
            abs(path.offsetX - targetOffsetX) < threshold && abs(path.offsetY - targetOffsetY) < threshold
        }

        if (allAligned) {
            snapToOriginalPosition()
            level?.isComplete = true
            setTouchEnabled(false)
            val endTime = System.nanoTime()
            val timeStop = endTime - startTime

            val durationInMs = timeStop / 1_000_000
            val seconds = (durationInMs / 1000) % 60
            val minutes = (durationInMs / (1000 * 60)) % 60

            when {
                seconds < 10 && minutes <= 1 -> star = 3
                seconds in 10..20 && minutes <= 1 -> star = 2
                else -> star = 1
            }
            val handler = Handler(Looper.getMainLooper())
            val action = Runnable { showDialog(endTime) }
            val delayMillis: Long = 1800
            handler.postDelayed(action, delayMillis)
            listener?.onComplete(star = star)
        }
    }

    fun scramblePaths() {
        startTime = System.nanoTime()
        val maxOffset = 150f
        val randomOffsetX = Random.nextInt(50, 100)

        paths.forEach { path ->
            if (path.centerX == 0f || path.centerY == 0f) return@forEach
            val targetOffsetX = (path.centerX + path.initialX + randomOffsetX * path.directionX)
                .coerceIn(path.x - maxOffset, path.x + maxOffset)
            val targetOffsetY = (path.centerY + path.initialY + randomOffsetX * path.directionY)
                .coerceIn(path.y - maxOffset, path.y + maxOffset)

            val animatorX = ValueAnimator.ofFloat(path.offsetX, targetOffsetX).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    path.offsetX = animation.animatedValue as Float
                    invalidate()
                }
            }
            val animatorY = ValueAnimator.ofFloat(path.offsetY, targetOffsetY).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    path.offsetY = animation.animatedValue as Float
                    invalidate()
                }
            }
            animatorX.start()
            animatorY.start()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showDialog(endTime: Long) {
        val timeStop = endTime - startTime
        val durationInMs = timeStop / 1_000_000
        val seconds = (durationInMs / 1000) % 60
        val minutes = (durationInMs / (1000 * 60)) % 60
        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val txtShowTime = dialog.findViewById<TextView>(R.id.tvFinishTime)
        val imgStarCenter = dialog.findViewById<ImageView>(R.id.imgCenterStar)
        val imgStarLeft = dialog.findViewById<ImageView>(R.id.imgLeftStar)
        val imgStarRight = dialog.findViewById<ImageView>(R.id.imgRightStar)

        when {
            seconds < 10 && minutes <= 1 -> {
                Log.e("TAG", "showDialog: 3")
                imgStarLeft.setImageResource(R.drawable.favourites)
                imgStarCenter.setImageResource(R.drawable.favourites)
                imgStarRight.setImageResource(R.drawable.favourites)
            }
            seconds in 10..20 && minutes <= 1 -> {
                Log.e("TAG", "showDialog: 2")
                imgStarLeft.setImageResource(R.drawable.favourites)
                imgStarCenter.setImageResource(R.drawable.favourites)
                imgStarRight.setImageResource(R.drawable.empty_star)
            }
            else -> {
                Log.e("TAG", "showDialog: 1")
                imgStarLeft.setImageResource(R.drawable.favourites)
                imgStarCenter.setImageResource(R.drawable.empty_star)
                imgStarRight.setImageResource(R.drawable.empty_star)
            }
        }

        imgStarLeft.animate()
            .setStartDelay(500)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(40)
            .start()

        imgStarCenter.animate()
            .setStartDelay(800)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(500)
            .start()

        imgStarRight.animate()
            .setStartDelay(1200)
            .scaleX(1.5f)
            .scaleY(1.5f)
            .setDuration(500)
            .start()

        txtShowTime.text = "\"$minutes\" : \"$seconds\""
        btnContinue.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun setTouchEnabled(enabled: Boolean) {
        isTouchEnabled = enabled
    }

    interface OnProcessingCompleteListener {
        fun onComplete(star: Int)
    }

    fun setOnProcessingCompleteListener(listener: OnProcessingCompleteListener) {
        this.listener = listener
    }

    fun nextLevel(@DrawableRes nextDrawableId: Int) {
        // 1. Clear existing paths
        paths.clear()

        // 2. Reset state
        startTime = System.nanoTime()
        star = 0
        isTouchEnabled = true

        // 3. Parse new vector drawable
        parseVectorDrawable(nextDrawableId)

        // 4. Force recalculation of positions
        post {
            // Gọi lại onSizeChanged với kích thước hiện tại
            onSizeChanged(width, height, width, height)

            // Hoặc reset thủ công các tọa độ
            relativeX = width / 2f
            relativeY = height / 2f

            paths.forEach { path ->
                path.centerX = relativeX
                path.centerY = relativeY
                path.offsetX = path.centerX + path.initialX
                path.offsetY = path.centerY + path.initialY
                path.x = relativeX + path.initialX
                path.y = relativeY + path.initialY
            }

            scramblePaths()
            invalidate()
        }
    }
}

fun String.toColorInt(): Int? {
    return try {
        this.toColorInt()
    } catch (e: Exception) {
        null
    }
}