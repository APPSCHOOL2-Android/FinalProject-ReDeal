package com.hifi.redeal.map.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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




        fragmentMapBottomSheetBinding.run {

            val distanceAdapter = ArrayAdapter(requireContext(), R.layout.item_map_bottom_sheet_distance, mutableListOf("주변 4km","주변 8km"))
            (mapBottomSheetDistanceList.editText as AutoCompleteTextView).setAdapter(distanceAdapter)
            // AutoCompleteTextView의 값이 변경될 때마다 호출되는 리스너를 추가
            mapBottomSheetDistanceList.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {

                }
            })
            mapBottomSheetLayoutClient.run {

            }
        }
        return fragmentMapBottomSheetBinding.root
    }


}