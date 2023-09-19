package com.hifi.redeal.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentAuthFindPwBinding

class AuthFindPwFragment : Fragment() {

    private lateinit var fragmentAuthFindPwBinding: FragmentAuthFindPwBinding
    lateinit var mainActivity: MainActivity

    // FirebaseAuth 객체 선언
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAuthFindPwBinding = FragmentAuthFindPwBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        fragmentAuthFindPwBinding.run {
            toolbarAuthFindPw.setNavigationOnClickListener {
                mainActivity.removeFragment(MainActivity.AUTH_FIND_PW_FRAGMENT)
            }

            buttonFindPwComplete.setOnClickListener {
                resetPassword()
            }

            return fragmentAuthFindPwBinding.root
        }
    }

    //  이메일로 인증코드를 보내는 함수
    private fun resetPassword() {
        val email = fragmentAuthFindPwBinding.textInputEditTextFindPwUserId.text.toString()

        if (email.isEmpty()) {
            // 이메일 주소를 입력하지 않은 경우 처리

            return
        }

        // Firebase Authentication을 사용하여 비밀번호 재설정 이메일을 보냅니다.
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 비밀번호 재설정 이메일을 성공적으로 보냈을 때의 처리
                    Log.e("AuthFindPwFragment", "비밀번호 재설정 이메일 전송 완료")
                } else {
                    // 비밀번호 재설정 이메일을 보내는 데 실패한 경우 처리
                    val exception = task.exception
                    if (exception != null) {
                        // 오류 처리를 추가할 수 있습니다.
                        Log.e("AuthFindPwFragment", "비밀번호 재설정 이메일 보내기 실패: ${exception.message}")
                    }
                }
            }
    }
}


