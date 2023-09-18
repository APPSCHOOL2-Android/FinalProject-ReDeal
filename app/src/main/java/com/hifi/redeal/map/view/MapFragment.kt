package com.hifi.redeal.map.view

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMapBinding
import com.hifi.redeal.databinding.RowMapClientListBinding
import com.hifi.redeal.map.repository.ClientRepository
import com.hifi.redeal.map.vm.ClientViewModel
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

    // 거래처 주소
    lateinit var clientViewModel: ClientViewModel
    var currentAddress: String? = null

    // 카카오 맵
    lateinit var kakaoMapTemp: KakaoMap
    private var centerPointLabel: Label? = null

    // 현재 위치
    var locationListener: LocationListener? = null
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBinding = FragmentMapBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity




        clientViewModel = ViewModelProvider(mainActivity)[ClientViewModel::class.java]
        clientViewModel.run {
            clientDataListByKeyWord.observe(mainActivity) {
//                Log.d("검색",it.toString())
                fragmentMapBinding.mapSearchRecyclerViewResult.adapter?.notifyDataSetChanged()
            }
        }


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
                    Log.d("지도2", "성공")
                    kakaoMapTemp = kakaoMap

                    centerPointLabel = kakaoMapTemp.labelManager!!.layer
                        .addLabel(
                            LabelOptions.from(kakaoMapTemp.cameraPosition!!.position)
                                .setStyles(R.drawable.ic_launcher_background)
                        )
                }

                override fun getZoomLevel(): Int {
                    return 2000
                }
            })

            mapFABMyLocation.setOnClickListener {
                moveCurrentLocation()
            }

            mapSearchView.run {
                addTransitionListener { searchView, previousState, newState ->
                    if (newState == SearchView.TransitionState.SHOWING) {
                        clientViewModel.resetClientList()
                    } else {
                        Log.d("지도주소", currentAddress.toString())
                        if (currentAddress != null) {
                            moveClientAddress(currentAddress!!)
                        }
                    }
                }

                editText.setOnEditorActionListener { textView, i, keyEvent ->
                    clientViewModel.getClientListByKeyword("1", text.toString())
                    true
                }
            }

            mapSearchRecyclerViewResult.run {
                adapter = SearchClientResultRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(
                    MaterialDividerItemDecoration(
                        context,
                        MaterialDividerItemDecoration.VERTICAL
                    )
                )

            }


        }


        return fragmentMapBinding.root
    }

    inner class SearchClientResultRecyclerViewAdapter :
        RecyclerView.Adapter<SearchClientResultRecyclerViewAdapter.ResultViewHolder>() {
        inner class ResultViewHolder(rowMapClientListBinding: RowMapClientListBinding) :
            RecyclerView.ViewHolder(rowMapClientListBinding.root) {

            val rowMapClientListName: TextView
            val rowMapClientListManagerName: TextView
            val rowMapClientListDateRecent: TextView
            val rowMapClientListDateRecentLayout: LinearLayout
            val rowMapClientListTransactionType: ImageView
            val rowMapClientListBtnToNavi: Button

            init {
                rowMapClientListName = rowMapClientListBinding.rowMapClientListName
                rowMapClientListManagerName = rowMapClientListBinding.rowMapClientListManagerName
                rowMapClientListDateRecent = rowMapClientListBinding.rowMapClientListDateRecent
                rowMapClientListTransactionType =
                    rowMapClientListBinding.rowMapClientListTransactionType
                rowMapClientListBtnToNavi = rowMapClientListBinding.rowMapClientListBtnToNavi
                rowMapClientListDateRecentLayout =
                    rowMapClientListBinding.rowMapClientListDateRecentLayout
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val rowMapClientListBinding = RowMapClientListBinding.inflate(layoutInflater)
            val allViewHolder = ResultViewHolder(rowMapClientListBinding)

            rowMapClientListBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            rowMapClientListBinding.root.setOnClickListener {
                val position = allViewHolder.adapterPosition
                currentAddress =
                    clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientAddress!!

                fragmentMapBinding.mapSearchView.hide()

            }

            return allViewHolder
        }

        override fun getItemCount(): Int {
            return clientViewModel.clientDataListByKeyWord.value?.size!!
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.rowMapClientListName.text =
                clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientName
            holder.rowMapClientListManagerName.text =
                clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientManagerName
            holder.rowMapClientListDateRecentLayout.visibility = View.GONE
//            holder.rowMapClientListDateRecent.text = clientViewModel.clientDataListByKeyWord.value?.get(position)?
        }
    }


    private fun moveCamera(position: LatLng) {
        kakaoMapTemp!!.moveCamera(
            CameraUpdateFactory.newCenterPosition(position),
            CameraAnimation.from(
                1500
            )
        )
    }

    fun moveClientAddress(addr: String) {
        ClientRepository.searchAddr(addr!!) {
            if (it != null) {
                val lat = it[0].y.toDouble()
                val long = it[0].x.toDouble()
                Log.d("주소 확인1", lat.toString())
                Log.d("주소 확인2", long.toString())
                moveCamera(LatLng.from(lat, long))
            }
        }
    }


    fun moveCurrentLocation() {
        val permissionCheck = ContextCompat
            .checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val lm = requireContext()
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            try {

                val location1 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val location2 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                // 기존에 추출된 정보가 있다면 일단 이것으로 먼저 사용한다.
                if (location1 != null) {
                    getMyLocation(location1)
                } else if (location2 != null) {
                    getMyLocation(location2)
                }

                locationListener = object : LocationListener {
                    // 위치가 새롭게 측정되면 호출되는 메서드
                    override fun onLocationChanged(p0: Location) {
                        getMyLocation(p0)
                    }
                }

                // GPS Provider가 사용 가능하다면 측정을 요청한다.
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    // 첫 번째 : 요청할 프로바이더
                    // 두 번째 : 측정할 시간 주기. 0을 넣어주면 가장 짧은 주기마다 측정을 한다. (단위 ms)
                    // 세 번째 : 측정할 거리 단위. 0을 넣어주면 가장 짧은 거리마다 측정을 한다. (단위 m)
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        locationListener!!
                    )
                }

                // Network Provider가 사용 가능하다면 측정을 요청한다.
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        locationListener!!
                    )
                }


            } catch (e: java.lang.NullPointerException) {
                Log.e("LOCATION_ERROR", e.toString())

                ActivityCompat.finishAffinity(requireActivity())

                val intent = Intent(context, MapFragment::class.java)
                startActivity(intent)
                System.exit(0)
            }
        } else {
            Toast.makeText(mainActivity, "위치 권한이 없습니다.", Toast.LENGTH_SHORT).show()
            requestPermissions(
                REQUIRED_PERMISSIONS,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun getMyLocation(location: Location) {
        val uLatitude = location!!.latitude
        val uLogitude = location!!.longitude
        Log.d("주소 확인3", uLatitude.toString())
        Log.d("주소 확인4", uLogitude.toString())
        moveCamera(LatLng.from(uLatitude, uLogitude))
    }


}