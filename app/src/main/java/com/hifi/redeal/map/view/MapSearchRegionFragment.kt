package com.hifi.redeal.map.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMapSearchRegionBinding
import com.hifi.redeal.map.repository.ClientRepository

class MapSearchRegionFragment : Fragment() {

    lateinit var fragmentMapSearchRegionBinding: FragmentMapSearchRegionBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapSearchRegionBinding = FragmentMapSearchRegionBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        val TAG = "지역"

        ClientRepository.searchSiDo {
            Log.d(TAG,it.toString())
        }

        ClientRepository.searchSiGunGu(45){
            Log.d(TAG,it.toString())
        }

        return fragmentMapSearchRegionBinding.root
    }

}