package com.hifi.redeal.auth

import android.os.Bundle
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
import com.hifi.redeal.model.UserDataClass
import com.hifi.redeal.vm.AuthViewModel

class AuthJoinFragment : Fragment() {

    private lateinit var fragmentAuthJoinBinding: FragmentAuthJoinBinding
    lateinit var mainActivity: MainActivity
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAuthJoinBinding = FragmentAuthJoinBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        fragmentAuthJoinBinding.run {
            toolbarAuthJoin.setNavigationOnClickListener {
                mainActivity.removeFragment(MainActivity.AUTH_JOIN_FRAGMENT)
            }

            buttonJoinIdCheck.setOnClickListener {

            }

            buttonJoinComplete.setOnClickListener {
                val email = textInputEditTextJoinUserId.text.toString()
                val password = textInputEditTextJoinUserPw.text.toString()
                val name = textInputEditTextJoinUserName.text.toString()
                val passwordCheck = textInputEditTextJoinUserPwCheck.text.toString()
                // 경고 메시지 초기화
                warningJoinEmailFormat.visibility = View.GONE
                warningJoinPassword.visibility = View.GONE
                warningJoinPasswordCheck.visibility = View.GONE
                warningJoinNicknameFormat.visibility = View.GONE
                warningJoinEmailAlready.visibility = View.GONE
                warningJoinNicknameAlready.visibility = View.GONE

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

                if (!authViewModel.isNicknameValid(name)) {
                    warningJoinNicknameFormat.visibility = View.VISIBLE
                    return@setOnClickListener
                } else {
                    warningJoinNicknameFormat.visibility = View.GONE
                }

                // Firebase Authentication을 사용하여 사용자 등록
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Firebase Authentication에서 사용자 등록 성공
                            val firebaseUser = task.result?.user

                            // Firestore에 추가할 사용자 정보 선언
                            val userData =
                                UserDataClass(0, email, password, name) // 여기도 UserDataClass로 변경

                            firebaseUser?.uid?.let { uid ->
                                // Firestore에 사용자 정보 추가 함수 호출
                                authViewModel.registerUser(email, password, name)
                            }
                        } else {
                            // Firebase Authentication에서 사용자 등록 실패
                            showErrorMessageDialog("Firebase Authentication에서 사용자 등록 실패: ${task.exception?.message}")
                        }
                    }

                // LiveData를 옵저빙하여 결과 처리
                authViewModel.registrationResult.observe(
                    viewLifecycleOwner,
                    Observer { registrationSuccess ->
                        if (registrationSuccess) {
                            showRegistrationSuccessDialog()
                            // 가입 성공 시 화면 이동 로직 추가
                            mainActivity.replaceFragment(
                                MainActivity.AUTH_LOGIN_FRAGMENT,
                                true,
                                null
                            )
                        } else {
                            showErrorMessageDialog("가입 실패")
                        }
                    }
                )
            }
        }
        return fragmentAuthJoinBinding.root
    }


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