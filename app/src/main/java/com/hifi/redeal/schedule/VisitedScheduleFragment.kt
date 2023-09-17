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
    var clientIdx = 0L
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

        clientIdx = arguments?.getLong("clientIdx")!!
        scheduleIdx = arguments?.getLong("scheduleIdx")!!
    }
    private fun setViewModel(){

        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{
            clientResultData.observe(requireActivity()){
                fragmentVisitedScheduleBinding.run{
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
                        clientLastVisitedTime.text = "0$hour : $minute"
                    } else {
                        clientLastVisitedTime.text = "$hour : $minute"
                    }

                }
            }

            selectScheduleData.observe(requireActivity()){ scheduleInfo ->
                fragmentVisitedScheduleBinding.run{
                    var date = Date(scheduleInfo.scheduleDeadlineTime.toDate().time).toString().replace("-",".")
                    clientVisitDateDate.text = date

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = scheduleInfo.scheduleDeadlineTime.toDate().time

                    val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24시간 형식
                    val minute = calendar.get(Calendar.MINUTE)
                    if(hour < 10){
                        clientVisitedTime.text = "0$hour : $minute"
                    } else {
                        clientVisitedTime.text = "$hour : $minute"
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

            getClientInfo(userIdx,clientIdx)
            getSelectClientLastVisitDate("$userIdx", clientIdx)
            getSelectScheduleInfo("$userIdx", "$scheduleIdx")
        }
    }

    private fun setClickEvent(){
        fragmentVisitedScheduleBinding.run{
            visitedScheduleToolbar.run{

                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.VISITED_SCHEDULE_FRAGMENT)
                }

                setOnMenuItemClickListener {

                    when(it.itemId){
                        R.id.scheduleCompleteMenu -> {
                            val updateScheduleData = scheduleVM.selectScheduleData.value!!
                            if(updateScheduleData.isScheduleFinish){
                                val cancelBuilder = AlertDialog.Builder(requireActivity())
                                cancelBuilder.setMessage("해당 일정을 완료 취소 처리 합니다.")
                                cancelBuilder.setNegativeButton("확인"){ dialogInterface: DialogInterface, i: Int ->
                                    updateScheduleData.isScheduleFinish = false
                                    ScheduleRepository.setUserSchedule("$userIdx", updateScheduleData){
                                        scheduleVM.getSelectClientLastVisitDate("$userIdx", clientIdx)
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
                                        scheduleVM.getSelectClientLastVisitDate("$userIdx", clientIdx)
                                        scheduleVM.getSelectScheduleInfo("$userIdx", "$scheduleIdx")
                                    }
                                }
                                completeBuilder.setPositiveButton("취소",null)
                                completeBuilder.show()
                            }

                        }
                        R.id.scheduleEditMenu -> {
                            Log.d("ttt", "수정버튼")
                        }
                        R.id.scheduleDelMenu -> {
                            Log.d("ttt", "삭제버튼")
                        }
                    }

                    true
                }

            }
        }
    }

}