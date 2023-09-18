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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMapBinding
import com.hifi.redeal.databinding.RowMapClientListBinding
import com.hifi.redeal.map.adapter.MapBottomSheetRecyclerViewAdapter
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.hifi.redeal.map.vm.ClientViewModel
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions


class MapFragment : Fragment(),KakaoMap.OnCameraMoveEndListener, KakaoMap.OnCameraMoveStartListener {
    lateinit var fragmentMapBinding: FragmentMapBinding
    lateinit var mainActivity: MainActivity
    lateinit var behavior: BottomSheetBehavior<LinearLayout>
    // 거래처 주소
    lateinit var clientViewModel: ClientViewModel


    var currentAddress: String? = null
    // 카카오 맵
    lateinit var kakaoMapTemp: KakaoMap
    private var centerPointLabel: Label? = null
    private lateinit var labels: Array<Label>
    private var labelLayer: LabelLayer? = null
    private val selectedList: List<Label> = ArrayList()


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
            clientDataListAll.observe(mainActivity){
                Log.d("하단",it.toString())
                fragmentMapBinding.mapBottomSheet.run {
                    mapBottomSheetTextViewEmpty.visibility = View.GONE
                    mapBottomSheetRecyclerView.run {
                        visibility = View.VISIBLE
                        adapter = MapBottomSheetRecyclerViewAdapter(clientViewModel.clientDataListAll.value!!)
                        layoutManager = LinearLayoutManager(context)
                        addItemDecoration(
                            MaterialDividerItemDecoration(
                                context,
                                MaterialDividerItemDecoration.VERTICAL
                            )
                        )
                    }
                }
            }

            clientDataListLabel.observe(mainActivity){
                Log.d("라벨 테스트2",clientViewModel.clientDataListLabel.value.toString())
                labels = kakaoMapTemp.labelManager!!.layer.addLabels(clientViewModel.clientDataListLabel.value)
            }


