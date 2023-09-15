package com.hifi.redeal.schedule

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.view.isVisible
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMakeScheduleBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import org.checkerframework.checker.units.qual.min
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class MakeScheduleFragment : Fragment() {
    lateinit var fragmentMakeScheduleBinding: FragmentMakeScheduleBinding
    lateinit var mainActivity: MainActivity
    private var selectedDate: LocalDate = LocalDate.now()
    var userIdx = "1" // 추후 사용자의 idx 저장

    var selectedScheduleIsVisit : Boolean? = null // true 방문, false 미방분, null 미선택
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMakeScheduleBinding = FragmentMakeScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        setCalendarView()
        setTimePicker()
        setDateText()
        setTimeToText()
        setClickEvent()

        return fragmentMakeScheduleBinding.root
    }

    private fun setTimePicker(){
        fragmentMakeScheduleBinding.run{

            makeScheduleTimePicker.run {
                // 시간 선택 이벤트 핸들러
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    var amPm = if(hour > 12) "오후" else "오전"
                    var hour = if(hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    if(hour == 0) hour = 12
                    var selMinute = if(minute * 5 < 10) "0${minute * 5}" else "${minute * 5}"
                    makeScheduleBtnSelectTime.text = "$amPm $hour : $selMinute"
                }
            }

        }

    }

    fun setTimeToText(){
        fragmentMakeScheduleBinding.run {
            var minute = makeScheduleTimePicker.minute
            var hour = makeScheduleTimePicker.hour
            if(((minute/5)+1)*5 > 55) {
                hour++
                minute = 0
            } else {
                minute = ((minute/5)+1)*5
            }
            if(hour == 0) hour = 12

            var amPm = if(hour > 12) "오후" else "오전"
            if(minute < 10){
                makeScheduleBtnSelectTime.text = "$amPm $hour : 0$minute"
            } else {
                makeScheduleBtnSelectTime.text = "$amPm $hour : $minute"
            }

        }
    }

    private fun setClickEvent(){
        fragmentMakeScheduleBinding.run {

            makeScheduleBtnVisit.run{
                setOnClickListener {
                    selectedScheduleIsVisit = true
                    setBackgroundResource(R.drawable.btn_round_primary20)
                    setTextColor(mainActivity.getColor(R.color.primary99))
                    makeScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    makeScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            makeScheduleBtnNotVisit.run{
                setOnClickListener {
                    selectedScheduleIsVisit = false
                    setBackgroundResource(R.drawable.btn_round_primary20)
                    setTextColor(mainActivity.getColor(R.color.primary99))
                    makeScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    makeScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            makeScheduleBtnSelectCalendar.run{
                setOnClickListener {

                    if(makeScheduleCalendarView.isVisible){
                        makeScheduleCalendarView.visibility = View.GONE
                        setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary20))
                    } else {
                        makeScheduleCalendarView.visibility = View.VISIBLE
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                    }

                    makeScheduleTimePicker.visibility = View.GONE
                    makeScheduleBtnSelectTime.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    makeScheduleBtnSelectTime.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            makeScheduleBtnSelectTime.run{
                setOnClickListener {

                    if(makeScheduleTimePicker.isVisible){
                        makeScheduleTimePicker.visibility = View.GONE
                        setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary20))
                    } else {
                        makeScheduleTimePicker.visibility = View.VISIBLE
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                    }

                    makeScheduleCalendarView.visibility = View.GONE
                    makeScheduleBtnSelectCalendar.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    makeScheduleBtnSelectCalendar.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

        }
    }

    private fun setDateText(){
        // 중간 날짜 셋팅
        val selectMonth = if(selectedDate.month.value < 10) "0${selectedDate.month.value}" else selectedDate.month.value.toString()
        val selectDay = if(selectedDate.dayOfMonth < 10) "0${selectedDate.dayOfMonth}" else selectedDate.dayOfMonth.toString()

        fragmentMakeScheduleBinding.makeScheduleBtnSelectCalendar.text ="${selectedDate.year}.${selectMonth}.${selectDay}"
    }
    private fun setCalendarView(){
        fragmentMakeScheduleBinding.run{

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(240)
            val lastMonth = currentMonth.plusMonths(240)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            makeScheduleCalendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
            makeScheduleCalendarView.scrollToMonth(currentMonth)

            makeScheduleCalendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    // 캘린더 내부 날짜를 표시 설정
                    container.day = day
                    val textView = container.textView
                    textView.text = day.date.dayOfMonth.toString()
                    if (day.owner == DayOwner.THIS_MONTH) {
                        // Show the month dates. Remember that views are recycled!
                        textView.visibility = View.VISIBLE

                        if (day.date == selectedDate) {
                            // If this is the selected date, show a round background and change the text color.
                            textView.setTextColor(Color.BLACK)
                            textView.setBackgroundResource(R.drawable.date_selection_background)
                        } else {
                            // If this is NOT the selected date, remove the background and reset the text color.
                            textView.setTextColor(Color.BLACK)
                            textView.background = null
                        }
                        // 일요일 텍스트 색상 설정
                        if(day.date.dayOfWeek.value == 7){
                            textView.setTextColor(Color.RED)
                        }
                        // 토요일 텍스트 색상 설정
                        if(day.date.dayOfWeek.value == 6){
                            textView.setTextColor(mainActivity.getColor(R.color.primary30))
                        }

                    } else {
                        // Hide in and out dates
                        textView.setTextColor(mainActivity.getColor(R.color.text80))
                    }
                }
            }
            makeScheduleCalendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
                // 캘린더 상단 설정
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    container.headerMonthTextView.text = "${month.month}월"
                    container.headerYearTextView.text = "${month.year}"
                }
            }
        }
    }

    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView = view.findViewById<TextView>(R.id.calendarDayText)
        // Will be set when this container is bound
        lateinit var day: CalendarDay

        init {
            // 날짜 클릭 이벤트
            view.setOnClickListener {
                // Use the CalendarDay associated with this container.
                val currentSelection = selectedDate
                selectedDate = day.date

                // 선택되어 있는 날짜에 해당하는 일정을 가져오는 코드 작성 부분
                setDateText()
                fragmentMakeScheduleBinding.makeScheduleCalendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    fragmentMakeScheduleBinding.makeScheduleCalendarView.notifyDateChanged(currentSelection)
                }

            }
        }
    }

    private inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val headerMonthTextView = view.findViewById<TextView>(R.id.headerMonthTextView)
        val headerYearTextView = view.findViewById<TextView>(R.id.headerYearTextView)
    }

}