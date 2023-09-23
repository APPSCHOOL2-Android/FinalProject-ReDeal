package com.hifi.redeal.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.auth.vm.AuthViewModel
import com.hifi.redeal.databinding.FragmentAuthLoginBinding

class AuthLoginFragment : Fragment() {

    lateinit var fragmentAuthLoginBinding: FragmentAuthLoginBinding
    lateinit var mainActivity: MainActivity
    lateinit var authViewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth

    private val RC_SIGN_IN = 123 // 구글 로그인 요청 코드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentAuthLoginBinding = FragmentAuthLoginBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.initContext(requireContext())
        auth = Firebase.auth

        // UI 요소 리스너
        setupUiListeners()

        // 로그인 시 저장된 체크 상태 확인 및 자동 로그인 시도
        checkAutoLoginStateAndAttempt()

        // 콜백 설정
        authViewModel.onLoginSuccess = {
            // 로그인 성공
            mainActivity.uid = Firebase.auth.uid!!
            mainActivity.replaceFragment(MainActivity. ACCOUNT_LIST_FRAGMENT, false, null)
        }

        return fragmentAuthLoginBinding.root
    }

    // UI 요소에 리스너를 설정하는 함수
    private fun setupUiListeners() {
        // 체크박스의 상태에 따라 자동 로그인 설정
        fragmentAuthLoginBinding.checkboxAuthAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            saveAutoLoginState(isChecked)
        }

        fragmentAuthLoginBinding.textViewAuthJoin.setOnClickListener {
            mainActivity.replaceFragment(MainActivity.AUTH_JOIN_FRAGMENT, true, null)
        }

        fragmentAuthLoginBinding.textViewAuthFindPw.setOnClickListener {
            mainActivity.replaceFragment(MainActivity.AUTH_FIND_PW_FRAGMENT, true, null)
        }

        fragmentAuthLoginBinding.buttonAuthLogin.setOnClickListener {
            handleLoginButtonClick()
        }

        fragmentAuthLoginBinding.textInputEditTextLoginUserId.setOnClickListener {
            mainActivity.showSoftInput(it)
        }

        // 구글 로그인 버튼
        fragmentAuthLoginBinding.buttonAuthGoogleLogin.setOnClickListener {
            val signInIntent = authViewModel.getGoogleSignInClient(requireContext()).signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
       }
    }

    // 자동 로그인 상태를 확인하고 시도하는 함수
    private fun checkAutoLoginStateAndAttempt() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val shouldAttemptAutoLogin = sharedPreferences.getBoolean("auto_login", false)

        if (shouldAttemptAutoLogin) {
            // 자동 로그인 시도
            attemptAutoLogin()
        }
    }

    private fun attemptAutoLogin() {
        // SharedPreferences에서 UID 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUid = sharedPreferences.getString("user_uid", null)

        // UID가 저장되어 있다면 자동 로그인 시도
        if (savedUid != null) {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // 사용자가 이미 로그인한 경우
                mainActivity.uid = Firebase.auth.uid!!
                mainActivity.replaceFragment(MainActivity.ACCOUNT_LIST_FRAGMENT, false, null)
            } else {
                // 저장된 UID가 없는 경우
                mainActivity.replaceFragment(MainActivity.AUTH_LOGIN_FRAGMENT, false, null)
            }
        }
    }

    // 자동 로그인 상태 저장
    private fun saveAutoLoginState(isChecked: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("auto_login", isChecked)
        editor.apply()
    }

    // 로그인 버튼 클릭 처리
    private fun handleLoginButtonClick() {
        val email = fragmentAuthLoginBinding.textInputEditTextLoginUserId.text.toString()
        val password = fragmentAuthLoginBinding.textInputEditTextLoginUserPw.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                // 이메일이 비어있다면 이메일 입력란에 포커스 및 키보드 표시
                fragmentAuthLoginBinding.textInputEditTextLoginUserId.requestFocus()
                mainActivity.showSoftInput(fragmentAuthLoginBinding.textInputEditTextLoginUserId)
            } else {
                // 비밀번호가 비어있다면 비밀번호 입력란에 포커스 및 키보드 표시
                fragmentAuthLoginBinding.textInputEditTextLoginUserPw.requestFocus()
                mainActivity.showSoftInput(fragmentAuthLoginBinding.textInputEditTextLoginUserPw)
            }
        } else {
            // 이메일과 비밀번호가 입력되었다면 로그인 처리 함수 호출
            authViewModel.loginUser(email, password, fragmentAuthLoginBinding.root)

            fragmentAuthLoginBinding.textInputEditTextLoginUserId.clearFocus()
            fragmentAuthLoginBinding.textInputEditTextLoginUserPw.clearFocus()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    // Google 로그인 성공, AuthViewModel의 googleSignIn 메서드 호출
                    authViewModel.googleSignIn(account, fragmentAuthLoginBinding.root)
                } else {
                    // Google 로그인 실패

                }
            } catch (e: ApiException) {
                // Google 로그인 실패

            }
        }
    }
}