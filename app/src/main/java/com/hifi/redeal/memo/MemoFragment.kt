package com.hifi.redeal.memo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.adapter.AccountListAdapter
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.databinding.FragmentMemoBinding
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowItemAccountListBinding
import com.hifi.redeal.databinding.RowUserPhotoMemoBinding
import com.hifi.redeal.memo.adapter.UserPhotoMemoListAdapter
import com.hifi.redeal.memo.model.UserPhotoMemoData
import com.hifi.redeal.memo.repository.MemoRepository
import com.hifi.redeal.memo.utils.dpToPx
import com.hifi.redeal.memo.vm.MemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MemoFragment : Fragment() {
    lateinit var fragmentMemoBinding: FragmentMemoBinding
    lateinit var mainActivity: MainActivity
    lateinit var memoViewModel: MemoViewModel
    private val userIdx = Firebase.auth.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMemoBinding = FragmentMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        memoViewModel = ViewModelProvider(this)[MemoViewModel::class.java]
        memoViewModel.run{
            userPhotoMemoList.observe(viewLifecycleOwner){
                fragmentMemoBinding.userPhotoMemoRecyclerView.adapter?.notifyDataSetChanged()
            }
        }

        fragmentMemoBinding.run{
            memoViewModel.getUserPhotoMemoList(userIdx!!)
            val photoMemoListAdapter = UserPhotoMemoListAdapter(mainActivity, memoViewModel)
            memoViewSwitcher.displayedChild = 0
            memoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MEMO_FRAGMENT)
                }
            }
            userPhotoMemoRecyclerView.run{
                adapter = photoMemoListAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }
        return fragmentMemoBinding.root
    }
}