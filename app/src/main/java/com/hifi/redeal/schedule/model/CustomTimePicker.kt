package com.hifi.redeal.schedule.model
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.hifi.redeal.R
import java.time.LocalTime

class CustomTimePicker : TimePicker {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // 해당 View가 윈도우에 연결될 때 호출되는 콜백 메서드

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val minutePicker = findViewById<NumberPicker>(
            resources.getIdentifier("minute", "id", "android")
        )

        val hourPicker = findViewById<NumberPicker>(
            resources.getIdentifier("hour", "id", "android")
        )

        val amPmPicker = findViewById<NumberPicker>(
            resources.getIdentifier("amPm", "id", "android")
        )

        // 분 단위를 5분 간격으로 설정
        val minuteValues = Array(12) { if(it < 2) "0${it * 5}" else "${it * 5}"}
        minutePicker.minValue = 0
        minutePicker.maxValue = 11
        minutePicker.displayedValues = minuteValues

        minutePicker.value = if((LocalTime.now().minute / 5)+1 >= 12){
            hourPicker.value++
            0
        } else {
            (LocalTime.now().minute / 5)+1
        }

        hourPicker.minValue = 1
        hourPicker.maxValue = 12

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            minutePicker.textColor = context.getColor(R.color.primary10)
            hourPicker.textColor = context.getColor(R.color.primary10)
            amPmPicker.textColor = context.getColor(R.color.primary10)
        }

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
