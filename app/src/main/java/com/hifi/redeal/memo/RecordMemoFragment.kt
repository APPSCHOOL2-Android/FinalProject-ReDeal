package com.hifi.redeal.memo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentRecordMemoBinding

class RecordMemoFragment : Fragment() {

    private lateinit var fragmentRecordMemoBinding : FragmentRecordMemoBinding
    private lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentRecordMemoBinding = FragmentRecordMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        fragmentRecordMemoBinding.run{
            recordMemoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.RECORD_MEMO_FRAGMENT)
                }
            }
            addRecordMemoBtn.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.ADD_RECORD_MEMO_FRAGMENT, true, null)
            }
        }
        return fragmentRecordMemoBinding.root
    }
}