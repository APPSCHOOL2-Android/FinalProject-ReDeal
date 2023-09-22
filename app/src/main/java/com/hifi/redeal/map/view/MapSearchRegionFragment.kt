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

//        TODO("확인 버튼 클릭 시 현재 선택 되어 있는 지역들을 문자열로 만들어 clientViemodel의 currentAddress에 입력 후 프래그먼트 삭제")

        fragmentMapSearchRegionBinding.run {
            mapSearchRegionToolbar.run {
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT)
                }
            }

            // 서치뷰에서 엔터쳤을 때 동작
            mapSearchRegionSearchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        setCurrentAddress(query)

                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })


            MapRepository.searchSiDo { result ->
                mapSearchRegionRecyclerViewSiDo.run {
                    adapter = SearchRegionSiDoRecyclerViewAdapter(mainActivity, result!!.sortedBy { it.admCode })
                    layoutManager = LinearLayoutManager(context)

                }

            }

            mapSearchRegionBtnToMap.setOnClickListener {

                MapRepository.searchSiDo {
                    var regionResult = ""
                    if(mapViewModel.currentSiDoPosition.value!=-1){
                        regionResult += it!!.sortedBy { it.admCode }.get(mapViewModel.currentSiDoPosition.value!!).lowestAdmCodeNm
                    }

                    if(mapViewModel.currentSiGunGuPosition.value!=-1 && mapViewModel.currentSiGunGuList.value!!.isNotEmpty()){
                        regionResult += mapViewModel.currentSiGunGuList.value!!.get(mapViewModel.currentSiGunGuPosition.value!!).lowestAdmCodeNm

                    }
                    if(mapViewModel.currentDongPosition.value!=-1 && mapViewModel.currentDongList.value!!.isNotEmpty()){
                        regionResult += mapViewModel.currentDongList.value!!.get(mapViewModel.currentDongPosition.value!!).lowestAdmCodeNm

                    }

                    setCurrentAddress(regionResult)
                }

            }

        }


        return fragmentMapSearchRegionBinding.root
    }



    fun setCurrentAddress(addr: String) {
        Log.d("지역3",addr)
        MapRepository.searchAddr(addr!!) { list ->
            if (list?.isNotEmpty() == true && list != null) {
                val lat = list[0].y.toDouble()
                val long = list[0].x.toDouble()
                Log.d("주소 확인1", lat.toString())
                Log.d("주소 확인2", long.toString())
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
            // 확인 버튼을 클릭했을 때 수행할 동작
            dialog.dismiss() // 다이얼로그 닫기
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



}