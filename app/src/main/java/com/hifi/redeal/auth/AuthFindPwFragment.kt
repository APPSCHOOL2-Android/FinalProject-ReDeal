package com.hifi.redeal.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentAuthFindPwBinding

class AuthFindPwFragment : Fragment() {

    private lateinit var fragmentAuthFindPwBinding: FragmentAuthFindPwBinding
    lateinit var mainActivity: MainActivity

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

            buttonJoinComplete.setOnClickListener{
                 val email =  textInputEditTextFindPwUserId.toString()

            }

            return fragmentAuthFindPwBinding.root
        }
    }
}
