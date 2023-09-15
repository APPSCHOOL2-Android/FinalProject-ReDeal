package com.hifi.redeal.map.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.map.model.ClientDataClass

class ClientRepository {
    companion object{
        fun getClientListByUser(userIdx: String, keyword : String,callback1: (Task<QuerySnapshot>) -> Unit) {
            val database = Firebase.firestore

            val clientDataRef = database.collection("userData").document(userIdx).collection("clientData")

            clientDataRef.get().addOnCompleteListener(callback1)

        }

    }
}


