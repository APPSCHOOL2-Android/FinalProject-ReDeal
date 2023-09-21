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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMemoBinding
import com.hifi.redeal.databinding.RowUserPhotoMemoBinding
import com.hifi.redeal.memo.model.UserPhotoMemoData
import com.hifi.redeal.memo.repository.MemoRepository
import com.hifi.redeal.memo.utils.dpToPx
import com.hifi.redeal.memo.vm.MemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MemoFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("yyyy년 M월 d일 a h시", Locale.getDefault())
    lateinit var fragmentMemoBinding: FragmentMemoBinding
    lateinit var mainActivity: MainActivity
    lateinit var memoViewModel: MemoViewModel
    private val userIdx = Firebase.auth.uid
    val drawableClientStateArr = arrayOf(
        R.drawable.circle_big_24px_primary20,
        R.drawable.circle_big_24px_primary50,
        R.drawable.circle_big_24px_primary80
    )
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
            memoViewSwitcher.displayedChild = 0
            memoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MEMO_FRAGMENT)
                }
            }
            userPhotoMemoRecyclerView.run{
                adapter = AllPhotoMemoRecyclerAdapter()
                layoutManager = LinearLayoutManager(context)
            }
        }
        return fragmentMemoBinding.root
    }

    inner class AllPhotoMemoRecyclerAdapter: RecyclerView.Adapter<AllPhotoMemoRecyclerAdapter.UserPhotoMemoViewHolder>(){
        inner class UserPhotoMemoViewHolder(rowUserPhotoMemoBinding: RowUserPhotoMemoBinding): RecyclerView.ViewHolder(rowUserPhotoMemoBinding.root){
            private val userPhotoImageListLayout = rowUserPhotoMemoBinding.userPhotoImageListLayout
            private val userPhotoDateTextView = rowUserPhotoMemoBinding.userPhotoDateTextView
            private val userPhotoMemoTextView = rowUserPhotoMemoBinding.userPhotoMemoTextView
            private val userPhotoMemoClientState = rowUserPhotoMemoBinding.userPhotoMemoClientState
            private val userPhotoMemoEnterClientDetailBtn = rowUserPhotoMemoBinding.userPhotoMemoEnterClientDetailBtn
            private val userPhotoMemoClientName = rowUserPhotoMemoBinding.userPhotoMemoClientName
            private val userPhotoMemoClientManagerName = rowUserPhotoMemoBinding.userPhotoMemoClientManagerName
            fun bindItem(item: UserPhotoMemoData){
                userPhotoDateTextView.text = dateFormat.format(item.date * 1000)
                userPhotoMemoTextView.text = item.context
                userPhotoMemoClientName.text = "로딩 중"
                userPhotoMemoClientManagerName.text = "로딩 중"
                userPhotoMemoEnterClientDetailBtn.setOnClickListener {
                    val newBundle = Bundle()
                    newBundle.putLong("clientIdx", item.clientIdx)
                    mainActivity.replaceFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT, true, newBundle)
                }
                Log.d("testaaa" ,"user : $userIdx client: ${item.clientIdx}")
                MemoRepository.getUserPhotoMemoClientInfo(userIdx!!, item.clientIdx){documentSnapshot ->
                    userPhotoMemoClientName.text = documentSnapshot.get("clientName") as String
                    userPhotoMemoClientManagerName.text = documentSnapshot.get("clientManagerName") as String
                    userPhotoMemoClientState.setImageResource(drawableClientStateArr[(item.clientIdx.toInt()) - 1] )
                }

                var linearLayoutHorizontal = LinearLayout(requireContext())
                linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0,0,0, dpToPx(requireContext(), 6))
                linearLayoutHorizontal.layoutParams = layoutParams
                userPhotoImageListLayout.removeAllViews()
                for(i in 0 until item.srcArr.size){
                    if(i != 0 && i % 3 == 0) {
                        userPhotoImageListLayout.addView(linearLayoutHorizontal)
                        val newLinearLayoutHorizontal = LinearLayout(requireContext())
                        linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(0,0,0, dpToPx(requireContext(), 6))
                        linearLayoutHorizontal.layoutParams = layoutParams
                        linearLayoutHorizontal = newLinearLayoutHorizontal
                    }
                    val imageViewLayoutParams = ViewGroup.MarginLayoutParams(
                        dpToPx(requireContext(), 100),
                        dpToPx(requireContext(), 100)
                    )
                    if(i%3 != 0) {
                        imageViewLayoutParams.marginStart = dpToPx(requireContext(), 6)
                    }
                    val imageView = ImageView(requireContext())
                    imageView.layoutParams = imageViewLayoutParams
                    imageView.setImageResource(R.drawable.empty_photo)
                    imageView.setOnClickListener {
                        val newBundle = Bundle()
                        newBundle.putInt("order", i)
                        newBundle.putStringArrayList("srcArr", item.srcArr as ArrayList<String>)
                        mainActivity.replaceFragment(MainActivity.PHOTO_DETAIL_FRAGMENT, true, newBundle)
                    }
                    linearLayoutHorizontal.addView(imageView)
                    MemoRepository.getUserPhotoMemoImgUrl(userIdx!!, item.srcArr[i]){ url ->
                        Glide.with(imageView)
                            .load(url)
                            .into(imageView)
                    }
                }
                userPhotoImageListLayout.addView(linearLayoutHorizontal)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPhotoMemoViewHolder {
            val rowUserPhotoMemoBinding = RowUserPhotoMemoBinding.inflate(layoutInflater)
            val userPhotoMemoViewHolder = UserPhotoMemoViewHolder(rowUserPhotoMemoBinding)

            rowUserPhotoMemoBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return userPhotoMemoViewHolder
        }

        override fun getItemCount(): Int {
            return memoViewModel.userPhotoMemoList.value?.size!!
        }

        override fun onBindViewHolder(holder: UserPhotoMemoViewHolder, position: Int) {
            val item = memoViewModel.userPhotoMemoList.value?.get(position)!!
            holder.bindItem(item)
        }
    }
}