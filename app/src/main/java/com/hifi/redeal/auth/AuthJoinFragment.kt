package com.hifi.redeal.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.auth.vm.AuthViewModel
import com.hifi.redeal.databinding.FragmentAuthJoinBinding

class AuthJoinFragment : Fragment() {

    lateinit var fragmentAuthJoinBinding: FragmentAuthJoinBinding
    lateinit var mainActivity: MainActivity
    lateinit var authViewModel: AuthViewModel

    private val INVALID_NICKNAME_CHARACTERS = listOf(
        "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "[", "]", "{", "}",
        "|", "\\", ":", ";", "\"", "'", "<", ">", ",", ".", "/", "?"
    )
    private val PASSWORD_POLICY =
        "^(?=.*[0-9])(?=.*[!@#\$%^&*()\\-_+\\=\\[\\]{}|\\\\:;\"'<>,./?])(?=.*[a-z]).{8,}$".toRegex()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAuthJoinBinding = FragmentAuthJoinBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        authViewModel.initContext(requireContext()) // Context 초기화

        fragmentAuthJoinBinding.run {
            toolbarAuthJoin.setNavigationOnClickListener {
                mainActivity.removeFragment(MainActivity.AUTH_JOIN_FRAGMENT)
            }

            buttonJoinComplete.setOnClickListener {
                // 버튼 비활성화
                it.isEnabled = false

                val email = textInputEditTextJoinUserId.text.toString()
                val password = textInputEditTextJoinUserPw.text.toString()
                val name = textInputEditTextJoinUserName.text.toString()
                val passwordCheck = textInputEditTextJoinUserPwCheck.text.toString()

                // 경고 메시지 초기화
                warningJoinEmailFormat.visibility = View.GONE
                warningJoinPassword.visibility = View.GONE
                warningJoinPasswordCheck.visibility = View.GONE
                warningJoinNameFormat.visibility = View.GONE
                warningJoinEmailAlready.visibility = View.GONE
                warningJoinPasswordContinuity.visibility = View.GONE

                // 예외처리
                val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                val isPasswordValid = password.length >= 8 && PASSWORD_POLICY.matches(password)
                val isPasswordMatch = password == passwordCheck
                val isNameValid =
                    name.length in 1..8 && !INVALID_NICKNAME_CHARACTERS.any { name.contains(it) }
                val isContinuitycheck = containsConsecutiveOrSequentialNumbers(password)

                // 유효성 검사
                if (!isEmailValid) {
                    warningJoinEmailFormat.visibility = View.VISIBLE
                    it.isEnabled = true
                    return@setOnClickListener
                } else {
                    warningJoinEmailFormat.visibility = View.GONE
                }

                if (!isPasswordValid) {
                    warningJoinPassword.visibility = View.VISIBLE
                    it.isEnabled = true
                    return@setOnClickListener
                } else {
                    warningJoinPassword.visibility = View.GONE
                }

                if (!isPasswordMatch) {
                    warningJoinPasswordCheck.visibility = View.VISIBLE
                    it.isEnabled = true
                    return@setOnClickListener
                } else {
                    warningJoinPasswordCheck.visibility = View.GONE
                }

                if (!isNameValid) {
                    warningJoinNameFormat.visibility = View.VISIBLE
                    it.isEnabled = true
                    return@setOnClickListener
                } else {
                    warningJoinNameFormat.visibility = View.GONE
                }

                if (isContinuitycheck) {
                    warningJoinPasswordContinuity.visibility = View.VISIBLE
                    it.isEnabled = true
                    return@setOnClickListener
                } else {
                    warningJoinPasswordContinuity.visibility = View.GONE
                }

                // Firebase Authentication을 사용하여 사용자 등록
                val registrationLiveData = authViewModel.registerUser(email, password, name, fragmentAuthJoinBinding.root)

                registrationLiveData.observe(viewLifecycleOwner, Observer { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        // 등록 성공
                        showRegistrationSuccessDialog()
                        mainActivity.replaceFragment(MainActivity.AUTH_LOGIN_FRAGMENT, true, null)
                    } else {
                        showErrorMessageDialog("가입 실패")
                    }

                    // 버튼 다시 활성화
                    it.isEnabled = true
                })
            }
        }

        hideKeyboardOnTouch(fragmentAuthJoinBinding.root)
        return fragmentAuthJoinBinding.root
    }

    // 가입 성공 다이얼로그
    private fun showRegistrationSuccessDialog() {
        val view =
            requireActivity().layoutInflater.inflate(R.layout.dialog_join_success_message, null)
        val alertDialogBuilder = AlertDialog.Builder(requireContext()).setView(view)
        val alertDialog = alertDialogBuilder.create()
        val buttonDialogDismiss = view.findViewById<Button>(R.id.buttonJoinSuccess)

        buttonDialogDismiss.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    // 오류 처리 다이얼로그
    private fun showErrorMessageDialog(message: String) {
        val alertDialogBuilder =
            AlertDialog.Builder(requireContext()).setTitle("오류").setMessage(message)
                .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    // 연속된 문자나 숫자 검사 함수 추가
    private fun containsConsecutiveOrSequentialNumbers(password: String): Boolean {
        var sameNumberCount = 1 // 동일한 숫자의 개수를 추적

        for (i in 0 until password.length - 1) {
            if (password[i] == password[i + 1]) {
                sameNumberCount++
                if (sameNumberCount >= 2) {
                    return true // 2개 이상의 동일한 숫자 패턴을 검사
                }
            } else {
                sameNumberCount = 1 // 동일한 숫자가 아니라면 초기화
            }
        }
        return false
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
