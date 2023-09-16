package com.hifi.redeal.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hifi.redeal.model.UserDataClass
import com.hifi.redeal.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userData = MutableLiveData<UserDataClass>()

    private val INVALID_NICKNAME_CHARACTERS = listOf(
        "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "[", "]", "{", "}",
        "|", "\\", ":", ";", "\"", "'", "<", ">", ",", ".", "/", "?"
    )

    // AuthLoginFragment의 로그인 함수
    fun loginUser(email: String, password: String) {
        AuthRepository.loginUser(email, password) {
            val userUid = it.user?.email
            Log.d("testloginUserVM", "User UID: $userUid")
            if (userUid != null) {
                AuthRepository.getUserInfoByUserId(userUid) { currentUser ->
                    val userIdx = currentUser.result.child("idx").value as Long
                    val userEmail = currentUser.result.child("idx").value as String
                    val userPassword = currentUser.result.child("idx").value as String
                    val userName = currentUser.result.child("idx").value as String
                    val loginUser = UserDataClass(
                        userIdx, email, password, userName
                    )
                    userData.value = loginUser
                }
            }
        }
    }

    // AuthJoinFragment의 계정 등록 함수
    fun registerUser(email: String, password: String, name: String) {
        AuthRepository.registerUser(email, password) { authResult ->
            val user = authResult.user
            if (user != null) {
                getNextIdx { userIdx ->
                    val newUser = UserDataClass(userIdx, email, password, name)
                    AuthRepository.addUserInfo(newUser) {
                        Log.d("testloginUserVM", "로그인 성공")
                    }
                    userData.value = newUser
                }
            } else {
                // 사용자가 null인 경우 처리
                Log.d("testloginUserVM", "사용자가 null입니다.")
            }
        }
    }

    // 파이어베이스에서 IDX를 가져와서 인덱스 계산
    private fun getNextIdx(callback: (Long) -> Unit) {
        val idxRef = firestore.collection("idxCounter").document("userIdx")

        firestore.runTransaction { transaction ->
            // 문서를 가져옵니다.
            val docSnapshot = transaction.get(idxRef)

            // "value" 필드의 현재 값 또는 기본 값(0)을 가져옵니다.
            val currentIdx = docSnapshot.getLong("value") ?: 0

            // 다음 인덱스 계산
            val nextIdx = currentIdx + 1

            // 다음 인덱스를 Firestore에 업데이트합니다.
            transaction.update(idxRef, "value", nextIdx)

            // 결과를 반환합니다.
            nextIdx
        }.addOnSuccessListener { nextIdx ->
            // 성공적으로 다음 인덱스를 얻었을 때 콜백을 호출합니다.
            callback(nextIdx)
        }.addOnFailureListener { e ->
            // 실패 시 처리
            Log.e("getNextIdx", "Error getting next index: $e")
        }
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun isNicknameValid(nickname: String): Boolean {
        return nickname.length in 2..12 && !INVALID_NICKNAME_CHARACTERS.any { nickname.contains(it) }
    }

}