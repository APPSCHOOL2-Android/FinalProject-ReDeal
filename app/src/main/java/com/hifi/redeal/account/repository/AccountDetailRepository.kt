package com.hifi.redeal.account.repository

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.account.model.ClientData
import com.hifi.redeal.account.model.ContactData
import com.hifi.redeal.account.model.ScheduleData

class AccountDetailRepository {
    val db = Firebase.firestore

    fun getClient(userId: Long, clientIdx: Long, callback: (ClientData?) -> Unit) {
        val userRef = db.collection("userData").document("$userId").collection("clientData").document("$clientIdx")
            .get()
            .addOnSuccessListener {
                val client = it.toObject<ClientData>()

                var contactFetch = false
                var scheduleFetch = false

                db.collection("userData").document("$userId").collection("contactData")
                    .whereEqualTo("clientIdx", client?.clientIdx)
                    .orderBy("contactDate", Query.Direction.DESCENDING).limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client?.recentContactDate = doc.toObject<ContactData>().contactDate
                        }
                        contactFetch = true

                        if (contactFetch && scheduleFetch) {
                            callback(client)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }

                db.collection("userData").document("$userId").collection("scheduleData")
                    .whereEqualTo("clientIdx", client?.clientIdx)
                    .whereEqualTo("isVisitSchedule", true)
                    .whereEqualTo("isScheduleFinish", true)
                    .orderBy("scheduleFinishTime", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            client?.recentVisitDate = doc.toObject<ScheduleData>().scheduleFinishTime
                        }
                        scheduleFetch = true

                        if (contactFetch && scheduleFetch) {
                            callback(client)
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun updateBookmark(userId: Long, clientIdx: Long, isBookmark: Boolean) {
        db.collection("userData").document("$userId").collection("clientData").document("$clientIdx")
            .update("isBookmark", isBookmark)
    }

    fun deleteClient(userId: Long, clientIdx: Long, callback: () -> Unit) {
        db.collection("userData").document("$userId").collection("clientData").document("$clientIdx")
            .delete()
            .addOnSuccessListener {
                callback()
            }
    }
}