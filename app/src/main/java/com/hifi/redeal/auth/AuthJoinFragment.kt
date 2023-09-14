package com.hifi.redeal.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAuthFindPwBinding
import com.hifi.redeal.databinding.FragmentAuthJoinBinding


class AuthJoinFragment : Fragment() {

    private lateinit var fragmentAuthJoinBinding: FragmentAuthJoinBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAuthJoinBinding = FragmentAuthJoinBinding.inflate(inflater)
        // Inflate the layout for this fragment

        return fragmentAuthJoinBinding.root
    }
}