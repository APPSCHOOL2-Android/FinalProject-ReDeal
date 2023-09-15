package com.hifi.redeal.map.view

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMapBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions


class MapFragment : Fragment() {
    lateinit var fragmentMapBinding: FragmentMapBinding
    lateinit var mainActivity: MainActivity
    lateinit var kakaoMapTemp: KakaoMap

    private val kakaoMap: KakaoMap? = null
    private var centerPointLabel: Label? = null
    private val ckAnimate: CheckBox? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBinding = FragmentMapBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        fragmentMapBinding.run {
            mapBtnSearchRegion.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT, true, null)
            }

            mapKakao.start(object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    // 지도 API가 정상적으로 종료될 때 호출됨
                }

                override fun onMapError(error: Exception) {
                    // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                    Log.d("지도", error.toString())
                }
            }, object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    // 인증 후 API가 정상적으로 실행될 때 호출
                    Log.d("지도2","성공")
                    kakaoMapTemp = kakaoMap

                    centerPointLabel = kakaoMapTemp.labelManager!!.layer
                        .addLabel(
                            LabelOptions.from(kakaoMapTemp.cameraPosition!!.position)
                                .setStyles(R.drawable.ic_launcher_background)
                        )
                }

                override fun getZoomLevel(): Int {
                    return 100
                }
            })

            mapFABMyLocation.setOnClickListener {
                moveCamera(LatLng.from(37.497838, 127.027576))
            }

            mapSearchView.run {
                editText.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        // 엔터 키를 눌렀을 때 실행할 동작


                    }
                    true
                }
            }


        }




        return fragmentMapBinding.root
    }


    private fun moveCamera(position: LatLng) { kakaoMapTemp!!.moveCamera(
            CameraUpdateFactory.newCenterPosition(position),
            CameraAnimation.from(
                1500
            )
        )
    }

}