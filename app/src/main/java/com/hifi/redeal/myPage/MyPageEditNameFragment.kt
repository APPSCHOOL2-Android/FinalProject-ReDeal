package com.hifi.redeal.myPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMyPageEditNameBinding
import com.hifi.redeal.myPage.repository.MyPageRepository

class MyPageEditNameFragment : Fragment() {
    private lateinit var fragmentMyPageEditNameBinding: FragmentMyPageEditNameBinding
    private lateinit var mainActivity: MainActivity

    private val namePolicy = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s]+$".toRegex()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMyPageEditNameBinding = FragmentMyPageEditNameBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        val uid = arguments?.getString("uid")
        val currentUserName = arguments?.getString("userName")

        fragmentMyPageEditNameBinding.run{
            myPageEditNameToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MY_PAGE_EDIT_NAME_FRAGMENT)
                }
            }
            myPageEditNameTextInputEditText.addTextChangedListener {
                myPageEditNameLengthWarningTextView.visibility = View.GONE
                myPageEditNameWarningTextView.visibility = View.GONE
            }
            currentNameTextView.text = currentUserName
            myPageEditNameConfirmBtn.setOnClickListener {
                val editName = myPageEditNameTextInputEditText.text.toString()
                if(editName.length < 2 || editName.length > 12){
                    myPageEditNameLengthWarningTextView.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if(namePolicy.matches(editName)){
                    MyPageRepository.updateUserName(uid!!, editName){
                        Snackbar.make(requireView(), "이름 변경 완료", Snackbar.LENGTH_LONG).show()
                        mainActivity.removeFragment(MainActivity.MY_PAGE_EDIT_NAME_FRAGMENT)
                    }
                }else{
                    myPageEditNameWarningTextView.visibility = View.VISIBLE
                }
            }
        }
        return fragmentMyPageEditNameBinding.root
    }
}