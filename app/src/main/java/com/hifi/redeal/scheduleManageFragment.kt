package com.hifi.redeal

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hifi.redeal.databinding.CompleteScheduleItemBinding
import com.hifi.redeal.databinding.FragmentScheduleManageBinding
import com.hifi.redeal.databinding.ScheduleItemBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class scheduleManageFragment : Fragment(){

    lateinit var mainActivity: MainActivity
    lateinit var fragmentScheduleManageBinding: FragmentScheduleManageBinding
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mainActivity = activity as MainActivity
        fragmentScheduleManageBinding = FragmentScheduleManageBinding.inflate(layoutInflater)

        setCalendarView()
        getSchedule()

        return fragmentScheduleManageBinding.root
    }

    fun getSchedule(){
        fragmentScheduleManageBinding.scheduleListLayout.removeAllViews()

        repeat(3){
            val scheduleItemBinding = ScheduleItemBinding.inflate(layoutInflater)
            var layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            scheduleItemBinding.root.layoutParams = layoutParams

            fragmentScheduleManageBinding.scheduleListLayout.addView(scheduleItemBinding.root)

            if(it == 2){
                val completeScheduleItemBinding = CompleteScheduleItemBinding.inflate(layoutInflater)
                var layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                completeScheduleItemBinding.root.layoutParams = layoutParams

                fragmentScheduleManageBinding.scheduleListLayout.addView(completeScheduleItemBinding.root)
            }
        }

        val spaceTextView = TextView(mainActivity)
        var layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400
        )
        spaceTextView.layoutParams = layoutParams
        spaceTextView.text = "방문 일정 3개"
        spaceTextView.setTextColor(mainActivity.getColor(R.color.text50))
        spaceTextView.gravity = Gravity.CENTER_HORIZONTAL
        spaceTextView.setBackgroundColor(mainActivity.getColor(R.color.background))
        fragmentScheduleManageBinding.scheduleListLayout.addView(spaceTextView)


    }

    private fun setCalendarView(){
        fragmentScheduleManageBinding.run{

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(240)
            val lastMonth = currentMonth.plusMonths(240)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
            calendarView.scrollToMonth(currentMonth)

            calendarView.dayBinder = object : DayBinder<DayViewContainer> {
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
//                        // 오늘 날짜 표시 설정
//                        if(day.date == LocalDate.now()){
//                            textView.setTextColor(mainActivity.getColor(R.color.primary99))
//                            textView.setBackgroundResource(R.drawable.btn_round_primary20)
//                        }

                    } else {
                        // Hide in and out dates
                        textView.setTextColor(mainActivity.getColor(R.color.text80))
                    }
                    dateTextSetting()
                }
            }
            calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
                // 캘린더 상단 설정
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    container.headerMonthTextView.text = "${month.month}월"
                    container.headerYearTextView.text = "${month.year}"
                }
            }
        }
    }

    fun dateTextSetting(){
        // 중간 날짜 셋팅
        val selectMonth = if(selectedDate.month.value < 10) "0${selectedDate.month.value}" else selectedDate.month.value.toString()
        val selectDay = if(selectedDate.dayOfMonth < 10) "0${selectedDate.dayOfMonth}" else selectedDate.dayOfMonth.toString()
        val today = when(selectedDate.dayOfWeek.value){
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            7 -> "일"
            else -> "날짜 오류"
        }
        fragmentScheduleManageBinding.scheduleMidBarToday.text ="${selectedDate.year}.${selectMonth}.${selectDay} $today"
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
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

                dateTextSetting()
                fragmentScheduleManageBinding.calendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    fragmentScheduleManageBinding.calendarView.notifyDateChanged(currentSelection)
                }

            }
        }
    }


    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val headerMonthTextView = view.findViewById<TextView>(R.id.headerMonthTextView)
        val headerYearTextView = view.findViewById<TextView>(R.id.headerYearTextView)
    }

}