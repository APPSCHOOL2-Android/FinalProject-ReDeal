package com.hifi.redeal.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
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

        // UI 요소에 대한 리스너 설정
        setupUiListeners()

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

//        // 로그인 버튼 클릭 시 로그인 처리 함수 호출
//        fragmentAuthLoginBinding.buttonAuthLogin.setOnClickListener {
//            handleLoginButtonClick()
//        }

//        // 이메일 입력 텍스트 클릭 시 소프트 키보드 표시
//        fragmentAuthLoginBinding.textInputEditTextLoginUserId.setOnClickListener {
//            authActivity.showSoftInput(it)
//        }
    }


}