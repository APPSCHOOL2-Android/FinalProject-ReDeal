package com.hifi.redeal.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentAuthLoginBinding
import com.hifi.redeal.vm.AuthViewModel

class AuthLoginFragment : Fragment() {

    lateinit var fragmentAuthLoginBinding: FragmentAuthLoginBinding
    lateinit var mainActivity: MainActivity
    lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentAuthLoginBinding = FragmentAuthLoginBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.initContext(requireContext()) // Context 초기화

        // UI 요소에 대한 리스너 설정
        setupUiListeners()

        // 저장된 UID를 사용한 자동 로그인 시도
        attemptAutoLogin()

        return fragmentAuthLoginBinding.root
    }

    // UI 요소에 리스너를 설정하는 함수
    private fun setupUiListeners() {
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

        // 체크박스의 상태에 따라 자동 로그인 설정
        fragmentAuthLoginBinding.checkboxAuthAutoLogin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 체크박스가 선택된 경우에만 자동 로그인 활성화
                enableAutoLogin()
            } else {
                // 체크박스가 선택되지 않은 경우에는 자동 로그인 비활성화
                disableAutoLogin()
            }
        }
    }

    private fun attemptAutoLogin() {
        // SharedPreferences에서 UID 가져오기
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUid = sharedPreferences.getString("user_uid", null)

        // UID가 저장되어 있다면 자동 로그인 시도
        if (savedUid != null) {
            FirebaseAuth.getInstance().signInWithCustomToken(savedUid)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공
                        val user = task.result?.user
                        // TODO: 사용자를 앱 내부에 로그인 상태로 유지
                    } else {
                        // 로그인 실패
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidUserException) {
                            // 사용자가 더 이상 존재하지 않음
                        } else {
                            // 기타 로그인 실패 상황 처리
                        }
                    }
                }
        } else {
            // 저장된 UID가 없는 경우 일반 로그인 화면을 표시하거나 다른 처리를 수행
        }
    }

    private fun enableAutoLogin() {
        // TODO: 자동 로그인 활성화 코드 추가
    }

    private fun disableAutoLogin() {
        // TODO: 자동 로그인 비활성화 코드 추가
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