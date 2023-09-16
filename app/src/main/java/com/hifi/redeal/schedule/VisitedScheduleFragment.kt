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

            getClientInfo(userIdx,clientIdx)
        }

        return fragmentVisitedScheduleBinding.root
    }


}