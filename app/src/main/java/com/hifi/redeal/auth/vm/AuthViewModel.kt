package com.hifi.redeal.auth.vm

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.hifi.redeal.R
import com.hifi.redeal.auth.model.UserDataClass
import com.hifi.redeal.auth.repository.AuthRepository
import com.hifi.redeal.databinding.FragmentAuthJoinBinding

class AuthViewModel : ViewModel() {

    // 초기화
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    fun initContext(context: Context) {
        this.context = context
    }

    // 콜백
    var onLoginSuccess: (() -> Unit)? = null
    var onRegistrationSuccess: (() -> Unit)? = null

    // AuthLoginFragment의 로그인 함수
    fun loginUser(email: String, password: String, view: View) {
        AuthRepository.loginUser(email, password,
            successCallback = { authResult ->
                val userUid = authResult.user?.uid
                if (userUid != null) {
                    // 로그인 성공 시 UID를 SharedPreferences에 저장
                    saveUidToSharedPreferences(authResult.user!!.uid)
                    onLoginSuccess?.invoke() // 콜백 호출
                } else {
                    val errorMessage = "사용자 정보를 가져올 수 없습니다."
                    showErrorSnackbar(view, errorMessage)
                }
            },
            errorCallback = { errorMessage ->
                showErrorSnackbar(view, errorMessage)
            }
        )
    }

    // AuthJoinFragment의 계정 등록 함수
    fun registerUser(email: String, password: String, name: String, view: View): LiveData<AuthResult> {
        val registrationResult = MutableLiveData<AuthResult>()

        val binding = FragmentAuthJoinBinding.bind(view)

        AuthRepository.registerUser(email, password,
            successCallback = { authResult ->
                val user = authResult.user
                if (user != null) {
                    // IDX를 가져오는 로그
                    getNextIdx(
                        successCallback = { idx ->
                            // IDX를 얻은 후 Firestore에 추가
                            addUserToFirestore(user.uid, UserDataClass(idx, email, name), view)
                            onRegistrationSuccess?.invoke()
                        },
                        errorCallback = { errorMessage ->
                                showErrorSnackbar(view, errorMessage)
                        }
                    )

                } else {
                    // 사용자가 null인 경우
                    showErrorSnackbar(view, "사용자가 없습니다")
                }
                registrationResult.value = authResult
            },
            errorCallback = { errorMessage ->
                if (errorMessage == "가입된 이메일 주소") {
                    // 중복된 이메일 주소 처리
                    binding.warningJoinEmailAlready.visibility = View.VISIBLE
                } else {
                    // 회원가입 중복 이메일 제외 처리
                    showErrorSnackbar(view, errorMessage)
                }
            }
        )
        return registrationResult
    }

    // 파이어스토어에 사용자 정보 추가
    private fun addUserToFirestore(uid: String, newUser: UserDataClass, view: View) {
        val userData = hashMapOf(
            "userIdx" to newUser.userIdx,
            "userEmail" to newUser.userEmail,
            "userName" to newUser.userName
        )

        AuthRepository.addUserToFirestore(uid, userData,
            successCallback = { uid ->
                if (uid != null) {
                    // Firestore에 사용자 정보 추가 성공
                    //
                } else {
                    // Firestore에 사용자 정보 추가 실패
                    showErrorSnackbar(view, "사용자 정보 추가 실패")
                }
            },
            errorCallback = { errorMessage ->
                showErrorSnackbar(view, errorMessage)
            }
        )
    }

    // 파이어베이스에서 IDX를 가져와서 인덱스 계산하여 +1
    private fun getNextIdx(successCallback: (Long) -> Unit,  errorCallback: (String) -> Unit) {
        AuthRepository.getNextIdx(successCallback, errorCallback)
    }

    // UID를 SharedPreferences에 저장
    private fun saveUidToSharedPreferences(uid: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_uid", uid)
        editor.apply()
    }

    private fun showErrorSnackbar(view: View, errorMessage: String) {
        Snackbar.make(view, errorMessage, Snackbar.LENGTH_LONG).show()
    }


    // 구글 로그인 함수 (+파이어베이스 처리)
    fun googleSignIn(account: GoogleSignInAccount, view: View) {
        AuthRepository.googleSignIn(account,
            successCallback = { authResult ->
                // Google 로그인 성공
                val firebaseUser = authResult.user
                val userEmail = firebaseUser?.email
                val userName = firebaseUser?.displayName

                // IDX를 가져오는 로그
                getNextIdx(
                    successCallback = { idx ->
                        addUserToFirestore(firebaseUser!!.uid, UserDataClass(idx,
                            userEmail.toString(), userName.toString()
                        ), view)
                        onRegistrationSuccess?.invoke()
                    },
                    errorCallback = { errorMessage ->
                        showErrorSnackbar(view, errorMessage)
                    }
                )

                onLoginSuccess?.invoke()
            },
            errorCallback = { errorMessage ->
                // Google 로그인 실패
                showErrorSnackbar(view, errorMessage)
            }
        )
    }

     //구글 로그인 클라이언트 초기화
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

}