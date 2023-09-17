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
import com.hifi.redeal.databinding.FragmentEditScheduleBinding
import com.hifi.redeal.schedule.model.ScheduleData
import com.hifi.redeal.schedule.vm.ScheduleVM


class EditScheduleFragment : Fragment() {

    lateinit var fragmentEditScheduleBinding: FragmentEditScheduleBinding
    lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentEditScheduleBinding = FragmentEditScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        setViewModel()

        return fragmentEditScheduleBinding.root
    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{
            selectEditScheduleData.observe(requireActivity()){
                fragmentEditScheduleBinding.editScheduleClientState.visibility = View.VISIBLE
//                when(it.){
//                    1L -> {
//                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trading)
//                    }
//                    2L -> {
//                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
//                    }
//                    3L -> {
//                        fragmentEditScheduleBinding.editScheduleClientState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
//                    }
//                    else -> fragmentEditScheduleBinding.editScheduleClientState.visibility = View.GONE
//                }
//                fragmentEditScheduleBinding.editScheduleClientInfo.text = "$clientName $clientManagerName"
//                if(isBookmark){
//                    fragmentEditScheduleBinding.editScheduleClientBookmark.setBackgroundResource(R.drawable.star_fill_24px)
//                    fragmentEditScheduleBinding.editScheduleClientBookmark.visibility = View.VISIBLE
//                } else {
//                    fragmentEditScheduleBinding.editScheduleClientBookmark.visibility = View.GONE
//                }
            }

        }
        scheduleVM.setEditScheduleData()
    }

}