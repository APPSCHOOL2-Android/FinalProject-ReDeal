package com.hifi.redeal.map.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
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

    lateinit var clientViewModel: ClientViewModel
    var clientAddress : String? = null

    lateinit var kakaoMapTemp: KakaoMap
    private var centerPointLabel: Label? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBinding = FragmentMapBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity



        clientViewModel = ViewModelProvider(mainActivity)[ClientViewModel::class.java]
        clientViewModel.run {
            clientDataListByKeyWord.observe(mainActivity){
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
                addTransitionListener { searchView, previousState, newState ->
                    if(newState == SearchView.TransitionState.SHOWING){
                        clientViewModel.resetClientList()
                    } else {
                        Log.d("지도주소",clientAddress.toString())
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
                addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))

            }



        }


        return fragmentMapBinding.root
    }

    inner class SearchClientResultRecyclerViewAdapter : RecyclerView.Adapter<SearchClientResultRecyclerViewAdapter.ResultViewHolder>(){
        inner class ResultViewHolder(rowMapClientListBinding: RowMapClientListBinding) : RecyclerView.ViewHolder(rowMapClientListBinding.root){

            val rowMapClientListName: TextView
            val rowMapClientListManagerName: TextView
            val rowMapClientListDateRecent: TextView
            val rowMapClientListDateRecentLayout : LinearLayout
            val rowMapClientListTransactionType : ImageView
            val rowMapClientListBtnToNavi : Button

            init{
                rowMapClientListName = rowMapClientListBinding.rowMapClientListName
                rowMapClientListManagerName = rowMapClientListBinding.rowMapClientListManagerName
                rowMapClientListDateRecent = rowMapClientListBinding.rowMapClientListDateRecent
                rowMapClientListTransactionType = rowMapClientListBinding.rowMapClientListTransactionType
                rowMapClientListBtnToNavi = rowMapClientListBinding.rowMapClientListBtnToNavi
                rowMapClientListDateRecentLayout = rowMapClientListBinding.rowMapClientListDateRecentLayout
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val rowMapClientListBinding = RowMapClientListBinding.inflate(layoutInflater)
            val allViewHolder = ResultViewHolder(rowMapClientListBinding)

            rowMapClientListBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            rowMapClientListBinding.root.setOnClickListener {
                val position = allViewHolder.adapterPosition
                clientAddress = clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientAddress!!

                fragmentMapBinding.mapSearchView.hide()

            }

            return allViewHolder
        }

        override fun getItemCount(): Int {
            return clientViewModel.clientDataListByKeyWord.value?.size!!
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.rowMapClientListName.text = clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientName
            holder.rowMapClientListManagerName.text = clientViewModel.clientDataListByKeyWord.value?.get(position)?.clientManagerName
            holder.rowMapClientListDateRecentLayout.visibility = View.GONE
//            holder.rowMapClientListDateRecent.text = clientViewModel.clientDataListByKeyWord.value?.get(position)?
        }
    }


    private fun moveCamera(position: LatLng) { kakaoMapTemp!!.moveCamera(
            CameraUpdateFactory.newCenterPosition(position),
            CameraAnimation.from(
                1500
            )
        )
    }



}