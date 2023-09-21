package com.hifi.redeal.schedule

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentUnvisitedScheduleBinding
import com.hifi.redeal.databinding.FragmentVisitedScheduleBinding
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import com.hifi.redeal.schedule.vm.ScheduleVM
import org.threeten.bp.DateTimeUtils.toDate
import java.sql.Date
import java.util.Calendar

class UnvisitedScheduleFragment : Fragment() {

    lateinit var fragmentUnvisitedScheduleBinding: FragmentUnvisitedScheduleBinding
    lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM
    private val uid = Firebase.auth.uid!!
    var scheduleIdx = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentUnvisitedScheduleBinding = FragmentUnvisitedScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        setViewModel()
        setClickEvent()

        return fragmentUnvisitedScheduleBinding.root
    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{

            selectScheduleData.observe(viewLifecycleOwner){ scheduleInfo ->

                getClientInfo(uid, scheduleInfo.clientIdx)

                fragmentUnvisitedScheduleBinding.run{
                    var crateDate = Date(scheduleInfo.scheduleDataCreateTime.toDate().time).toString().replace("-",".")
                    scheduleWriteDate.text = crateDate

                    val crateCalendar = Calendar.getInstance()
                    crateCalendar.timeInMillis = scheduleInfo.scheduleDataCreateTime.toDate().time

                    val crateHour = crateCalendar.get(Calendar.HOUR_OF_DAY) // 24시간 형식
                    val crateMinute = crateCalendar.get(Calendar.MINUTE)

                    if(crateHour < 10){
                        if(crateMinute < 10){
                            scheduleWriteTime.text = "0$crateHour : 0$crateMinute"
                        } else {
                            scheduleWriteTime.text = "0$crateHour : $crateMinute"
                        }
                    } else {
                        if(crateMinute < 10){
                            scheduleWriteTime.text = "$crateHour : 0$crateMinute"
                        } else {
                            scheduleWriteTime.text = "$crateHour : $crateMinute"
                        }
                    }

                    var deadLineDate = Date(scheduleInfo.scheduleDeadlineTime.toDate().time).toString().replace("-",".")
                    scheduleDeadLineDate.text = deadLineDate

                    val deadLineCalendar = Calendar.getInstance()
                    deadLineCalendar.timeInMillis = scheduleInfo.scheduleDeadlineTime.toDate().time

                    val deadLineHour = deadLineCalendar.get(Calendar.HOUR_OF_DAY) // 24시간 형식
                    val deadLineMinute = deadLineCalendar.get(Calendar.MINUTE)
                    if(deadLineHour < 10){
                        if(deadLineMinute < 10){
                            scheduleDeadLineTime.text = "0$deadLineHour : 0$deadLineMinute"
                        } else {
                            scheduleDeadLineTime.text = "0$deadLineHour : $deadLineMinute"
                        }
                    } else {
                        if(deadLineMinute < 10){
                            scheduleDeadLineTime.text = "$deadLineHour : 0$deadLineMinute"
                        } else {
                            scheduleDeadLineTime.text = "$deadLineHour : $deadLineMinute"
                        }
                    }

                    scheduleDataTitle.text = scheduleInfo.scheduleTitle
                    scheduleDataContents.text = scheduleInfo.scheduleContext

                    unvisitedScheduleToolbar.run {
                        if(scheduleInfo.isScheduleFinish){
                            unvisitedScheduleToolbar.menu.findItem(R.id.scheduleCompleteMenu).setIcon(R.drawable.replay_fill_24px)
                        } else {
                            unvisitedScheduleToolbar.menu.findItem(R.id.scheduleCompleteMenu).setIcon(R.drawable.done_paint_24px)
                        }
                    }
                }
            }

            selectClientData.observe(viewLifecycleOwner){
                fragmentUnvisitedScheduleBinding.run{

                    scheduleClientState.visibility = View.VISIBLE
                    unvisitedClientBookmark.visibility = View.GONE

                    when(it.clientState){
                        1L -> {
                            scheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trading)
                        }
                        2L -> {
                            scheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                        }
                        3L -> {
                            scheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                        }
                        else -> scheduleClientState.visibility = View.GONE
                    }
                    if(it.isBookmark){
                        unvisitedClientBookmark.setBackgroundResource(R.drawable.star_fill_24px)
                        unvisitedClientBookmark.visibility = View.VISIBLE
                    }

                    unvisitedClientInfo.text = "${it.clientName} ${it.clientManagerName}"

                }
            }

            getSelectScheduleInfo(uid, "${scheduleVM.selectScheduleIdx}")

        }
    }

    private fun setClickEvent(){
        fragmentUnvisitedScheduleBinding.run{
            unvisitedScheduleToolbar.run{

                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.UNVISITED_SCHEDULE_FRAGMENT)
                }

                setOnMenuItemClickListener {
                    val updateScheduleData = scheduleVM.selectScheduleData.value!!

                    when(it.itemId){
                        R.id.scheduleCompleteMenu -> {
                            if(updateScheduleData.isScheduleFinish){
                                val cancelBuilder = AlertDialog.Builder(requireActivity())
                                cancelBuilder.setMessage("해당 일정을 완료 취소 처리 합니다.")
                                cancelBuilder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                    updateScheduleData.isScheduleFinish = false
                                    ScheduleRepository.setUserSchedule(uid,updateScheduleData,{
                                        scheduleVM.getSelectClientLastVisitDate(uid, scheduleVM.selectScheduleData.value!!.clientIdx)
                                    },{
                                        scheduleVM.getSelectScheduleInfo(uid, "${scheduleVM.selectScheduleData.value!!.scheduleIdx}")
                                    })
                                }
                                cancelBuilder.setPositiveButton("취소",null)
                                cancelBuilder.show()
                            } else {
                                val completeBuilder = AlertDialog.Builder(requireActivity())
                                completeBuilder.setMessage("해당 일정을 완료 처리합니다.")
                                completeBuilder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                    updateScheduleData.isScheduleFinish = true
                                    updateScheduleData.scheduleFinishTime = Timestamp.now()
                                    ScheduleRepository.setUserSchedule(uid,updateScheduleData,{
                                        scheduleVM.getSelectClientLastVisitDate(uid, scheduleVM.selectScheduleData.value!!.clientIdx)
                                    },{
                                        scheduleVM.getSelectScheduleInfo(uid, "${scheduleVM.selectScheduleData.value!!.scheduleIdx}")
                                    })
                                }
                                completeBuilder.setPositiveButton("취소",null)
                                completeBuilder.show()
                            }

                        }
                        R.id.scheduleEditMenu -> {
                            mainActivity.replaceFragment(MainActivity.EDIT_SCHEDULE_FRAGMENT, true, null)
                        }
                        R.id.scheduleDelMenu -> {
                            val deleteBuilder = AlertDialog.Builder(requireActivity())
                            deleteBuilder.setMessage("해당 일정을 삭제 처리합니다.")
                            deleteBuilder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                ScheduleRepository.delSelectSchedule(uid,"${updateScheduleData.scheduleIdx}"){
                                    mainActivity.removeFragment(MainActivity.UNVISITED_SCHEDULE_FRAGMENT)
                                }
                            }
                            deleteBuilder.setPositiveButton("취소",null)
                            deleteBuilder.show()
                        }
                    }
                    true
                }

            }
        }
    }

}