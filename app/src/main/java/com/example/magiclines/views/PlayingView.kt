package com.example.magiclines.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.PathParser
import com.example.magiclines.R
import org.xmlpull.v1.XmlPullParser
import androidx.core.graphics.toColorInt
import com.example.magiclines.models.Level
import kotlin.math.abs
import kotlin.random.Random
import androidx.core.graphics.drawable.toDrawable

@SuppressLint("ViewConstructor")
class PlayingView (
    context: Context,
    private val level: Level
) : View(context) {

    val paths = mutableListOf<PathInfo>()
    private var isTouchEnabled = true
    private var listener: OnProcessingCompleteListener? = null


    private var relativeX: Float = 0f
    private var relativeY: Float = 0f
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private val matrix = Matrix()
    private var scale: Float = 1f
    private var dialog: Dialog
    private var startTime: Long = 0
    private var directX = -1
    private var directY = -1

    data class PathInfo(
        val originalPath: Path,
        val originalPathX: Float,
        val originalPathY: Float,
        val path: Path,
        val paintColor: Paint,
        val relativeBounds: RectF,
        var initialX: Float,    // Tọa độ ban đầu tuyệt đối so với tâm
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
        parseVectorDrawable(level.getResourceId()!!)
        isClickable = true
        isFocusable = true
        dialog = Dialog(context)
        dialog.setContentView(R.layout.finishing_dialog)
        startTime = System.nanoTime()
    }

    private fun parseVectorDrawable(drawableId: Int) {
        val parser = resources.getXml(drawableId)
        val centerX = 593f / 2f // 296.5
        val centerY = 420f / 2f // 210
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
                if (pathData != null) {
                    val bounds = RectF()
                    val originalPath = PathParser.createPathFromPathData(pathData)
                    val path = Path(originalPath)
                    path.computeBounds(bounds, true)
                    val relativeBounds = RectF(
                        bounds.left - centerX,
                        bounds.top - centerY,
                        bounds.right - centerX,
                        bounds.bottom - centerY
                    )
                    // Tọa độ ban đầu so với tâm viewport
                    val initialX = bounds.centerX() - centerX
                    val initialY = bounds.centerY() - centerY

                    val paint = Paint().apply {
                        this.color = fillColor
                        this.isAntiAlias = true
                    }
                    paths.add(PathInfo(
                        originalPath, initialX, initialY, path, paint, relativeBounds,
                        initialX, initialY, vectorWidth, vectorHeight
                    ))
                }
            }
            eventType = parser.next()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paths.forEach {path ->
            canvas.save()
            matrix.reset()
            matrix.postScale(scale, scale, relativeX, relativeY)
            matrix.postTranslate(path.offsetX - path.initialX * scale - path.width/2, path.offsetY - path.initialY * scale - path.height/2)
            canvas.concat(matrix)
            canvas.drawPath(path.path, path.paintColor)
            canvas.restore()
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        relativeX = w / 2f
        relativeY = h / 2f

        val vectorWidth = 593f
        val vectorHeight = 420f
        val scaleX = w / vectorWidth
        val scaleY = h / vectorHeight
        scale = minOf(scaleX, scaleY) * 0.8f

        paths.forEach { path ->
//            path.directionX = if (path.initialX > 0) -1 else 1
//            path.directionY = if (path.initialY > 0) -1 else 1
            path.directionX = if (Random.nextBoolean()) 1 else -1
            path.directionY = if (Random.nextBoolean()) 1 else -1

            directX *= -1
            directY *= -1
            // Khởi tạo vị trí center và offset
            path.centerX = relativeX
            path.centerY = relativeY
            path.offsetX = path.centerX + path.initialX * scale
            path.offsetY = path.centerY + path.initialY * scale
            path.x = relativeX + path.initialX
            path.y = relativeY + path.initialY
        }
        scramblePaths()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (!isTouchEnabled) {
                return false
            }
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
        // Tính khoảng cách di chuyển thực tế
        val dx = (x - touchX) * path.directionX
        val dy = (y - touchY) * path.directionY

        // Tính vị trí mới
        var newOffsetX = path.offsetX + dx
        var newOffsetY = path.offsetY + dy

        val maxOffset = 150f // Khoảng cách tối đa từ vị trí gốc

        // Kiểm tra và đảo hướng nếu chạm biên
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
            // Tính toán vị trí mục tiêu (vị trí ban đầu)
            val targetOffsetX = path.centerX + path.initialX * scale
            val targetOffsetY = path.centerY + path.initialY * scale

            // Tạo ValueAnimator cho offsetX
            val animatorX = ValueAnimator.ofFloat(path.offsetX, targetOffsetX).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    path.offsetX = animation.animatedValue as Float
                    invalidate() // Vẽ lại view
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


//            path.directionX = if (path.initialX > 0) -1 else 1
//            path.directionY = if (path.initialY > 0) -1 else 1
            path.directionX = if (Random.nextBoolean()) 1 else -1
            path.directionY = if (Random.nextBoolean()) 1 else -1


            animatorX.start()
            animatorY.start()
        }
    }

    private fun checkAndSnapIfAligned() {
        val threshold = 20f
        val allAligned = paths.all { path ->
            val targetOffsetX = path.centerX + path.initialX * scale
            val targetOffsetY = path.centerY + path.initialY * scale
            abs(path.offsetX - targetOffsetX) < threshold && abs(path.offsetY - targetOffsetY) < threshold
        }

        if (allAligned) {
            snapToOriginalPosition()
            level.setIsComplete(true)
            setTouchEnabled(false)
            val endTime = System.nanoTime()
            val timeStop = endTime - startTime

            val durationInMs = timeStop / 1_000_000

            val seconds = (durationInMs / 1000) % 60
            val minutes = (durationInMs / (1000 * 60)) % 60

             when {
                seconds < 10 -> level.setStar(3)
                seconds >= 10 && seconds <= 20 -> level.setStar(2)
                seconds > 20 && minutes >= 1 -> level.setStar(1)
                else -> level.setStar(0)
            }
            val handler = Handler(Looper.getMainLooper())
            val action = Runnable {
                showDialog(endTime)
            }
            val delayMillis: Long = 1800
            handler.postDelayed(action, delayMillis)
            listener?.onComplete()
        }
    }

    fun scramblePaths() {
        startTime = System.nanoTime()
        val maxOffset = 150f
        val randomOffsetX = Random.nextInt(100,150)
        val randomOffsetY = Random.nextInt(100, 150)

        paths.forEach { path ->
            if (path.centerX == 0f || path.centerY == 0f) return@forEach
            val targetOffsetX = (path.centerX + path.initialX * scale + randomOffsetX * path.directionX)
                .coerceIn(path.x - maxOffset, path.x + maxOffset)
            val targetOffsetY = (path.centerY + path.initialY * scale + randomOffsetY * path.directionY)
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

    @SuppressLint("SetTextI18n", "Recycle")
    fun showDialog(endTime: Long){
        val timeStop = endTime - startTime

        val durationInMs = timeStop / 1_000_000

        val seconds = (durationInMs / 1000) % 60
        val minutes = (durationInMs / (1000 * 60)) % 60
        val btnContinue = dialog.findViewById<Button>(R.id.btnContinue)
        val txtShowTime = dialog.findViewById<TextView>(R.id.tvFinishTime)
        val imgStarCenter = dialog.findViewById<ImageView>(R.id.imgCenterStar)
        val imgStarLeft = dialog.findViewById<ImageView>(R.id.imgLeftStar)
        val imgStarRight = dialog.findViewById<ImageView>(R.id.imgRightStar)


        if( minutes < 1 && seconds > 10 && seconds < 20){
            imgStarRight.setImageResource(R.drawable.empty_star)

        }else if(minutes < 1 && seconds < 10){

        }else  {
            imgStarCenter.setImageResource(R.drawable.empty_star)
            imgStarRight.setImageResource(R.drawable.empty_star)

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
        btnContinue.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable());
        dialog.show()
    }

      fun setTouchEnabled(enabled: Boolean) {
        isTouchEnabled = enabled
    }

    interface OnProcessingCompleteListener{
        fun onComplete()
    }

    fun setOnProcessingCompleteListener(listener: OnProcessingCompleteListener) {
        this.listener = listener
    }

}