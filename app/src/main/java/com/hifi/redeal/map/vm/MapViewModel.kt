package com.hifi.redeal.map.vm

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.R
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.model.ClientDataClass
import com.hifi.redeal.map.model.ScheduleDataClass
import com.hifi.redeal.map.repository.ClientRepository
import com.hifi.redeal.map.repository.MapRepository
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import java.text.SimpleDateFormat
import java.util.Calendar

class MapViewModel : ViewModel() {
    var currentSiGunGuList = MutableLiveData<MutableList<AdmVO>?>()
    var currentDongList = MutableLiveData<MutableList<AdmVO>?>()
    var currentSiDo = MutableLiveData<Int>()
    var currentSiGunGu = MutableLiveData<Int>()
    var currentDong = MutableLiveData<Int>()

    init {
        // 초기값 설정, 선택되지 않은 상태로 시작
        currentSiDo.value = -1
        currentSiGunGu.value = -1
        currentDong.value = -1
    }

    fun getSiGunGuList(admCode:String) {
        MapRepository.searchSiGunGu(admCode.toInt()) {
            currentSiGunGuList.value = it as MutableList<AdmVO>?
        }
    }

    fun getDongList(admCode:String) {
        MapRepository.searchDong(admCode.toInt()) {
            currentDongList.value = it as MutableList<AdmVO>?
        }
    }

}
