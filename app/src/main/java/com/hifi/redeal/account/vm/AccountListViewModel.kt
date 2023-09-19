package com.hifi.redeal.account.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hifi.redeal.account.model.ClientData
import com.hifi.redeal.account.repository.AccountListRepository

class AccountListViewModel: ViewModel() {
    val accountListRepository = AccountListRepository()

    val selectedTabItemPosState = MutableLiveData(-1)

    fun setSelectedTabItemPosState(position: Int) {
        selectedTabItemPosState.value = position
    }

    val tabItemCheckedListSort = mutableListOf(true, false, false, false)

    val tabItemDescListSort = mutableListOf(true, true, true, true)

    val clientList = MutableLiveData<List<ClientData>>()

    fun getClientList(userId: Long) {
        val filter = selectedTabItemPosState.value ?: -1

        val sortBy = tabItemCheckedListSort.indexOf(true)

        val descending = tabItemDescListSort[sortBy]

        accountListRepository.getClientList(userId, filter, sortBy, descending) {
            clientList.value = it
        }
    }
}