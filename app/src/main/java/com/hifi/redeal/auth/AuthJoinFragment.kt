package com.hifi.redeal.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAuthJoinBinding
import com.hifi.redeal.vm.AuthViewModel

class AuthJoinFragment : Fragment() {

    private lateinit var fragmentAuthJoinBinding: FragmentAuthJoinBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var authViewModel: AuthViewModel
    // FirebaseAuth 객체 선언
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAuthJoinBinding = FragmentAuthJoinBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel.initContext(requireContext()) // Context 초기화, Preference에 UID 넣기 위함

        fragmentAuthJoinBinding.run {
            toolbarAuthJoin.setNavigationOnClickListener {
                mainActivity.removeFragment(MainActivity.AUTH_JOIN_FRAGMENT)
            }

//            buttonJoinIdCheck.setOnClickListener{
//                sendVerificationCode()
//            }

            buttonJoinComplete.setOnClickListener {
                val email = textInputEditTextJoinUserId.text.toString()
                val verificationCode = textInputEditTextJoinIdCheck.text.toString()
                val password = textInputEditTextJoinUserPw.text.toString()
                val name = textInputEditTextJoinUserName.text.toString()
                val passwordCheck = textInputEditTextJoinUserPwCheck.text.toString()

                // 경고 메시지 초기화
                warningJoinEmailFormat.visibility = View.GONE
                warningJoinPassword.visibility = View.GONE
                warningJoinPasswordCheck.visibility = View.GONE
                warningJoinNameFormat.visibility = View.GONE
                warningJoinEmailAlready.visibility = View.GONE

                // 예외처리
                if (!authViewModel.isEmailValid(email)) {
                    warningJoinEmailFormat.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    warningJoinEmailFormat.visibility = View.GONE
                }

                if (!authViewModel.isPasswordValid(password)) {
                    warningJoinPassword.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    warningJoinPassword.visibility = View.GONE
                }

                if (password != passwordCheck) {
                    warningJoinPasswordCheck.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    warningJoinPasswordCheck.visibility = View.GONE
                }

                if (!authViewModel.isNameValid(name)) {
                    warningJoinNameFormat.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    warningJoinNameFormat.visibility = View.GONE
                }

                // Firebase Authentication을 사용하여 사용자 등록
                val registrationLiveData = authViewModel.registerUser(email, password, name)

                registrationLiveData.observe(viewLifecycleOwner, Observer { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        // 등록 성공 시 처리
                        showRegistrationSuccessDialog()
                        mainActivity.replaceFragment(MainActivity.AUTH_LOGIN_FRAGMENT, true, null)
                        Log.d("AuthJoinFragment", "replaceFragment 호출됨")
                    } else {
                        showErrorMessageDialog("가입 실패")
                    }
                })

            }
        }
        return fragmentAuthJoinBinding.root
    }

    // 이메일로 인증코드를 보내는 함수
//    private fun verifyEmail() {
//        val email = textInputEditTextJoinUserId.text.toString()
//
//        if (email.isEmpty()) {
//            // 이메일 주소를 입력하지 않은 경우 처리
//            // 사용자에게 이메일 주소를 입력하라는 메시지를 보여줄 수 있습니다.
//            return
//        }
//
//        // Firebase Authentication을 사용하여 이메일로 인증 코드를 보냅니다.
//        auth.sendSignInLinkToEmail(email, null)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // 인증 코드를 성공적으로 보냈을 때의 처리
//                    // 사용자에게 이메일이 성공적으로 전송되었음을 알리는 메시지를 보여줄 수 있습니다.
//                } else {
//                    // 인증 코드를 보내는 데 실패한 경우 처리
//                    val exception = task.exception
//                    if (exception != null) {
//                        // 오류 처리를 추가할 수 있습니다.
//                        Log.e("AuthJoinFragment", "인증 코드 보내기 실패: ${exception.message}")
//                    }
//                }
//            }
//    }

    // 가입 성공 다이얼로그를 보여주는 함수
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

        // 로그 메시지 추가
        Log.d("AuthJoinFragment", "showRegistrationSuccessDialog() 호출됨")
    }

    // 오류 처리 다이얼로그를 보여주는 함수
    private fun showErrorMessageDialog(message: String) {
        val alertDialogBuilder =
            AlertDialog.Builder(requireContext()).setTitle("오류").setMessage(message)
                .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}