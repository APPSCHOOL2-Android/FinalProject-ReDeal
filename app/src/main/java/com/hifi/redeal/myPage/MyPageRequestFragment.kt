package com.hifi.redeal.myPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMyPageRequestBinding
import com.hifi.redeal.myPage.repository.MyPageRepository

class MyPageRequestFragment : Fragment() {
    private lateinit var fragmentMyPageRequestBinding: FragmentMyPageRequestBinding
    private lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMyPageRequestBinding = FragmentMyPageRequestBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        fragmentMyPageRequestBinding.run{
            myPageRequestToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MY_PAGE_REQUEST_FRAGMENT)
                }
            }

            myPageRequestEditNameConfirmBtn.setOnClickListener {
                val msg = myPageRequestEditNameTextInputEditText.text.toString()
                if(msg.isNotEmpty()){
                    MyPageRepository.addRequestMessage(msg)
                    Snackbar.make(requireView(), "전송 완료", Snackbar.LENGTH_LONG).show()
                    mainActivity.removeFragment(MainActivity.MY_PAGE_REQUEST_FRAGMENT)
                }
            }
        }
        return fragmentMyPageRequestBinding.root
    }
}