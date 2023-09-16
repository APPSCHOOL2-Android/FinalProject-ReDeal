package com.hifi.redeal.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.hifi.redeal.model.UserDataClass

class AuthRepository {
    companion object {
        fun loginUser(email: String, password: String, callback1: (AuthResult) -> Unit) {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                Log.d("testloginUserRepo", "flag111")
                if(task.isSuccessful){
                    Log.d("testloginUserRepo", "flag222")
                    val result = task.result
                    callback1(result)
                }else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Log.d("testaaa","Invalid credentials: ${exception.localizedMessage}")
                            }
                            is FirebaseAuthInvalidUserException -> {
                                Log.d("testaaa","Invalid user: ${exception.localizedMessage}")
                            }
                            else -> {
                                Log.d("testaaa","Authentication failed: ${exception.localizedMessage}")
                            }
                        }
                    } else {
                        Log.d("testaaa","Authentication failed: ${exception?.localizedMessage}")
                    }
                }
            }
        }

        fun registerUser(email: String, password: String, callback1: (AuthResult) -> Unit) {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(callback1)
        }

        fun addUserInfo(userClass: UserDataClass, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val userDataRef = database.getReference("UserData")
            userDataRef.push().setValue(userClass).addOnCompleteListener(callback1)
        }
        // 사용자 아이디를 통해 사용자 정보를 가져온다.
        fun getUserInfoByUserId(loginUserEmail:String, callback1: (Task<DataSnapshot>) -> Unit){
            val database = FirebaseDatabase.getInstance()
            val userDataRef = database.getReference("UserData/")

            // userId가 사용자가 입력한 아이디와 같은 데이터를 가져온다.
            userDataRef.child(loginUserEmail).get().addOnCompleteListener(callback1)
        }

    }
}