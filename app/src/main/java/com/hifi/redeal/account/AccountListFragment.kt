package com.hifi.redeal.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAccountListBinding

class AccountListFragment : Fragment() {

    lateinit var fragmentAccountListBinding: FragmentAccountListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAccountListBinding = FragmentAccountListBinding.inflate(layoutInflater)

        fragmentAccountListBinding.run {

        }

        return fragmentAccountListBinding.root
    }
}