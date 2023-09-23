package com.hifi.redeal.account.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.account.repository.model.NotificationData
import com.hifi.redeal.account.repository.model.UserData

class NotificationRepository {

    val db = Firebase.firestore

    fun getNotificationList(userId: String, callback: (List<NotificationData>) -> Unit) {
        db.collection("userData").document(userId).collection("notificationData")
            .whereEqualTo("isChecked", false)
            .get()
            .addOnSuccessListener { docs ->
                val notificationList = docs.map { doc ->
                    doc.toObject<NotificationData>().apply {
                        notificationId = doc.id
                    }
                }

                callback(notificationList)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun getNotificationSender(senderId: String, callback: (UserData?) -> Unit) {
        db.collection("userData").document(senderId)
            .get()
            .addOnSuccessListener {
                val userData = it.toObject<UserData>()

                callback(userData)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun getNotificationClient(senderId: String, clientIdx: Long, callback: (ClientData?) -> Unit) {
        db.collection("userData").document(senderId).collection("clientData").document("$clientIdx")
            .get()
            .addOnSuccessListener {
                val clientData = it.toObject<ClientData>()

                callback(clientData)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun setChecked(userId: String, notificationId: String) {
        db.collection("userData").document(userId).collection("notificationData").document(notificationId)
            .update("isChecked", true)
    }
}