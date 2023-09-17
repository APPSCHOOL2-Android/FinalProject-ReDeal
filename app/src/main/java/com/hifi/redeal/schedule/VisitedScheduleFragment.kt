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
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentVisitedScheduleBinding
import com.hifi.redeal.schedule.schedule_repository.ScheduleRepository
import com.hifi.redeal.schedule.vm.ScheduleVM
import java.sql.Date
import java.util.Calendar


class VisitedScheduleFragment : Fragment() {

    lateinit var fragmentVisitedScheduleBinding: FragmentVisitedScheduleBinding
    lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM
    var userIdx = 1L
    var scheduleIdx = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setBasicData()
        setViewModel()
        setClickEvent()


        return fragmentVisitedScheduleBinding.root
    }

    private fun setBasicData(){
        fragmentVisitedScheduleBinding = FragmentVisitedScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

    }
    private fun setViewModel(){

        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{

            selectScheduleData.observe(requireActivity()){ scheduleInfo ->

                getClientInfo(userIdx,scheduleInfo.clientIdx)
                getSelectClientLastVisitDate("$userIdx", scheduleInfo.clientIdx)

                fragmentVisitedScheduleBinding.run{
                    var date = Date(scheduleInfo.scheduleDeadlineTime.toDate().time).toString().replace("-",".")
                    clientVisitDateDate.text = date

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = scheduleInfo.scheduleDeadlineTime.toDate().time

                    val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24시간 형식
                    val minute = calendar.get(Calendar.MINUTE)
                    if(hour < 10){
                        if(minute < 10){
                            clientVisitedTime.text = "0$hour : 0$minute"
                        } else {
                            clientVisitedTime.text = "0$hour : $minute"
                        }
                    } else {
                        if(minute < 10){
                            clientVisitedTime.text = "$hour : 0$minute"
                        } else {
                            clientVisitedTime.text = "$hour : $minute"
                        }
                    }

                    visitedScheduleDataTitle.text = scheduleInfo.scheduleTitle
                    visitedScheduleDataContents.text = scheduleInfo.scheduleContext

                    visitedScheduleToolbar.run {
                        if(scheduleInfo.isScheduleFinish){
                            visitedScheduleToolbar.menu.findItem(R.id.scheduleCompleteMenu).setIcon(R.drawable.replay_fill_24px)
                        } else {
                            visitedScheduleToolbar.menu.findItem(R.id.scheduleCompleteMenu).setIcon(R.drawable.done_paint_24px)
                        }
                    }
                }
            }

            clientResultData.observe(requireActivity()){
                fragmentVisitedScheduleBinding.run{

                    visitedScheduleClientState.visibility = View.VISIBLE
                    visitedClientBookmark.visibility = View.GONE

                    when(it.clientState){
                        1L -> {
                            visitedScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trading)
                        }
                        2L -> {
                            visitedScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                        }
                        3L -> {
                            visitedScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                        }
                        else -> visitedScheduleClientState.visibility = View.GONE
                    }
                    if(it.isBookmark){
                        visitedClientBookmark.visibility = View.VISIBLE
                        visitedClientBookmark.setBackgroundResource(R.drawable.star_fill_24px)
                    }

                    visitedClientInfo.text = "${it.clientName} ${it.clientManagerName}"
                    scheduleClientAddress.text = "${it.clientAddress} ${it.clientDetailAdd}"
                }
            }

            clientLastVisitDate.observe(requireActivity()){ timestamp ->
                fragmentVisitedScheduleBinding.run{

                    var date = Date(timestamp.toDate().time).toString().replace("-",".")
                    clientLastVisitedDate.text = date

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = timestamp.toDate().time

                    val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24시간 형식
                    val minute = calendar.get(Calendar.MINUTE)
                    if(hour < 10){
                        if(minute < 10){
                            clientLastVisitedTime.text = "0$hour : 0$minute"
                        } else {
                            clientLastVisitedTime.text = "0$hour : $minute"
                        }
                    } else {
                        if(minute < 10){
                            clientLastVisitedTime.text = "$hour : 0$minute"
                        } else {
                            clientLastVisitedTime.text = "$hour : $minute"
                        }
                    }

                }
            }

            getSelectScheduleInfo("$userIdx", "${scheduleVM.selectScheduleIdx}")

        }
    }

    private fun setClickEvent(){
        fragmentVisitedScheduleBinding.run{
            visitedScheduleToolbar.run{

                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.VISITED_SCHEDULE_FRAGMENT)
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
                                    ScheduleRepository.setUserSchedule("$userIdx", updateScheduleData){
                                        scheduleVM.getSelectClientLastVisitDate("$userIdx", scheduleVM.selectScheduleData.value!!.clientIdx)
                                        scheduleVM.getSelectScheduleInfo("$userIdx", "$scheduleIdx")
                                    }
                                }
                                cancelBuilder.setPositiveButton("취소",null)
                                cancelBuilder.show()
                            } else {
                                val completeBuilder = AlertDialog.Builder(requireActivity())
                                completeBuilder.setMessage("해당 일정을 완료 처리합니다.")
                                completeBuilder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                    updateScheduleData.isScheduleFinish = true
                                    updateScheduleData.scheduleFinishTime = Timestamp.now()
                                    ScheduleRepository.setUserSchedule("$userIdx", updateScheduleData){
                                        scheduleVM.getSelectClientLastVisitDate("$userIdx", scheduleVM.selectScheduleData.value!!.clientIdx)
                                        scheduleVM.getSelectScheduleInfo("$userIdx", "$scheduleIdx")
                                    }
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
                                ScheduleRepository.delSelectSchedule("$userIdx", "${updateScheduleData.scheduleIdx}"){
                                    mainActivity.removeFragment(MainActivity.VISITED_SCHEDULE_FRAGMENT)
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