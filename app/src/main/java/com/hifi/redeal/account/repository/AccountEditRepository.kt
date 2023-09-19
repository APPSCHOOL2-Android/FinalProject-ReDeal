package com.hifi.redeal.account.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.account.repository.model.ClientInputData

class AccountEditRepository {
    val db = Firebase.firestore

    fun registerClient(userId: Long, clientInputData: ClientInputData, callback: () -> Unit) {
        val ref = db.collection("userData").document("$userId").collection("clientData")
        ref.document("${clientInputData.clientIdx}").set(clientInputData)
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun getClient(userId: Long, clientIdx: Long, callback: (ClientData) -> Unit) {
        db.collection("userData").document("$userId").collection("clientData").document("$clientIdx")
            .get()
            .addOnSuccessListener {
                val client = it.toObject<ClientData>()
                if (client != null)
                    callback(client)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun updateClient(userId: Long, clientInputData: ClientInputData, callback: () -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "clientState" to clientInputData.clientState,
            "clientName" to clientInputData.clientName,
            "clientAddress" to clientInputData.clientAddress,
            "clientDetailAdd" to clientInputData.clientDetailAdd,
            "clientCeoPhone" to clientInputData.clientCeoPhone,
            "clientFaxNumber" to clientInputData.clientFaxNumber,
            "clientManagerName" to clientInputData.clientManagerName,
            "clientManagerPhone" to clientInputData.clientManagerPhone,
            "clientExplain" to clientInputData.clientExplain,
            "clientMemo" to clientInputData.clientMemo
        )

        db.collection("userData").document("$userId").collection("clientData").document("${clientInputData.clientIdx}")
            .update(updates)
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}