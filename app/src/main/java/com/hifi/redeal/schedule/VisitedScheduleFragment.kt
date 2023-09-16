package com.hifi.redeal.schedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentVisitedScheduleBinding
import com.hifi.redeal.schedule.vm.ScheduleVM
import org.threeten.bp.DateTimeUtils.toDate
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min


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

        fragmentVisitedScheduleBinding = FragmentVisitedScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        clientIdx = arguments?.getLong("clientIdx")!!
        scheduleIdx = arguments?.getLong("scheduleIdx")!!

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
                }
            }

            getClientInfo(userIdx,clientIdx)
            getSelectClientLastVisitDate("$userIdx", clientIdx)
            getSelectScheduleInfo("$userIdx", "$scheduleIdx")
        }

        return fragmentVisitedScheduleBinding.root
    }


}