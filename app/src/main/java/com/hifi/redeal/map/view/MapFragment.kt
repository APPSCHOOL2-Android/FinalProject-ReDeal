package com.hifi.redeal.map.view

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.repository.model.Coordinate
import com.hifi.redeal.databinding.FragmentMapBinding
import com.hifi.redeal.databinding.RowMapClientListBinding
import com.hifi.redeal.map.model.ScheduleDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.hifi.redeal.map.repository.MapRepository
import com.hifi.redeal.map.vm.ClientViewModel
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import java.text.SimpleDateFormat


class MapFragment : Fragment(), KakaoMap.OnCameraMoveEndListener,
    KakaoMap.OnCameraMoveStartListener {
    lateinit var fragmentMapBinding: FragmentMapBinding
    lateinit var mainActivity: MainActivity
    lateinit var behavior: BottomSheetBehavior<LinearLayout>

    // 거래처 주소
    lateinit var clientViewModel: ClientViewModel

    // 카카오 맵
    var kakaoMapTemp: KakaoMap? = null
    private var centerPointLabel: Label? = null


    // 현재 위치
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

            clientDataListByKeyWord.observe(viewLifecycleOwner) {
                fragmentMapBinding.mapSearchRecyclerViewResult.adapter?.notifyDataSetChanged()
            }
            clientDataListAll.observe(viewLifecycleOwner) {
                fragmentMapBinding.mapBottomSheet.run {
                    if (clientDataListAll.value!!.isEmpty() || clientDataListAll.value == null) {
                        mapBottomSheetTextViewEmpty.visibility = View.VISIBLE
                        mapBottomSheetRecyclerView.visibility = View.GONE
                    } else {
                        mapBottomSheetTextViewEmpty.visibility = View.GONE
                        mapBottomSheetRecyclerView.run {
                            visibility = View.VISIBLE
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }



            selectedButtonId.observe(viewLifecycleOwner) { selectedButtonId ->

                fragmentMapBinding.mapBottomSheet.run {
                    mapBottomSheetTabAll.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabAll.setTextColor(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.primary20
                        )
                    )

                    mapBottomSheetTabBookMark.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabBookMark.setTextColor(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.primary20
                        )
                    )

                    mapBottomSheetTabVisit.setBackgroundResource(R.drawable.btn_round_nofill_primary20)
                    mapBottomSheetTabVisit.setTextColor(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.primary20
                        )
                    )

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

            startKakaoMap()

            mapFABMyLocation.setOnClickListener {
                setCurrentLocation()
            }


            mapSearchView.run {

                addTransitionListener { searchView, previousState, newState ->
                    if (newState == SearchView.TransitionState.SHOWING) {
                        clientViewModel.resetClientListByKeyword()
                        val searchViewLayoutParams =
                            mapSearchView.layoutParams as CoordinatorLayout.LayoutParams

                        searchViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                        mapSearchView.layoutParams = searchViewLayoutParams
                    }

                }

                editText.setOnEditorActionListener { textView, i, keyEvent ->
                    clientViewModel.getClientListByKeyword(Firebase.auth.uid!!, text.toString())

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

                mapBottomSheetRecyclerView.run {
                    adapter = MapBottomSheetRecyclerViewAdapter()
                    layoutManager = LinearLayoutManager(context)
                    addItemDecoration(
                        MaterialDividerItemDecoration(
                            context,
                            MaterialDividerItemDecoration.VERTICAL
                        )
                    )
                }

                mapBottomSheetTabAll.setOnClickListener {
                    clientViewModel.getClientListAll(Firebase.auth.uid!!)

                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabAll)
                }

                mapBottomSheetTabBookMark.setOnClickListener {
                    clientViewModel.getClientListBookMark(Firebase.auth.uid!!)
                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabBookMark)
                }

                mapBottomSheetTabVisit.setOnClickListener {
                    clientViewModel.getClientListTodayVisit(Firebase.auth.uid!!)
                    clientViewModel.setSelectedButton(R.id.mapBottomSheetTabVisit)
                }
            }

        }


        return fragmentMapBinding.root
    }

    private fun FragmentMapBinding.startKakaoMap() {
        mapKakao.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception) {
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                kakaoMapTemp = kakaoMap

                kakaoMap?.setOnCameraMoveStartListener(this@MapFragment)
                kakaoMap?.setOnCameraMoveEndListener(this@MapFragment)
                centerPointLabel = kakaoMap.labelManager!!.layer
                    .addLabel(
                        LabelOptions.from(kakaoMap.cameraPosition!!.position)
                            .setStyles(R.drawable.red_dot_marker)
                    )
                clientViewModel.run {
                    getClientListLabel(Firebase.auth.uid!!)
                    clientDataListLabel.observe(viewLifecycleOwner) {
                        for(i in clientDataListLabel.value!!){
                            kakaoMap.mapWidgetManager?.infoWindowLayer?.addInfoWindow(i)
                        }

                    }
                    currentAddress.observe(viewLifecycleOwner) {
                        if (currentAddress != null) {
                            moveCamera(currentAddress.value!!)
                        }
                    }
                }


            }

            override fun getZoomLevel(): Int {
                return 17
            }

        })
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
            val rowMapClientListBookMark: ImageView


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
                val regex = "\\([^)]*\\)"
                val clientAddr =
                    clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientAddress!!.replace(
                        regex.toRegex(),
                        ""
                    )
                setClientAddress(clientAddr)

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
            if (clientViewModel.clientDataListByKeyWord.value?.get(position)?.isBookmark == false) {
                holder.rowMapClientListBookMark.visibility = View.INVISIBLE
            }
            holder.rowMapClientListBtnToNavi.visibility = View.INVISIBLE
        }
    }

    inner class MapBottomSheetRecyclerViewAdapter :
        RecyclerView.Adapter<MapBottomSheetRecyclerViewAdapter.ResultViewHolder>() {
        inner class ResultViewHolder(rowMapClientListBinding: RowMapClientListBinding) :
            RecyclerView.ViewHolder(rowMapClientListBinding.root) {

            val rowMapClientListName: TextView
            val rowMapClientListManagerName: TextView
            val rowMapClientListDateRecent: TextView
            val rowMapClientListDateRecentLayout: LinearLayout
            val rowMapClientListTransactionType: ImageView
            val rowMapClientListBtnToNavi: Button
            val rowMapClientListBookMark: ImageView

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
            val rowMapClientListBinding = RowMapClientListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            val allViewHolder = ResultViewHolder(rowMapClientListBinding)

            rowMapClientListBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            rowMapClientListBinding.root.setOnClickListener {
                val position = allViewHolder.adapterPosition
                val regex = "\\([^)]*\\)"

                val clientAddr =
                    clientViewModel.clientDataListAll.value?.get(position)?.clientAddress!!.replace(
                        regex.toRegex(),
                        ""
                    )
                setClientAddress(clientAddr)
                BottomSheetBehavior.from(fragmentMapBinding.mapBottomSheet.mapBottomSheetLayout).state =
                    BottomSheetBehavior.STATE_COLLAPSED
            }



            return allViewHolder
        }

        override fun getItemCount(): Int {
            return clientViewModel.clientDataListAll.value?.size!!
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.rowMapClientListName.text =
                clientViewModel.clientDataListAll.value?.get(position)?.clientName
            holder.rowMapClientListManagerName.text =
                clientViewModel.clientDataListAll.value?.get(position)?.clientManagerName
            holder.rowMapClientListBtnToNavi.setOnClickListener {
                val clientAddr = clientViewModel.clientDataListAll.value?.get(position)?.clientAddress
                if(!clientAddr.isNullOrBlank()){
                    getTMapAddress(clientAddr)
                }

            }

            when (clientViewModel.clientDataListAll.value?.get(position)?.clientState) {
                1L -> {
                    holder.rowMapClientListTransactionType.setImageResource(R.drawable.circle_24px_primary20)
                }

                2L -> {
                    holder.rowMapClientListTransactionType.setImageResource(R.drawable.circle_24px_primary50)
                }

                3L -> {
                    holder.rowMapClientListTransactionType.setImageResource(R.drawable.circle_24px_primary80)
                }

                else -> {
                    holder.rowMapClientListTransactionType.setImageResource(R.drawable.circle_24px_primary20)
                }
            }

            if (clientViewModel.clientDataListAll.value?.get(position)?.isBookmark == false) {
                holder.rowMapClientListBookMark.visibility = View.INVISIBLE
            } else {
                holder.rowMapClientListBookMark.visibility = View.VISIBLE
            }
            val clientIdx = clientViewModel.clientDataListAll.value?.get(position)?.clientIdx

            val scheduleTempList = mutableListOf<ScheduleDataClass>()
            getScheduleData(clientIdx, scheduleTempList, holder)
        }

        private fun getScheduleData(
            clientIdx: Long?,
            scheduleTempList: MutableList<ScheduleDataClass>,
            holder: ResultViewHolder
        ) {
            ClientRepository.getScheduleListByClientAndUser(Firebase.auth.uid!!, clientIdx!!) {
                for (snapshot in it.result.documents) {
                    if (snapshot.getBoolean("isVisitSchedule")!!
                            .equals(true) && snapshot.getBoolean("isScheduleFinish")!!.equals(true)
                    ) {
                        var item = snapshot.toObject(ScheduleDataClass::class.java)
                        scheduleTempList.add(item!!)
                    }
                }
                if (scheduleTempList.isNotEmpty()) {
                    holder.rowMapClientListDateRecentLayout.visibility = View.VISIBLE
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd")
                    holder.rowMapClientListDateRecent.text =
                        dateFormat.format(scheduleTempList.get(scheduleTempList.size - 1).scheduleFinishTime)
                } else {
                    holder.rowMapClientListDateRecentLayout.visibility = View.GONE
                }
            }
        }
    }

    fun getTMapAddress(addr : String){
        MapRepository.getFullAddrGeocoding(addr){ coordinate ->
            if(coordinate!=null){
                goToTMap(coordinate!!)
            }
        }
    }
    private fun goToTMap(coordinate: Coordinate) {
        android.app.AlertDialog.Builder(mainActivity)
            .setTitle("길 안내")
            .setMessage("티맵 길 안내를 시작하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                if (mainActivity.tMapTapi.isTmapApplicationInstalled) {
                    mainActivity.tMapTapi.invokeRoute(
                        coordinate.newBuildingName,
                        coordinate.newLon.toFloat(),
                        coordinate.newLat.toFloat()
                    )
                } else {
                    Snackbar.make(
                        fragmentMapBinding.root,
                        "티맵 앱 설치 후 다시 시도해주세요",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction("설치하기") {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("market://details?id=com.skt.tmap.ku")
                            startActivity(intent)
                        }.apply {
                            anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                        }
                        .show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun setClientAddress(addr: String) {
        MapRepository.searchAddr(addr!!) { list ->
            if (list?.isNotEmpty() == true && list != null) {
                val lat = list[0].y.toDouble()
                val long = list[0].x.toDouble()
                clientViewModel.currentAddress.value = LatLng.from(lat, long)
            } else {
                MapRepository.getFullAddrGeocoding(addr!!){
                    val lat = it!!.newLat.toDouble()
                    val long = it!!.newLon.toDouble()
                    clientViewModel.currentAddress.value = LatLng.from(lat, long)

                }
            }
        }
    }


    fun setCurrentLocation() {
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
                    val latlng1 = locationToLatLng(location1)
                    clientViewModel.currentAddress.value = LatLng.from(latlng1)
                } else if (location2 != null) {
                    val latlng2 = locationToLatLng(location2)
                    clientViewModel.currentAddress.value = LatLng.from(latlng2)
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

    private fun locationToLatLng(location1: Location): LatLng {
        val lat = location1.latitude
        val long = location1.longitude
        val latlng = LatLng.from(lat, long)
        return latlng
    }


    private fun persistentBottomSheetEvent() {
        behavior = BottomSheetBehavior.from(fragmentMapBinding.mapBottomSheet.mapBottomSheetLayout)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        clientViewModel.selectedButtonId.value = R.id.mapBottomSheetTabAll
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        clientViewModel.getClientListAll(Firebase.auth.uid!!)
                    }

                }
            }
        })
    }


    private fun moveCamera(position: LatLng) {
        if (kakaoMapTemp == null) {
            SystemClock.sleep(100)
        } else {
            kakaoMapTemp!!.moveCamera(
                CameraUpdateFactory.newCenterPosition(position)
            )
        }

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