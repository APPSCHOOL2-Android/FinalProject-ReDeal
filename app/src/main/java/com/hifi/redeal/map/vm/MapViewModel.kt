package com.hifi.redeal.map.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.repository.MapRepository

class MapViewModel : ViewModel() {
    var currentSiGunGuList = MutableLiveData<MutableList<AdmVO>?>()
    var currentDongList = MutableLiveData<MutableList<AdmVO>?>()
    var currentSiDoPosition = MutableLiveData<Int>()
    var currentSiGunGuPosition = MutableLiveData<Int>()
    var currentDongPosition = MutableLiveData<Int>()

    init {
        currentSiDoPosition.value = -1
        currentSiGunGuPosition.value = -1
        currentDongPosition.value = -1
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
