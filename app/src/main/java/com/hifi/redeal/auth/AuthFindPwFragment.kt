package com.hifi.redeal.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
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

            // 터치 시 키보드 숨기기 처리
            hideKeyboardOnTouch(fragmentAuthFindPwBinding.root)

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

                    // 성공적으로 이메일을 보냈을 때 스낵바로 메시지 표시
                    val successMessage = "비밀번호 재설정 발송 성공. 이메일을 확인해주세요"
                    showSnackbar(successMessage)

                } else {
                    // 비밀번호 재설정 이메일을 보내는 데 실패한 경우 처리
                    val exception = task.exception
                    if (exception != null) {

                        //  이메일을 보내기 실패한 경우 스낵바로 메시지 표시
                        val failedMessage = "비밀번호 재설정 발송 실패. 아이디를 확인해주세요"
                        showSnackbar(failedMessage)

                    }
                }
            }
    }

    // 스낵바를 표시하는 함수
    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(
            fragmentAuthFindPwBinding.root,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun hideKeyboardOnTouch(view: View) {
        view.setOnTouchListener { _, _ ->
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            false
        }
    }
}


