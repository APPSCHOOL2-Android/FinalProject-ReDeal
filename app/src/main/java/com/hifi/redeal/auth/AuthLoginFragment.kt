package com.hifi.redeal.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.auth.vm.AuthViewModel
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAuthLoginBinding

class AuthLoginFragment : Fragment() {

    lateinit var fragmentAuthLoginBinding: FragmentAuthLoginBinding
    lateinit var mainActivity: MainActivity
    lateinit var authViewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentAuthLoginBinding = FragmentAuthLoginBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.initContext(requireContext()) // Context 초기화
        auth = Firebase.auth

        // UI 요소에 대한 리스너 설정
        setupUiListeners()

        // 로그인 시 저장된 체크 상태 확인 및 자동 로그인 시도
        checkAutoLoginStateAndAttempt()

        // 로그인 성공 시 처리를 담당하는 콜백 설정
        authViewModel.onLoginSuccess = {
            // 로그인 성공 시 처리 코드를 여기에 추가
            // MainActivity의 replaceFragment 메서드를 호출하여 화면 이동
            mainActivity.uid = Firebase.auth.uid!!
            mainActivity.replaceFragment(MainActivity. ACCOUNT_LIST_FRAGMENT, false, null)
        }

        return fragmentAuthLoginBinding.root
    }

    // UI 요소에 리스너를 설정하는 함수
    private fun setupUiListeners() {
        // 체크박스의 상태에 따라 자동 로그인 설정
        fragmentAuthLoginBinding.checkboxAuthAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            // 사용자가 체크박스 상태를 변경할 때마다 해당 상태를 저장
            saveAutoLoginState(isChecked)
        }

        // 회원가입 텍스트 클릭 시 JoinFragment로 교체
        fragmentAuthLoginBinding.textViewAuthJoin.setOnClickListener {
            mainActivity.replaceFragment(MainActivity.AUTH_JOIN_FRAGMENT, true, null)
        }

        // 비밀번호 찾기 텍스트 클릭 시 FindPwFragment로 교체
        fragmentAuthLoginBinding.textViewAuthFindPw.setOnClickListener {
            mainActivity.replaceFragment(MainActivity.AUTH_FIND_PW_FRAGMENT, true, null)
        }

        // 로그인 버튼 클릭 시 로그인 처리 함수 호출
        fragmentAuthLoginBinding.buttonAuthLogin.setOnClickListener {
            handleLoginButtonClick()
        }

        // 이메일 입력 텍스트 클릭 시 소프트 키보드 표시
        fragmentAuthLoginBinding.textInputEditTextLoginUserId.setOnClickListener {
            mainActivity.showSoftInput(it)
        }
    }


    // 자동 로그인 상태를 확인하고 시도하는 함수
    private fun checkAutoLoginStateAndAttempt() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val shouldAttemptAutoLogin = sharedPreferences.getBoolean("auto_login", false)

        if (shouldAttemptAutoLogin) {
            // 자동 로그인을 시도
            Log.d("authlogin", "자동 로그인 상태: 활성화")
            attemptAutoLogin()
        }
    }

    private fun attemptAutoLogin() {
        // SharedPreferences에서 UID 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUid = sharedPreferences.getString("user_uid", null)
        val currentUser = auth.currentUser

        // UID가 저장되어 있다면 자동 로그인 시도
        if (savedUid != null) {
            Log.d("authlogin", "저장된 UID: $savedUid")

            if (currentUser != null) {
                Log.d("authlogin", "사용자 로그인 상태: ${currentUser.email}")

                // 사용자가 이미 로그인한 경우, 자동 로그인
                // 화면 이동
                mainActivity.uid = Firebase.auth.uid!!
                mainActivity.replaceFragment(MainActivity.ACCOUNT_LIST_FRAGMENT, false, null)
            } else {
                // 저장된 UID가 없는 경우 로그인 화면을 표시
                Log.e("authlogin", "사용자 로그인 상태 없음")
            }
        }
    }


    // 자동 로그인 상태를 저장하는 함수
    private fun saveAutoLoginState(isChecked: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("auto_login", isChecked)
        editor.apply()
    }


    // 로그인 버튼 클릭 처리 함수
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
            authViewModel.loginUser(email, password)
            // 포커스와 키보드 클리어
            fragmentAuthLoginBinding.textInputEditTextLoginUserId.clearFocus()
            fragmentAuthLoginBinding.textInputEditTextLoginUserPw.clearFocus()
        }
    }
}