package com.hifi.redeal.auth.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.hifi.redeal.auth.model.UserDataClass

class AuthRepository {
    companion object {
        private val auth = FirebaseAuth.getInstance()
        private val firestore = FirebaseFirestore.getInstance()
        fun loginUser(email: String, password: String, callback1: (AuthResult) -> Unit) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("testloginUserRepo", "리포지트리 loginUser 성공")
                    val result = task.result
                    callback1(result)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Log.d("testRefologinUser", "Invalid credentials: ${exception.localizedMessage}"
                                )
                            }

                            is FirebaseAuthInvalidUserException -> {
                                Log.d("testRefologinUser", "Invalid user: ${exception.localizedMessage}")
                            }
                            else -> {
                                Log.d(
                                    "testRefologinUser",
                                    "Authentication failed: ${exception.localizedMessage}"
                                )
                            }
                        }
                    } else {
                        Log.d("testRefologinUser", "Authentication failed: ${exception?.localizedMessage}")
                    }
                }
            }
        }

        // 사용자 정보를 Firestore에 추가하고 사용자 UID를 콜백으로 처리
        fun addUserToFirestore(uid: String, userData: Map<String, Any>, callback: (String?) -> Unit) {
            firestore.collection("userData").document(uid).set(userData)
                .addOnSuccessListener {
                    // Firestore에 사용자 정보 추가 성공 시 사용자 UID를 반환
                    callback(uid)
                }
                .addOnFailureListener { e ->
                    // Firestore에 사용자 정보 추가 실패
                    Log.e("FirestoreError", "Firestore에 사용자 정보 추가 실패: ${e.message}")
                    callback(null)
                }
        }

        fun registerUser(email: String, password: String, callback1: (AuthResult) -> Unit) {
            // 이미 가입된 이메일인지 확인
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                            val errorMessage = "이미 가입된 이메일 주소입니다. 다른 이메일 주소를 사용하세요."
                            Log.d("registerUser", errorMessage) // 또는 다른 방법으로 사용자에게 알릴 수 있음
                        } else {
                            // 이미 가입되지 않은 이메일 주소인 경우, Firebase Authentication을 통해 사용자 등록
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(callback1)
                        }
                    } else {
                        // 이메일 확인 작업에 실패한 경우 처리
                        Log.e("registerUser", "Failed to check email: ${task.exception?.message}")
                    }
                }
        }
    }
}

