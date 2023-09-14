package com.hifi.redeal.map.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMapBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback


class MapFragment : Fragment() {
    lateinit var fragmentMapBinding: FragmentMapBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBinding = FragmentMapBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        fragmentMapBinding.run {
            mapBtnSearchRegion.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT,true,null)
            }
            mapKakao.start(object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    // 지도 API가 정상적으로 종료될 때 호출됨
                }

                override fun onMapError(error: Exception) {
                    // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                    Log.d("지도",error.toString())
                }
            }, object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    // 인증 후 API가 정상적으로 실행될 때 호출됨
                }

                override fun getZoomLevel(): Int {
                    return 100
                }
            })


        }



        return fragmentMapBinding.root
    }

}