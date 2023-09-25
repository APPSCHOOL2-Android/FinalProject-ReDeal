package com.hifi.redeal.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.hifi.redeal.transaction.model.ClientSimpleData
import com.hifi.redeal.transaction.model.customTransactionData

class TransactionViewModel: ViewModel() {

    var transactionList = MutableLiveData<MutableList<customTransactionData>>()
    var tempTransactionList = mutableListOf<customTransactionData>()

    var clientSimpleDataListVM = MutableLiveData<MutableList<ClientSimpleData>>()
    var tempClientSimpleDataList = mutableListOf<ClientSimpleData>()

    var nextTransactionIdx = 0L
    fun getNextTransactionIdx(uid: String){
        TransactionRepository.getNextTransactionIdx(uid){
            for(c1 in it.result){
                nextTransactionIdx = c1["transactionIdx"] as Long + 1L
            }
        }
    }
    fun getAllTransactionData(uid: String){
        tempTransactionList.clear()
        TransactionRepository.getAllTransactionData(uid,{
            for(c1 in it.result){

                val clientIdx = c1["clientIdx"] as Long
                val date = c1["date"] as Timestamp
                val isDeposit = c1["isDeposit"] as Boolean
                val transactionAmountReceived = c1["transactionAmountReceived"] as String
                val transactionIdx = c1["transactionIdx"] as Long
                val transactionItemCount = c1["transactionItemCount"] as Long
                val transactionItemPrice = c1["transactionItemPrice"] as String
                val transactionName = c1["transactionName"] as String

                val newTransactionData = customTransactionData(clientIdx, date, isDeposit, transactionAmountReceived, transactionIdx, transactionItemCount, transactionItemPrice, transactionName, null)
                tempTransactionList.add(newTransactionData)
                transactionList.postValue(tempTransactionList)
            }
        },{
            tempTransactionList.forEach{TransactionData ->
                TransactionRepository.getClientInfo(uid, TransactionData.clientIdx){
                    for(c2 in it.result){
                        TransactionData.clientName = c2["clientName"] as String
                        transactionList.postValue(tempTransactionList)
                    }
                }
            }
        })
    }

    fun getUserAllClient(uid: String){
        tempClientSimpleDataList.clear()
        TransactionRepository.getUserAllClient(uid){
            for(c1 in it.result){
                val newClientData = ClientSimpleData(
                    c1["clientIdx"] as Long,
                    c1["clientName"] as String,
                    c1["clientManagerName"] as String,
                    c1["clientState"] as Long,
                    c1["isBookmark"] as Boolean
                )
                tempClientSimpleDataList.add(newClientData)
                clientSimpleDataListVM.postValue(tempClientSimpleDataList)
            }
        }
    }
}