package com.hifi.redeal.schedule

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentEditScheduleBinding
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import com.hifi.redeal.schedule.vm.ScheduleVM
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.YearMonth
import java.time.ZoneId.systemDefault
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale


class EditScheduleFragment : Fragment() {

    lateinit var fragmentEditScheduleBinding: FragmentEditScheduleBinding
    lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM
    lateinit var newEditScheduleData: ScheduleData
    var userIdx = "1" // 추후 사용자의 idx 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentEditScheduleBinding = FragmentEditScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        setViewModel()
        setBasicView()
        setCalendarView()
        setTimePicker()
        setTimeToText()
        setDateToText()
        setClickEvent()


        return fragmentEditScheduleBinding.root
    }


    private fun setClickEvent(){

        fragmentEditScheduleBinding.run{
            editScheduleBtnVisit.run{
                setOnClickListener {
                    scheduleVM.editScheduleData.value?.isVisitSchedule = true
                    setBackgroundResource(R.drawable.btn_round_primary20)
                    setTextColor(mainActivity.getColor(R.color.primary99))
                    editScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    editScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            editScheduleBtnNotVisit.run{
                setOnClickListener {
                    scheduleVM.editScheduleData.value?.isVisitSchedule = false
                    setBackgroundResource(R.drawable.btn_round_primary20)
                    setTextColor(mainActivity.getColor(R.color.primary99))
                    editScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    editScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            editScheduleBtnSelectCalendar.run{
                setOnClickListener {

                    if(editScheduleCalendarView.isVisible){
                        editScheduleCalendarView.visibility = View.GONE
                        setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary20))
                    } else {
                        editScheduleCalendarView.visibility = View.VISIBLE
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                    }

                    editScheduleTimePicker.visibility = View.GONE
                    editScheduleBtnSelectTime.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    editScheduleBtnSelectTime.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            editScheduleBtnSelectTime.run{
                setOnClickListener {

                    if(editScheduleTimePicker.isVisible){
                        editScheduleTimePicker.visibility = View.GONE
                        setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary20))
                    } else {
                        editScheduleTimePicker.visibility = View.VISIBLE
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                    }

                    editScheduleCalendarView.visibility = View.GONE
                    editScheduleBtnSelectCalendar.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    editScheduleBtnSelectCalendar.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            editScheduleBtnSelectAccount.run{
                setOnClickListener {
                    mainActivity.replaceFragment(MainActivity.SCHEDULE_SELECT_BY_CLIENT_FRAGMENT, true, null)
                }
            }

            editScheduleBtnComplete.run{
                setOnClickListener {
                    val scheduleDeadlineTime = java.sql.Date.valueOf("${scheduleVM.selectDate}")

                    val calendar = Calendar.getInstance()
                    calendar.time = scheduleDeadlineTime
                    calendar.set(Calendar.HOUR_OF_DAY, editScheduleTimePicker.hour)
                    calendar.set(Calendar.MINUTE, editScheduleTimePicker.minute*5)

                    scheduleVM.editScheduleData.value?.scheduleDeadlineTime = Timestamp(Date(calendar.timeInMillis))

                    if(!editScheduleEditTextScheduleContent.editableText.isNullOrEmpty()) {
                        scheduleVM.editScheduleData.value?.scheduleContext = editScheduleEditTextScheduleContent.editableText.toString()
                    }

                    if(!editScheduleEditTextScheduleTitle.editableText.isNullOrEmpty()) {
                        scheduleVM.editScheduleData.value?.scheduleTitle = editScheduleEditTextScheduleTitle.editableText.toString()
                    }

                    ScheduleRepository.setUserSchedule(userIdx, scheduleVM.editScheduleData.value!!){
                        val builder = AlertDialog.Builder(mainActivity)
                        builder.setMessage("일정을 성공적으로 변경 하였습니다.")
                        builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                            mainActivity.removeFragment(MainActivity.EDIT_SCHEDULE_FRAGMENT)
                        }
                        builder.show()
                    }
                }
            }
        }

    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{

            editScheduleData.observe(viewLifecycleOwner){schedule ->
                newEditScheduleData = schedule
                getUserSelectClientInfo(userIdx, schedule.clientIdx)

                fragmentEditScheduleBinding.run{
                    if(schedule.isVisitSchedule){
                        editScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_primary20)
                        editScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary99))
                        editScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        editScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    } else {
                        editScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_primary20)
                        editScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary99))
                        editScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        editScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    }
                    selectDate = schedule.scheduleDeadlineTime.toDate().toInstant().atZone(systemDefault()).toLocalDate()

                    val hour = schedule.scheduleDeadlineTime.toDate().toInstant().atZone(systemDefault()).hour
                    val minute = schedule.scheduleDeadlineTime.toDate().toInstant().atZone(systemDefault()).minute

                    editScheduleTimePicker.hour = hour
                    editScheduleTimePicker.minute = minute/5

                    editScheduleEditTextScheduleTitle.hint = schedule.scheduleTitle
                    editScheduleEditTextScheduleContent.hint = schedule.scheduleContext

                }

            }

            userSelectClientSimpleData.observe(requireActivity()){client ->

                fragmentEditScheduleBinding.editScheduleClientInfo.text = "${client.clientName} ${client.clientManagerName}"

                fragmentEditScheduleBinding.editScheduleClientBookmark.visibility = View.GONE
                fragmentEditScheduleBinding.editScheduleClientState.visibility = View.VISIBLE

                when(client.clientState){
                    1L ->{
                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trading)
                    }
                    2L ->{
                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                    }
                    3L ->{
                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                    }
                    else -> {
                        fragmentEditScheduleBinding.editScheduleClientState.visibility = View.GONE
                    }
                }
                if(client.isBookmark){
                    fragmentEditScheduleBinding.editScheduleClientBookmark.visibility = View.VISIBLE
                    fragmentEditScheduleBinding.editScheduleClientBookmark.setBackgroundResource(R.drawable.star_fill_24px)
                }
            }

            setEditScheduleData()
        }

    }

    private fun setBasicView(){
        fragmentEditScheduleBinding.run{
            when(scheduleVM.selectedScheduleIsVisit){
                true ->{
                    editScheduleBtnVisit.run{
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                        editScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        editScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    }
                }
                false ->{
                    editScheduleBtnNotVisit.run{
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                        editScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        editScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    }
                }
                else -> {}
            }

            editScheduleEditTextScheduleTitle.run{
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    private var isKeyboardOpen = false // 키보드가 열려 있는지 여부를 추적

                    override fun onGlobalLayout() {
                        val rect = android.graphics.Rect()
                        rootView.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = rootView.height

                        // 화면 높이와 키보드의 높이를 비교하여 키보드 상태를 확인합니다.
                        val keypadHeight = screenHeight - rect.bottom
                        if (keypadHeight > screenHeight * 0.15) {
                            // 키보드가 열려 있는 상태
                            isKeyboardOpen = true
                        } else {
                            // 키보드가 닫혀 있는 상태
                            if (isKeyboardOpen) {
                                requireActivity().currentFocus?.clearFocus()
                                isKeyboardOpen = false
                            }
                        }
                    }
                })
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }

            editScheduleEditTextScheduleContent.run{
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    private var isKeyboardOpen = false // 키보드가 열려 있는지 여부를 추적

                    override fun onGlobalLayout() {
                        val rect = android.graphics.Rect()
                        rootView.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = rootView.height

                        // 화면 높이와 키보드의 높이를 비교하여 키보드 상태를 확인합니다.
                        val keypadHeight = screenHeight - rect.bottom
                        if (keypadHeight > screenHeight * 0.15) {
                            // 키보드가 열려 있는 상태
                            isKeyboardOpen = true
                        } else {
                            // 키보드가 닫혀 있는 상태
                            if (isKeyboardOpen) {
                                // 키보드가 내려갈 때 포커스를 제거합니다.
                                requireActivity().currentFocus?.clearFocus()
                                isKeyboardOpen = false
                            }
                        }
                    }
                })
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }
        }
    }
    private fun setTimePicker(){
        fragmentEditScheduleBinding.run{

            editScheduleTimePicker.run {

                // 시간 선택 이벤트 핸들러
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    var amPm = if(hour >= 12) "오후" else "오전"
                    var hour = if(hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    if(hourOfDay == 0) hour = 12
                    var selMinute = if(minute * 5 < 10) "0${minute * 5}" else "${minute * 5}"
                    editScheduleBtnSelectTime.text = "$amPm $hour : $selMinute"
                }
            }

        }

    }
    private fun setTimeToText(){
        fragmentEditScheduleBinding.run {
            var minute = editScheduleTimePicker.minute
            var hour = editScheduleTimePicker.hour
            if(((minute/5)+1)*5 > 55) {
                hour++
                minute = 0
            } else {
                minute = ((minute/5)+1)*5
            }
            if(editScheduleTimePicker.hour > 12)  hour -= 12
            if(editScheduleTimePicker.hour == 0) hour = 12

            var amPm = if(editScheduleTimePicker.hour >= 12) "오후" else "오전"
            if(minute < 10){
                editScheduleBtnSelectTime.text = "$amPm $hour : 0$minute"
            } else {
                editScheduleBtnSelectTime.text = "$amPm $hour : $minute"
            }

        }
    }

    private fun setDateToText(){
        // 중간 날짜 셋팅
        val selectMonth = if(scheduleVM.selectDate.month.value < 10) "0${scheduleVM.selectDate.month.value}" else scheduleVM.selectDate.month.value.toString()
        val selectDay = if(scheduleVM.selectDate.dayOfMonth < 10) "0${scheduleVM.selectDate.dayOfMonth}" else scheduleVM.selectDate.dayOfMonth.toString()

        fragmentEditScheduleBinding.editScheduleBtnSelectCalendar.text ="${scheduleVM.selectDate.year}.${selectMonth}.${selectDay}"
    }

    private fun setCalendarView(){
        fragmentEditScheduleBinding.run{

            val currentMonth = YearMonth.now()
            val firstMonth = currentMonth.minusMonths(240)
            val lastMonth = currentMonth.plusMonths(240)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            editScheduleCalendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
            editScheduleCalendarView.scrollToMonth(currentMonth)

            editScheduleCalendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    // 캘린더 내부 날짜를 표시 설정
                    container.day = day
                    val textView = container.textView
                    textView.text = day.date.dayOfMonth.toString()
                    if (day.owner == DayOwner.THIS_MONTH) {
                        // Show the month dates. Remember that views are recycled!
                        textView.visibility = View.VISIBLE

                        if (day.date == scheduleVM.selectDate) {
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
            editScheduleCalendarView.monthHeaderBinder = object :
                MonthHeaderFooterBinder<MonthViewContainer> {
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
                val currentSelection = scheduleVM.selectDate
                scheduleVM.selectDate = day.date

                // 선택되어 있는 날짜에 해당하는 일정을 가져오는 코드 작성 부분
                setDateToText()
                fragmentEditScheduleBinding.editScheduleCalendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    fragmentEditScheduleBinding.editScheduleCalendarView.notifyDateChanged(currentSelection)
                }

            }
        }
    }

    private inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val headerMonthTextView = view.findViewById<TextView>(R.id.headerMonthTextView)
        val headerYearTextView = view.findViewById<TextView>(R.id.headerYearTextView)
    }
}