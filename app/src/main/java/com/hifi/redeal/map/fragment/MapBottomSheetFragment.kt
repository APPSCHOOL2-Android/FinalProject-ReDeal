package com.hifi.redeal.map.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMapBottomSheetBinding


class MapBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var fragmentMapBottomSheetBinding: FragmentMapBottomSheetBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBottomSheetBinding =  FragmentMapBottomSheetBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity
        return fragmentMapBottomSheetBinding.root
    }


}