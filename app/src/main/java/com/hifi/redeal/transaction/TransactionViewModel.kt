package com.hifi.redeal.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.transaction.model.TransactionData
import com.hifi.redeal.transaction.model.customTransactionData

class TransactionViewModel: ViewModel() {

    val uid = Firebase.auth.uid!!

    var transactionList = MutableLiveData<MutableList<customTransactionData>>()
    var tempTransactionList = mutableListOf<customTransactionData>()

    var nextTransactionIdx = 0L
    fun getNextTransactionIdx(){
        TransactionRepository.getNextTransactionIdx(uid){
            for(c1 in it.result){
                nextTransactionIdx = c1["transactionIdx"] as Long + 1L
            }
        }
    }
    fun getAllTransactionData(){
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

}