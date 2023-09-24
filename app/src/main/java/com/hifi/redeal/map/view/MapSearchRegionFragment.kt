package com.hifi.redeal.map.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
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
import com.hifi.redeal.map.vm.ClientViewModel
import com.hifi.redeal.map.vm.MapViewModel
import com.kakao.vectormap.LatLng

class MapSearchRegionFragment : Fragment() {

    lateinit var fragmentMapSearchRegionBinding: FragmentMapSearchRegionBinding
    lateinit var mainActivity: MainActivity
    lateinit var mapViewModel: MapViewModel
    lateinit var clientViewModel : ClientViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapSearchRegionBinding = FragmentMapSearchRegionBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        mapViewModel = ViewModelProvider(mainActivity)[MapViewModel::class.java]
        clientViewModel = ViewModelProvider(mainActivity)[ClientViewModel::class.java]

        mapViewModel.run {
            currentSiGunGuList.observe(viewLifecycleOwner) {
                currentDongList.value?.clear()
                fragmentMapSearchRegionBinding.mapSearchRegionRecyclerViewSiGunGu.run {
                    adapter = SearchRegionSiGunGuRecyclerViewAdapter(mainActivity, it!!)
                    layoutManager = LinearLayoutManager(context)
                }

            }

            currentDongList.observe(viewLifecycleOwner) {
                fragmentMapSearchRegionBinding.mapSearchRegionRecyclerViewDong.run {
                    adapter = SearchRegionDongRecyclerViewAdapter(mainActivity, it!!)
                    layoutManager = LinearLayoutManager(context)
                }
            }

            currentSiDoPosition.observe(viewLifecycleOwner){
                fragmentMapSearchRegionBinding.mapSearchRegionBtnToMap.isEnabled =
                    !(mapViewModel.currentSiDoPosition.value==-1 && fragmentMapSearchRegionBinding.mapSearchRegionSearchView.query.isNullOrBlank())
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

            mapSearchRegionSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    mapSearchRegionBtnToMap.isEnabled =
                        !(mapViewModel.currentSiDoPosition.value==-1 && newText.isNullOrBlank())
                    return true
                }
            })

            mapSearchRegionBtnToMap.run{
                setOnClickListener {
                    fragmentMapSearchRegionBinding.mapSearchRegionBtnToMap.isEnabled =
                        !(mapViewModel.currentSiDoPosition.value==-1 && fragmentMapSearchRegionBinding.mapSearchRegionSearchView.query.isNullOrBlank())
                    if (mapViewModel.currentSiDoPosition.value!=-1 && mapSearchRegionSearchView.query.isNotBlank()){
                        showDialog("입력 오류","검색 혹은 지역 선택 둘 중 하나만 해주세요.",mainActivity)
                        mapViewModel.currentSiDoPosition.value==-1
                        mapSearchRegionSearchView.clearFocus()
                    } else {
                        if(mapViewModel.currentSiDoPosition.value==-1){
                            setCurrentAddress( mapSearchRegionSearchView.query.toString())
                        } else {
                            MapRepository.searchSiDo {
                                var regionResult = ""
                                if (mapViewModel.currentSiDoPosition.value != -1) {
                                    regionResult += it!!.sortedBy { it.admCode }
                                        .get(mapViewModel.currentSiDoPosition.value!!).lowestAdmCodeNm
                                }

                                if (mapViewModel.currentSiGunGuPosition.value != -1 && mapViewModel.currentSiGunGuList.value!!.isNotEmpty()) {
                                    regionResult += mapViewModel.currentSiGunGuList.value!!.get(
                                        mapViewModel.currentSiGunGuPosition.value!!
                                    ).lowestAdmCodeNm

                                }
                                if (mapViewModel.currentDongPosition.value != -1 && mapViewModel.currentDongList.value!!.isNotEmpty()) {
                                    regionResult += mapViewModel.currentDongList.value!!.get(
                                        mapViewModel.currentDongPosition.value!!
                                    ).lowestAdmCodeNm

                                }

                                setCurrentAddress(regionResult)
                            }
                        }
                    }

                }
            }

        }


        return fragmentMapSearchRegionBinding.root
    }



    fun setCurrentAddress(addr: String) {
        MapRepository.searchAddr(addr!!) { list ->
            if (list?.isNotEmpty() == true && list != null) {
                val lat = list[0].y.toDouble()
                val long = list[0].x.toDouble()
                clientViewModel.currentAddress.value = LatLng.from(lat,long)
                mapViewModel.run {
                    currentSiDoPosition.value=-1
                    currentSiGunGuList.value= mutableListOf()
                    currentDongList.value= mutableListOf()
                }
                mainActivity.removeFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT)
            }else{
                showDialog("주소 오류","지역 명으로 입력해주세요.",mainActivity)
            }
        }
    }
    fun showDialog(title:String,message:String,context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("확인") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



}