            selectedButtonId.observe(mainActivity) { selectedButtonId ->

                fragmentMapBinding.mapBottomSheet.run {
                    // 모든 버튼의 원래 스타일로 초기화
                    mapBottomSheetTabAll.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabAll.setTextColor(ContextCompat.getColor(mainActivity, R.color.primary20))

                    mapBottomSheetTabBookMark.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabBookMark.setTextColor(ContextCompat.getColor(mainActivity, R.color.primary20))

                    mapBottomSheetTabVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabVisit.setTextColor(ContextCompat.getColor(mainActivity, R.color.primary20))

                    // 선택된 버튼의 스타일 변경
                    val selectedButton = mainActivity.findViewById<Button>(selectedButtonId)
                    selectedButton.setBackgroundResource(R.drawable.btn_round_primary20)
                    selectedButton.setTextColor(ContextCompat.getColor(mainActivity, R.color.white))
                }

            }
        }



        fragmentMapBinding.run {
            mapBtnSearchRegion.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MAP_SEARCH_REGION_FRAGMENT, true, null)
            }
            persistentBottomSheetEvent()

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

                    kakaoMap?.setOnCameraMoveStartListener(this@MapFragment)
                    kakaoMap?.setOnCameraMoveEndListener(this@MapFragment)
                    centerPointLabel = kakaoMap.labelManager!!.layer
                        .addLabel(
                            LabelOptions.from(kakaoMap.cameraPosition!!.position)
                                .setStyles(R.drawable.red_dot_marker)
                        )
                    clientViewModel.getClientListLabel("1")





                }

                override fun getZoomLevel(): Int {
                    return 17
                }
            })

            mapFABMyLocation.setOnClickListener {
                moveCurrentLocation()
            }

            mapSearchView.run {
                addTransitionListener { searchView, previousState, newState ->
                    if (newState == SearchView.TransitionState.SHOWING) {
                        clientViewModel.resetClientListByKeyword()
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

            mapBottomSheet.run {
                // 클릭된 버튼 ID를 뷰모델로 업데이트
                mapBottomSheetTabAll.setOnClickListener {
                    clientViewModel.resetClientList()
                    clientViewModel.getClientListAll("1")
                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabAll)
                }

                mapBottomSheetTabBookMark.setOnClickListener {
                    clientViewModel.clientDataListAll.value = clientViewModel.clientDataListAll.value?.filter { it.isBookmark==true } as MutableList<ClientDataClass>
                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabBookMark)
                }

                mapBottomSheetTabVisit.setOnClickListener {
                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabVisit)
                }
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
            val rowMapClientListBookMark : ImageView


            init {
                rowMapClientListName = rowMapClientListBinding.rowMapClientListName
                rowMapClientListManagerName = rowMapClientListBinding.rowMapClientListManagerName
                rowMapClientListDateRecent = rowMapClientListBinding.rowMapClientListDateRecent
                rowMapClientListTransactionType =
                    rowMapClientListBinding.rowMapClientListTransactionType
                rowMapClientListBtnToNavi = rowMapClientListBinding.rowMapClientListBtnToNavi
                rowMapClientListDateRecentLayout =
                    rowMapClientListBinding.rowMapClientListDateRecentLayout
                rowMapClientListBookMark =
                    rowMapClientListBinding.rowMapClientListBookMark
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
            if (clientViewModel.clientDataListByKeyWord.value?.get(position)?.isBookmark==false){
                holder.rowMapClientListBookMark.visibility = View.INVISIBLE
            }
        }
    }


    private fun moveCamera(position: LatLng) {
        kakaoMapTemp!!.moveCamera(
            CameraUpdateFactory.newCenterPosition(position)
        )
        //        centerPointLabel = kakaoMapTemp.labelManager!!.layer
//            .addLabel(
//                LabelOptions.from(kakaoMapTemp.cameraPosition!!.position)
//                    .setStyles(R.drawable.red_dot_marker)
//            )

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

//                locationListener = object : LocationListener {
//                    // 위치가 새롭게 측정되면 호출되는 메서드
//                    override fun onLocationChanged(p0: Location) {
//                        getMyLocation(p0)
//                    }
//                }
//
//                // GPS Provider가 사용 가능하다면 측정을 요청한다.
//                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
//                    // 첫 번째 : 요청할 프로바이더
//                    // 두 번째 : 측정할 시간 주기. 0을 넣어주면 가장 짧은 주기마다 측정을 한다. (단위 ms)
//                    // 세 번째 : 측정할 거리 단위. 0을 넣어주면 가장 짧은 거리마다 측정을 한다. (단위 m)
//                    lm.requestLocationUpdates(
//                        LocationManager.GPS_PROVIDER,
//                        0,
//                        0f,
//                        locationListener!!
//                    )
//                }
//
//                // Network Provider가 사용 가능하다면 측정을 요청한다.
//                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
//                    lm.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER,
//                        0,
//                        0f,
//                        locationListener!!
//                    )
//                }


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

    private fun persistentBottomSheetEvent() {
        val TAG = "하단"
        behavior = BottomSheetBehavior.from(fragmentMapBinding.mapBottomSheet.mapBottomSheetLayout)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 되는 도중 계속 호출
                // called continuously while dragging
                Log.d(TAG, "onStateChanged: 드래그 중")
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED-> {
                        Log.d(TAG, "onStateChanged: 접음")
                        clientViewModel.selectedButtonId.value=R.id.mapBottomSheetTabAll
                    }
                    BottomSheetBehavior.STATE_DRAGGING-> {
                        Log.d(TAG, "onStateChanged: 드래그")
                    }
                    BottomSheetBehavior.STATE_EXPANDED-> {
                        Log.d(TAG, "onStateChanged: 펼침")
                        clientViewModel.getClientListAll("1")
//                        showIconLabel("test")

                    }
                    BottomSheetBehavior.STATE_HIDDEN-> {
                        Log.d(TAG, "onStateChanged: 숨기기")
                    }
                    BottomSheetBehavior.STATE_SETTLING-> {
                        Log.d(TAG, "onStateChanged: 고정됨")
                    }
                }
            }
        })
    }

    private fun getSelectedPoints(): Array<LatLng?>? {
        val count: Int = selectedList.size
        val points = arrayOfNulls<LatLng>(count)
        for (i in selectedList.indices) {
            points[i] = selectedList.get(i).getPosition()
        }
        return points
    }

    override fun onCameraMoveEnd(
        kakaoMap: KakaoMap,
        cameraPosition: CameraPosition,
        gestureType: GestureType
    ) {
        centerPointLabel = kakaoMap.labelManager!!.layer
            .addLabel(
                LabelOptions.from(kakaoMap.cameraPosition!!.position)
                    .setStyles(R.drawable.red_dot_marker)
            )

    }

    override fun onCameraMoveStart(kakaoMap: KakaoMap, gestureType: GestureType) {
        centerPointLabel?.remove()
    }

}