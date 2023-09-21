package com.hifi.redeal.map.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMapSearchRegionBinding
import com.hifi.redeal.map.adapter.SearchRegionDongRecyclerViewAdapter
import com.hifi.redeal.map.adapter.SearchRegionSiDoRecyclerViewAdapter
import com.hifi.redeal.map.adapter.SearchRegionSiGunGuRecyclerViewAdapter
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.repository.MapRepository
import com.hifi.redeal.map.vm.MapViewModel

class MapSearchRegionFragment : Fragment() {

    lateinit var fragmentMapSearchRegionBinding: FragmentMapSearchRegionBinding
    lateinit var mainActivity: MainActivity
    lateinit var mapViewModel: MapViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapSearchRegionBinding = FragmentMapSearchRegionBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        mapViewModel = ViewModelProvider(mainActivity)[MapViewModel::class.java]

        mapViewModel.run {
            currentSiGunGuList.observe(mainActivity) {
                currentDongList.value?.clear()
                fragmentMapSearchRegionBinding.mapSearchRegionRecyclerViewSiGunGu.run {
                    adapter = SearchRegionSiGunGuRecyclerViewAdapter(mainActivity, it!!)
                    layoutManager = LinearLayoutManager(context)
                }

            }

            currentDongList.observe(mainActivity) {
                fragmentMapSearchRegionBinding.mapSearchRegionRecyclerViewDong.run {
                    adapter = SearchRegionDongRecyclerViewAdapter(mainActivity, it!!)
                    layoutManager = LinearLayoutManager(context)
                }
            }
        }


        fragmentMapSearchRegionBinding.run {
            mapSearchRegionToolbar.run {
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT)
                }
            }
            MapRepository.searchSiDo { result ->
                mapSearchRegionRecyclerViewSiDo.run {
                    adapter = SearchRegionSiDoRecyclerViewAdapter(mainActivity, result!!.sortedBy { it.admCode })
                    layoutManager = LinearLayoutManager(context)

                }

            }

        }


        return fragmentMapSearchRegionBinding.root
    }

    private fun admVOS(): MutableList<AdmVO> {
        val admVo1 = AdmVO("경상남도", "48", "경상남도")
        val admVo2 = AdmVO("경상남도", "47", "경상북도")
        val admVo3 = AdmVO("전라남도", "46", "전라남도")
        val admVo4 = AdmVO("전라남도", "45", "전라북도")
        val admVo5 = AdmVO("충청남도", "44", "충청남도")
        val admVo6 = AdmVO("충청남도", "42", "강원도")
        val admVo7 = AdmVO("충청남도", "50", "제주특별자치도")
        val admList = mutableListOf(admVo1, admVo2, admVo3, admVo4, admVo5, admVo6, admVo7)
        return admList
    }

}