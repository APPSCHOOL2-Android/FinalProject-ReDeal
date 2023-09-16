package com.hifi.redeal.schedule

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.CompleteScheduleItemBinding
import com.hifi.redeal.databinding.FragmentScheduleManageBinding
import com.hifi.redeal.databinding.ScheduleItemBinding
import com.hifi.redeal.schedule.model.ScheduleTotalData
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import com.hifi.redeal.schedule.vm.ScheduleVM
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class ScheduleManageFragment : Fragment(){

    lateinit var mainActivity: MainActivity
    lateinit var fragmentScheduleManageBinding: FragmentScheduleManageBinding
    lateinit var scheduleVM: ScheduleVM

    private var selectedDate: LocalDate = LocalDate.now()
    var userIdx = "1" // 추후 사용자의 idx 저장
    var visitFilter = true // true 일 경우 방문 일정, false 일 경우 미방문 일정.
    var scheduleList = mutableListOf<ScheduleTotalData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mainActivity = activity as MainActivity
        fragmentScheduleManageBinding = FragmentScheduleManageBinding.inflate(layoutInflater)

        setCalendarView()
        setViewModel()
        setClickEvent()

        return fragmentScheduleManageBinding.root
    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{
            scheduleListVM.observe(mainActivity){
                scheduleList = it
                setScheduleListLayout(scheduleList)
            }
        }

        scheduleVM.getUserDayOfSchedule(userIdx, LocalDate.now().toString())
    }

    private fun setClickEvent(){
        fragmentScheduleManageBinding.run{
            visitScheduleFilter.run{
                setOnClickListener {
                    setTextColor(mainActivity.getColor(R.color.primary10))
                    notVisitScheduleFilter.setTextColor(mainActivity.getColor(R.color.primary90))
                    visitFilter = true
                    scheduleVM.getUserDayOfSchedule(userIdx, "$selectedDate")
                }
            }

            notVisitScheduleFilter.run{
                setOnClickListener {
                    setTextColor(mainActivity.getColor(R.color.primary10))
                    visitScheduleFilter.setTextColor(mainActivity.getColor(R.color.primary90))
                    visitFilter = false
                    scheduleVM.getUserDayOfSchedule(userIdx, "$selectedDate")
                }
            }

            schedultAddBtn.run{
                setOnClickListener {
                    scheduleVM.selectDataClear()
                    mainActivity.replaceFragment(MainActivity.MAKE_SCHEDULE_FRAGMENT, true, null)
                }
            }

            scheduleManageToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.SCHEDULE_MANAGE_FRAGMENT)
                }
            }
        }
    }
    private fun setScheduleListLayout(scheduleList: MutableList<ScheduleTotalData>){
        fragmentScheduleManageBinding.scheduleListLayout.removeAllViews()

        scheduleList.sortWith(compareBy<ScheduleTotalData> { it.isScheduleFinish }.thenBy { it.scheduleDeadlineTime })

        var count = 0

        for(schedule in scheduleList){
            // 미완료 스케줄 레이아웃 추가 및 이벤트 설정
            if(schedule.isVisitSchedule == visitFilter){
                count++
                if(!schedule.isScheduleFinish){
                    val scheduleItemBinding = ScheduleItemBinding.inflate(layoutInflater)

                    scheduleItemBinding.scheduleName.text = schedule.scheduleTitle
                    val sdf = SimpleDateFormat("HH : mm", Locale.getDefault())
                    var time = schedule.scheduleDeadlineTime.toDate()
                    var scheduleTime = sdf.format(time)
                    scheduleItemBinding.scheduleTime.text = scheduleTime

                    if(schedule.clientName != null){
                        scheduleItemBinding.scheduleClientInfo.text = "${schedule.clientName} ${schedule.clientManagerName}"
                        when(schedule.clientState){
                            1L -> {
                                scheduleItemBinding.scheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trading)
                            }
                            2L -> {
                                scheduleItemBinding.scheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                            }
                            3L -> {
                                scheduleItemBinding.scheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                            }
                            else -> return
                        }
                    }

                    if(schedule.isBookmark != null){
                        if(schedule.isBookmark!!){
                            scheduleItemBinding.scheduleClientBookmarkView.setBackgroundResource(R.drawable.star_fill_24px)
                        }
                    }

                    var layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    scheduleItemBinding.root.layoutParams = layoutParams
                    // 일정 클릭 이벤트 설정 부분
                    scheduleItemBinding.run{

                        // 뷰 클릭 이벤트
                        root.setOnClickListener {
                            // 추후 번들에 선택한 일정 idx 넣은 후 전달
                            mainActivity.replaceFragment(MainActivity.VISITED_SCHEDULE_FRAGMENT, true, null)
                        }

                        // 스케줄 완료 처리 버튼 클릭 이벤트
                        completeCheckSchedule.setOnClickListener {
                            val builder = AlertDialog.Builder(mainActivity)
                            builder.setTitle("일정 완료 처리")
                            builder.setMessage("확인 버튼을 누르면 해당 일정은 완료 처리 됩니다.")
                            builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                ScheduleRepository.updateUserDayOfScheduleState(userIdx, schedule.scheduleIdx.toString()){
                                    scheduleVM.getUserDayOfSchedule(userIdx, "$selectedDate")
                                }
                            }
                            builder.setPositiveButton("취소", null)
                            builder.show()
                        }
                    }
                    fragmentScheduleManageBinding.scheduleListLayout.addView(scheduleItemBinding.root)
                } else {
                    val completeScheduleItemBinding = CompleteScheduleItemBinding.inflate(layoutInflater)

                    completeScheduleItemBinding.completeScheduleName.text = schedule.scheduleTitle
                    val sdf = SimpleDateFormat("HH : mm", Locale.getDefault())
                    var time = schedule.scheduleDeadlineTime.toDate()
                    var scheduleTime = sdf.format(time)
                    completeScheduleItemBinding.completeScheduleTime.text = scheduleTime

                    if(schedule.clientName != null){
                        completeScheduleItemBinding.completeScheduleClientInfo.text = "${schedule.clientName} ${schedule.clientManagerName}"
                        when(schedule.clientState){
                            1L -> {
                                completeScheduleItemBinding.completeScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trading)
                            }
                            2L -> {
                                completeScheduleItemBinding.completeScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                            }
                            3L -> {
                                completeScheduleItemBinding.completeScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                            }
                            else -> return
                        }
                    }

                    if(schedule.isBookmark != null){
                        if(schedule.isBookmark!!){
                            completeScheduleItemBinding.completeScheduleClientBookmarkView.setBackgroundResource(R.drawable.star_fill_24px)
                        }
                    }

                    var layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    completeScheduleItemBinding.root.layoutParams = layoutParams

                    completeScheduleItemBinding.run {

                        root.setOnClickListener {
                            // 추후 번들에 선택한 일정 idx 넣은 후 전달
                            mainActivity.replaceFragment(MainActivity.UNVISITED_SCHEDULE_FRAGMENT, true, null)
                        }

                        completeScheduleRetryImageView.setOnClickListener {
                            val builder = AlertDialog.Builder(mainActivity)
                            builder.setTitle("일정 완료 취소 처리")
                            builder.setMessage("확인 버튼을 누르면 해당 일정은 완료 취소 처리 됩니다.")
                            builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                ScheduleRepository.updateUserDayOfScheduleState(userIdx, schedule.scheduleIdx.toString()){
                                    scheduleVM.getUserDayOfSchedule(userIdx, "$selectedDate")
                                }
                            }
                            builder.setPositiveButton("취소", null)
                            builder.show()
                        }
                    }

                    fragmentScheduleManageBinding.scheduleListLayout.addView(completeScheduleItemBinding.root)
                }
            }

        }

        // 방문 일정 표시 하는 텍스트 뷰
        val spaceTextView = TextView(mainActivity)
        var layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400
        )
        spaceTextView.layoutParams = layoutParams
        spaceTextView.text = if(visitFilter) {
            "방문 일정 ${count}개"
        } else {
            "미방문 일정 ${count}개"
        }
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

                    } else {
                        // Hide in and out dates
                        textView.setTextColor(mainActivity.getColor(R.color.text80))
                    }
                    setDateText()
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

    private fun setDateText(){
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

                // 클릭한 날짜에 해당하는 일정을 가져오는 코드 작성 부분
                scheduleVM.getUserDayOfSchedule(userIdx, "$selectedDate")

                setDateText()

                fragmentScheduleManageBinding.calendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    fragmentScheduleManageBinding.calendarView.notifyDateChanged(currentSelection)
                }

            }
        }
    }

    private inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val headerMonthTextView = view.findViewById<TextView>(R.id.headerMonthTextView)
        val headerYearTextView = view.findViewById<TextView>(R.id.headerYearTextView)
    }

}