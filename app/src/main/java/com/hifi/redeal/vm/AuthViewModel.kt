package com.hifi.redeal.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.hifi.redeal.model.UserDataClass
import com.hifi.redeal.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    val userData = MutableLiveData<UserDataClass>()

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
                    if (currentUser != null) {
                        val loginUser = currentUser
                        userData.value = loginUser
                    } else {
                        // 사용자 정보가 없는 경우 또는 가져오기 실패한 경우 처리
                        Log.e("testloginUserVM", "사용자 정보를 가져올 수 없습니다.")
                    }
                }
            }
        }
    }

    // AuthJoinFragment의 계정 등록 함수
    fun registerUser(email: String, password: String, name: String) {
        AuthRepository.registerUser(email, password) { authResult ->
            val user = authResult.user
            if (user != null) {
                // 사용자가 성공적으로 등록된 경우
                Log.d("testloginUserVM", "사용자가 성공적으로 등록되었습니다.")
                addUserToFirestore(user.uid, UserDataClass(0, email, password, name))
           } else {
                // 사용자가 null인 경우 처리
                Log.d("testloginUserVM", "사용자가 null입니다.")
            }
        }
    }

    // 파이어스토어에 사용자 정보 추가
    private fun addUserToFirestore(uid: String, newUser: UserDataClass) {
        val userData = hashMapOf(
            "userIdx" to newUser.userIdx,
            "email" to newUser.userEmail,
            "password" to newUser.userPw,
            "name" to newUser.userName
        )

        firestore.collection("userData").document(uid).set(userData) // UID를 문서 ID로 사용합니다.
            .addOnSuccessListener { documentReference ->
                // Firestore에 사용자 정보 추가 성공
                Log.d("FirestoreSuccess", "Firestore에 사용자 정보 추가 성공")
            }
            .addOnFailureListener { e ->
                // Firestore에 사용자 정보 추가 실패
                Log.e("FirestoreError", "Firestore에 사용자 정보 추가 실패: ${e.message}")
                showErrorMessageDialog("Firestore에 사용자 정보 추가 실패: ${e.message}")
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

    private fun showErrorMessageDialog(message: String) {
        // 오류 처리 다이얼로그를 보여주는 함수 구현
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun isNicknameValid(nickname: String): Boolean {
        return nickname.length in 2..12 && !INVALID_NICKNAME_CHARACTERS.any { nickname.contains(it) }
    }

}