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
                // 키보드 설정
                val imm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            }

            hideKeyboardOnTouch(fragmentAuthFindPwBinding.root)

            return fragmentAuthFindPwBinding.root
        }
    }

    //  이메일로 인증코드를 보내는 함수
    private fun resetPassword() {
        val email = fragmentAuthFindPwBinding.textInputEditTextFindPwUserId.text.toString()

        if (email.isEmpty()) {
            // 이메일 주소를 입력하지 않은 경우 처리
            showSnackbar("이메일 주소를 입력해주세요")
            return
        }

        // Firebase Authentication을 사용하여 비밀번호 재설정 이메일 발송
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSnackbar("비밀번호 재설정 발송 성공. 이메일을 확인해주세요")

                } else {
                    val exception = task.exception
                    if (exception != null) {

                        showSnackbar("비밀번호 재설정 발송 실패. 아이디를 확인해주세요")
                    }
                }
            }
    }

    // 스낵바
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
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            false
        }
    }
}


