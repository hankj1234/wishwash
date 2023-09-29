package com.example.wishwash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.round

class RectView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var results: ArrayList<Result>? = null
    private lateinit var classes: Array<String>

    private val textPaint = Paint().also {
        it.textSize = 60f
        it.color = Color.WHITE
    }

    fun transformRect(results: ArrayList<Result>) {
        // scale 구하기
        val scaleX = width / DataProcess.INPUT_SIZE.toFloat()
        val scaleY = scaleX * 9f / 16f
        val realY = width * 9f / 16f
        val diffY = realY - height

        results.forEach {
            it.rectF.left *= scaleX
            it.rectF.right *= scaleX
            it.rectF.top = it.rectF.top * scaleY - (diffY / 2f)
            it.rectF.bottom = it.rectF.bottom * scaleY - (diffY / 2f)
        }
        this.results = results
    }

    override fun onDraw(canvas: Canvas) {
        //그림 그리기
        results?.forEach {
            canvas?.drawRect(it.rectF, findPaint(it.classIndex))
            canvas?.drawText(
                classes[it.classIndex] + ", " + round(it.score * 100) + "%",
                it.rectF.left + 10,
                it.rectF.top + 60,
                textPaint
            )
        }
        if (canvas != null) {
            super.onDraw(canvas)
        }
    }

    fun setClassLabel(classes: Array<String>) {
        this.classes = classes
    }
    private val paintCache = mutableMapOf<Int, Paint>()

    //paint 지정
    private fun findPaint(classIndex: Int): Paint {
        return paintCache.getOrPut(classIndex) {
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 10.0f
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeMiter = 100f

            paint.color = when (classIndex) {
                0, 45, 18, 19, 22, 30, 42, 43, 44, 61, 71, 72 -> Color.WHITE
                1, 3, 14, 25, 37, 38, 79 -> Color.BLUE
                2, 9, 10, 11, 32, 47, 49, 51, 52 -> Color.RED
                // ... (rest of your colors)
                else -> Color.DKGRAY
            }
            paint
        }
    }
}