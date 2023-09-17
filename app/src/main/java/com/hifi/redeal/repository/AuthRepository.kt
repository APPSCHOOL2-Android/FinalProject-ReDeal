package com.hifi.redeal.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.hifi.redeal.model.UserDataClass

class AuthRepository {
    companion object {
        private val auth = FirebaseAuth.getInstance()
        private val firestore = FirebaseFirestore.getInstance()
        fun loginUser(email: String, password: String, callback1: (AuthResult) -> Unit) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                Log.d("testloginUserRepo", "flag111")
                if (task.isSuccessful) {
                    Log.d("testloginUserRepo", "flag222")
                    val result = task.result
                    callback1(result)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                Log.d(
                                    "testaaa",
                                    "Invalid credentials: ${exception.localizedMessage}"
                                )
                            }

                            is FirebaseAuthInvalidUserException -> {
                                Log.d("testaaa", "Invalid user: ${exception.localizedMessage}")
                            }

                            else -> {
                                Log.d(
                                    "testaaa",
                                    "Authentication failed: ${exception.localizedMessage}"
                                )
                            }
                        }
                    } else {
                        Log.d("testaaa", "Authentication failed: ${exception?.localizedMessage}")
                    }
                }
            }
        }

        // 사용자 정보를 Firestore에 추가하고 사용자 UID를 콜백으로 처리
        fun addUserToFirestore(user: FirebaseUser?, userData: Map<String, Any>, callback: (String?) -> Unit) {
            if (user != null) {
                firestore.collection("userData").add(userData)
                    .addOnSuccessListener { documentReference ->
                        // Firestore에 사용자 정보 추가 성공 시 사용자 UID를 반환
                        callback(user.uid)
                    }
                    .addOnFailureListener { e ->
                        // Firestore에 사용자 정보 추가 실패
                        Log.e("FirestoreError", "Firestore에 사용자 정보 추가 실패: ${e.message}")
                        callback(null)
                    }
            } else {
                // 사용자가 null인 경우 처리
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



        // 사용자 아이디를 통해 사용자 정보를 가져온다.
        fun getUserInfoByUserId(loginUserEmail: String, callback1: (UserDataClass?) -> Unit) {
            // Firestore에서 사용자 정보를 가져옵니다. (예: 이메일로 검색)
            firestore.collection("userData")
                .whereEqualTo("userEmail", loginUserEmail)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val querySnapshot = task.result
                        if (!querySnapshot.isEmpty) {
                            // 검색 결과가 비어있지 않으면 첫 번째 문서의 데이터를 가져와서 UserDataClass로 변환합니다.
                            val documentSnapshot = querySnapshot.documents[0]
                            val userIdx = documentSnapshot.getLong("userIdx") ?: 0
                            val userName = documentSnapshot.getString("userName") ?: ""
                            val userData = UserDataClass(userIdx, loginUserEmail, "", userName)
                            callback1(userData)
                        } else {
                            // 검색 결과가 비어있으면 null을 반환합니다.
                            callback1(null)
                        }
                    } else {
                        // 실패 처리
                        Log.e(
                            "FirestoreError",
                            "Firestore에서 사용자 정보 가져오기 실패: ${task.exception?.message}"
                        )
                        callback1(null)
                    }
                }
        }
    }
}

