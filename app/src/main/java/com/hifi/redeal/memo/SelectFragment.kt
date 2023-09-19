package com.hifi.redeal.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentSelectBinding

class SelectFragment : Fragment() {

    lateinit var fragmentSelectBinding: FragmentSelectBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSelectBinding = FragmentSelectBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        fragmentSelectBinding.run{
            photoMemoBtn.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.PHOTO_MEMO_FRAGMENT, true, null)
            }
            recordMemoBtn.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.RECORD_MEMO_FRAGMENT, true, null)
            }
        }
        return fragmentSelectBinding.root
    }
}