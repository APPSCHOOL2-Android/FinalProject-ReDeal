package com.hifi.redeal.auth.vm

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hifi.redeal.auth.model.UserDataClass
import com.hifi.redeal.auth.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // Context 초기화
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    // SharedPreferences 초기화
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    fun initContext(context: Context) {
        this.context = context
    }

    // 콜백 정의: 로그인 성공 시 호출됨
    var onLoginSuccess: (() -> Unit)? = null
    var onRegistrationSuccess: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    // AuthLoginFragment의 로그인 함수
    fun loginUser(email: String, password: String, view: View) {
        AuthRepository.loginUser(email, password,
            successCallback = { authResult ->
                val userUid = authResult.user?.uid
                if (userUid != null) {
                    // 로그인 성공 시 UID를 SharedPreferences에 저장
                    saveUidToSharedPreferences(authResult.user!!.uid)
                    Log.d("testloginUserVM", "로그인 성공.")
                    // 로그인 성공 시 처리
                    onLoginSuccess?.invoke() // 콜백 호출
                } else {
                    // 사용자 정보가 없는 경우 또는 가져오기 실패한 경우 처리
                    val errorMessage = "사용자 정보를 가져올 수 없습니다."
                    showErrorSnackbar(view, errorMessage)
                }
            },
            errorCallback = { errorMessage ->
                // 스낵바를 사용하여 오류 메시지 표시
                showErrorSnackbar(view, errorMessage)
            }
        )
    }

    // AuthJoinFragment의 계정 등록 함수
    fun registerUser(email: String, password: String, name: String, view: View): LiveData<AuthResult> {
        val registrationResult = MutableLiveData<AuthResult>()

        AuthRepository.registerUser(email, password,
            successCallback = { authResult ->
                val user = authResult.user
                if (user != null) {
                    // 사용자가 성공적으로 등록된 경우
                    Log.d("testloginUserVM", "사용자가 성공적으로 등록되었습니다.")

                    // IDX를 가져오는 로그
                    getNextIdx(
                        successCallback = { idx ->
                            Log.d("getNextIdx", "현재 IDX: $idx")
                            // IDX를 얻은 후 Firestore에 추가
                            addUserToFirestore(user.uid, UserDataClass(idx, email, name), view)
                            onRegistrationSuccess?.invoke()
                        },
                        errorCallback = { errorMessage ->
                            // 에러 발생 시 처리
                            showErrorSnackbar(view, errorMessage)
                        }
                    )

                } else {
                    // 사용자가 null인 경우 처리
                    Log.d("testloginUserVM", "사용자가 null입니다.")
                }
                // 사용자 등록 결과를 LiveData에 넣어줍니다.
                registrationResult.value = authResult
            },
            errorCallback = { errorMessage ->
                onError?.invoke(errorMessage)
            }
        )
        // LiveData를 반환합니다.
        return registrationResult
    }

    // 파이어스토어에 사용자 정보 추가
    private fun addUserToFirestore(uid: String, newUser: UserDataClass, view: View) {
        val userData = hashMapOf(
            "userIdx" to newUser.userIdx,
            "userEmail" to newUser.userEmail,
            "userName" to newUser.userName
        )

        // 리포지토리의 addUserToFirestore 함수
        AuthRepository.addUserToFirestore(uid, userData,
            successCallback = { uid ->
                if (uid != null) {
                    // Firestore에 사용자 정보 추가 성공
                    Log.d("FirestoreSuccess", "Firestore에 사용자 정보 추가 성공")
                } else {
                    // Firestore에 사용자 정보 추가 실패
                    Log.e("FirestoreError", "Firestore에 사용자 정보 추가 실패")
                    showErrorSnackbar(view, "Firestore에 사용자 정보 추가 실패")
                }
            },
            errorCallback = { errorMessage ->
                // 스낵바를 사용하여 오류 메시지 표시
                showErrorSnackbar(view, errorMessage)
            }
        )
    }

    // 파이어베이스에서 IDX를 가져와서 인덱스 계산하여 +1
    private fun getNextIdx(successCallback: (Long) -> Unit,  errorCallback: (String) -> Unit) {
        // 리포지토리의 getNextIdx 함수 사용
        AuthRepository.getNextIdx(successCallback, errorCallback)
    }

    // UID를 SharedPreferences에 저장
    private fun saveUidToSharedPreferences(uid: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_uid", uid)
        editor.apply()
        // 저장 후에 로그로 확인
        Log.d("saveUidToSharedPreferences", "UID 저장 완료: $uid")
    }

    // 스낵바를 사용하여 오류 메시지 표시
    private fun showErrorSnackbar(view: View, errorMessage: String) {
        Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
    }

}