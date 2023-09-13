package com.hifi.redeal.schedule
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.hifi.redeal.R

class CustomTimePicker : TimePicker {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // 해당 View가 윈도우에 연결될 때 호출되는 콜백 메서드
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // 분 단위 스피너를 찾기
        val minutePicker = findViewById<NumberPicker>(
            resources.getIdentifier("minute", "id", "android")
        )

        // 분 단위를 5분 간격으로 설정
        val minuteValues = Array(12) { (it * 5).toString().padStart(2, '0') }
        minutePicker.minValue = 0
        minutePicker.maxValue = 11
        minutePicker.displayedValues = minuteValues
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val selectedColor = ContextCompat.getColor(context, R.color.primary95)
        drawBackground(this, canvas,selectedColor)

    }

    private fun drawBackground(view: TimePicker, canvas: Canvas,selectedColor:Int) {
        val paint = Paint()
        paint.color = selectedColor
    }
}
