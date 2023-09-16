package com.hifi.redeal.schedule

import android.content.Context.INPUT_METHOD_SERVICE
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
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMakeScheduleBinding
import com.hifi.redeal.schedule.model.ClientSimpleData
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.vm.ScheduleVM
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MakeScheduleFragment : Fragment() {
    private lateinit var fragmentMakeScheduleBinding: FragmentMakeScheduleBinding
    private lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM
    private var selectedDate: LocalDate = LocalDate.now()
    private var userIdx = "1" // 추후 사용자의 idx 저장
    private var clientIdx = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMakeScheduleBinding = FragmentMakeScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        setViewModel()
        setCalendarView()
        setTimePicker()
        setDateToText()
        setTimeToText()
        setOnBasicView()
        setClickEvent()

        return fragmentMakeScheduleBinding.root
    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{
            userSelectClientSimpleData.observe(mainActivity){
                val clientName = it.clientName
                val clientManagerName = it.clientManagerName
                clientIdx = it.clientIdx
                Log.d("ttt","$it")
                fragmentMakeScheduleBinding.makeScheduleClientInfo.text = "$clientName $clientManagerName"
            }
        }
    }
    private fun setOnBasicView(){
        fragmentMakeScheduleBinding.run{
            when(scheduleVM.selectedScheduleIsVisit){
                true ->{
                    makeScheduleBtnVisit.run{
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                        makeScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        makeScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    }
                }
                false ->{
                    makeScheduleBtnNotVisit.run{
                        setBackgroundResource(R.drawable.btn_round_primary20)
                        setTextColor(mainActivity.getColor(R.color.primary99))
                        makeScheduleBtnVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                        makeScheduleBtnVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                    }
                }
                else -> {}
            }

            makeScheduleEditTextScheduleTitle.run{
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
                                clearFocus()
                                isKeyboardOpen = false
                            }
                        }
                    }
                })
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            }

            makeScheduleEditTextScheduleContent.run{
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
                                clearFocus()
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
        fragmentMakeScheduleBinding.run{

            makeScheduleTimePicker.run {
                // 시간 선택 이벤트 핸들러
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    var amPm = if(hour >= 12) "오후" else "오전"
                    var hour = if(hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    if(hourOfDay == 0) hour = 12
                    var selMinute = if(minute * 5 < 10) "0${minute * 5}" else "${minute * 5}"
                    makeScheduleBtnSelectTime.text = "$amPm $hour : $selMinute"
                }
            }

        }

    }

    private fun setTimeToText(){
        fragmentMakeScheduleBinding.run {
            var minute = makeScheduleTimePicker.minute
            var hour = makeScheduleTimePicker.hour
            if(((minute/5)+1)*5 > 55) {
                hour++
                minute = 0
            } else {
                minute = ((minute/5)+1)*5
            }
            if(makeScheduleTimePicker.hour > 12)  hour -= 12
            if(makeScheduleTimePicker.hour == 0) hour = 12

            var amPm = if(makeScheduleTimePicker.hour >= 12) "오후" else "오전"
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
                    scheduleVM.selectedScheduleIsVisit = true
                    setBackgroundResource(R.drawable.btn_round_primary20)
                    setTextColor(mainActivity.getColor(R.color.primary99))
                    makeScheduleBtnNotVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    makeScheduleBtnNotVisit.setTextColor(mainActivity.getColor(R.color.primary20))
                }
            }

            makeScheduleBtnNotVisit.run{
                setOnClickListener {
                    scheduleVM.selectedScheduleIsVisit = false
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

            makeScheduleBtnSelectAccount.run{
                setOnClickListener {
                    mainActivity.replaceFragment(MainActivity.SCHEDULE_SELECT_BY_CLIENT_FRAGMENT, true, null)
                }
            }

            makeScheduleBtnComplete.setOnClickListener {
                if(scheduleVM.selectedScheduleIsVisit == null){
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("일정 종류를 선택해 주세요.")
                    builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                        makeScheduleBtnVisit.requestFocus()
                    }
                    builder.show()
                    return@setOnClickListener
                }
                if(scheduleVM.userSelectClientSimpleData.value == null){
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("거래처를 선택해 주세요.")
                    builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                        makeScheduleClientInfo.requestFocus()
                    }
                    builder.show()
                    return@setOnClickListener
                }
                if(makeScheduleEditTextScheduleTitle.editableText.isNullOrEmpty()){
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("일정 제목을 입력해 주세요.")
                    builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                        makeScheduleClientInfo.requestFocus()
                    }
                    builder.show()
                    return@setOnClickListener
                }
                if(makeScheduleEditTextScheduleContent.editableText.isNullOrEmpty()){
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("일정에 대한 내용을 입력해 주세요.")
                    builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                        makeScheduleClientInfo.requestFocus()
                    }
                    builder.show()
                    return@setOnClickListener
                }

                val scheduleDeadlineTime = java.sql.Date.valueOf("$selectedDate")

                val calendar = Calendar.getInstance()
                calendar.time = scheduleDeadlineTime
                calendar.set(Calendar.HOUR_OF_DAY, makeScheduleTimePicker.hour)
                calendar.set(Calendar.MINUTE, makeScheduleTimePicker.minute*5)

                val newScheduleData = ScheduleData(
                    0L,
                    clientIdx,
                    false,
                    scheduleVM.selectedScheduleIsVisit!!,
                    makeScheduleEditTextScheduleContent.editableText.toString(),
                    Timestamp(Date()),
                    Timestamp(Date(calendar.timeInMillis)),
                    makeScheduleEditTextScheduleTitle.editableText.toString()
                )

                scheduleVM.addUserSchedule(userIdx, newScheduleData){
                    val builder = AlertDialog.Builder(mainActivity)
                    builder.setMessage("일정을 성공적으로 저장하였습니다.")
                    builder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                        mainActivity.removeFragment(MainActivity.MAKE_SCHEDULE_FRAGMENT)
                    }
                    builder.show()
                }

            }

        }
    }

    private fun setDateToText(){
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
                setDateToText()
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