package com.hifi.redeal.account

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAccountEditBinding

class AccountEditFragment : Fragment(){

    lateinit var mainActivity: MainActivity
    lateinit var fragmentAccountEditBinding: FragmentAccountEditBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAccountEditBinding = FragmentAccountEditBinding.inflate(layoutInflater)

        setFragmentResultListener("addressSearchResult") { _, bundle ->
            val data = bundle.getString("address")
            fragmentAccountEditBinding.textEditTextAccountEditZipCode.setText(data.toString())
        }

        fragmentAccountEditBinding.run {
            val items = listOf("거래 중", "거래 시도", "거래 중지")
            val adapter = CustomArrayAdapter(items)
            textEditTextAccountEditState.run {
                setAdapter(adapter)

                setOnItemClickListener { parent, view, position, id ->

                    val scale = resources.displayMetrics.density // 화면 밀도를 가져옴
                    val pixel = (4 * scale + 0.5f).toInt()

                    compoundDrawablePadding = pixel

                    when (position) {
                        0 -> setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.circle_big_16px_primary20, 0, 0, 0)
                        1 -> setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.circle_big_16px_primary50, 0, 0, 0)
                        2 -> setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.circle_big_16px_primary80, 0, 0, 0)
                    }
                }
            }

            buttonAccountEditAddressSearch.setOnClickListener {
                mainActivity.navigateTo(R.id.addressSearchFragment)
            }
        }

        return fragmentAccountEditBinding.root
    }

    inner class CustomArrayAdapter(items: List<String>): ArrayAdapter<String>(
        requireContext(),
        R.layout.dropdown_menu_item_account_edit,
        items
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            when ((view as TextView).text) {
                "거래 중" -> view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_big_16px_primary20, 0, 0, 0)
                "거래 시도" -> view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_big_16px_primary50, 0, 0, 0)
                "거래 중지" -> view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.circle_big_16px_primary80, 0, 0, 0)
            }
            return view
        }
    }
}