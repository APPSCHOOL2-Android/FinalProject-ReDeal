package com.hifi.redeal.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMakeScheduleBinding

class MakeScheduleFragment : Fragment() {
    lateinit var fragmentMakeScheduleBinding: FragmentMakeScheduleBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMakeScheduleBinding = FragmentMakeScheduleBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        fragmentMakeScheduleBinding.run {
            makeScheduleTimePicker.run {
                // 시간 선택 이벤트 핸들러
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    
                }
            }
        }
        return fragmentMakeScheduleBinding.root
    }


}