package com.hifi.redeal.memo.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowUserPhotoMemoBinding
import com.hifi.redeal.memo.model.UserPhotoMemoData
import com.hifi.redeal.memo.repository.MemoRepository
import com.hifi.redeal.memo.utils.dpToPx
import com.hifi.redeal.memo.utils.intervalBetweenDateText
import com.hifi.redeal.memo.vm.MemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class UserPhotoMemoListAdapter(
    val mainActivity: MainActivity,
    val memoViewModel: MemoViewModel
): ListAdapter<UserPhotoMemoData, RecyclerView.ViewHolder>(diffUtil){

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val drawableClientStateArr = arrayOf(
        R.drawable.circle_big_24px_primary20,
        R.drawable.circle_big_24px_primary50,
        R.drawable.circle_big_24px_primary80
    )
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<UserPhotoMemoData>() {
            override fun areItemsTheSame(oldItem: UserPhotoMemoData, newItem: UserPhotoMemoData): Boolean {
                return oldItem.clientIdx == newItem.clientIdx
            }

            override fun areContentsTheSame(oldItem: UserPhotoMemoData, newItem: UserPhotoMemoData): Boolean {
                return oldItem == newItem
            }
        }
    }
    val ITEM = 0
    val FOOTER = 1
    inner class UserPhotoMemoViewHolder(rowUserPhotoMemoBinding: RowUserPhotoMemoBinding): RecyclerView.ViewHolder(rowUserPhotoMemoBinding.root){
        private val userPhotoImageListLayout = rowUserPhotoMemoBinding.userPhotoImageListLayout
        private val userPhotoDateTextView = rowUserPhotoMemoBinding.userPhotoDateTextView
        private val userPhotoMemoTextView = rowUserPhotoMemoBinding.userPhotoMemoTextView
        private val userPhotoMemoClientState = rowUserPhotoMemoBinding.userPhotoMemoClientState
        private val userPhotoMemoEnterClientDetailBtn = rowUserPhotoMemoBinding.userPhotoMemoEnterClientDetailBtn
        private val userPhotoMemoClientName = rowUserPhotoMemoBinding.userPhotoMemoClientName
        private val userPhotoMemoClientManagerName = rowUserPhotoMemoBinding.userPhotoMemoClientManagerName
        fun bindItem(item: UserPhotoMemoData){
            userPhotoDateTextView.text = intervalBetweenDateText(dateFormat.format(item.date.toDate()))
            userPhotoMemoTextView.text = item.context.ifEmpty { "메모를 등록하지 않았어요" }

            userPhotoMemoClientName.text = "로딩 중"
            userPhotoMemoClientManagerName.text = "로딩 중"
            userPhotoMemoEnterClientDetailBtn.setOnClickListener {
                val newBundle = Bundle()
                newBundle.putLong("clientIdx", item.clientIdx)
                mainActivity.replaceFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT, true, newBundle)
            }
            MemoRepository.getUserMemoClientInfo(mainActivity.uid, item.clientIdx){ documentSnapshot ->
                userPhotoMemoClientName.text = documentSnapshot.get("clientName") as String
                userPhotoMemoClientManagerName.text = documentSnapshot.get("clientManagerName") as String
                val clientState = documentSnapshot.get("clientState") as Long
                userPhotoMemoClientState.setImageResource(drawableClientStateArr[clientState.toInt() - 1] )
            }

            var linearLayoutHorizontal = LinearLayout(mainActivity)
            linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0,0,0, dpToPx(mainActivity, 6))
            linearLayoutHorizontal.layoutParams = layoutParams
            userPhotoImageListLayout.removeAllViews()
            for(i in 0 until item.srcArr.size){
                if(i != 0 && i % 3 == 0) {
                    userPhotoImageListLayout.addView(linearLayoutHorizontal)
                    val newLinearLayoutHorizontal = LinearLayout(mainActivity)
                    linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0,0,0, dpToPx(mainActivity, 6))
                    linearLayoutHorizontal.layoutParams = layoutParams
                    linearLayoutHorizontal = newLinearLayoutHorizontal
                }
                val imageViewLayoutParams = ViewGroup.MarginLayoutParams(
                    dpToPx(mainActivity, 100),
                    dpToPx(mainActivity, 100)
                )
                if(i%3 != 0) {
                    imageViewLayoutParams.marginStart = dpToPx(mainActivity, 6)
                }
                val imageView = ImageView(mainActivity)
                imageView.layoutParams = imageViewLayoutParams
                imageView.setImageResource(R.drawable.empty_photo)
                imageView.setOnClickListener {
                    val newBundle = Bundle()
                    newBundle.putInt("order", i)
                    newBundle.putStringArrayList("srcArr", item.srcArr as ArrayList<String>)
                    mainActivity.replaceFragment(MainActivity.PHOTO_DETAIL_FRAGMENT, true, newBundle)
                }
                linearLayoutHorizontal.addView(imageView)
                MemoRepository.getUserPhotoMemoImgUrl(mainActivity.uid, item.srcArr[i]){ url ->
                    Glide.with(imageView)
                        .load(url)
                        .into(imageView)
                }
            }
            userPhotoImageListLayout.addView(linearLayoutHorizontal)
        }
    }

    inner class UserPhotoMemoFooterViewHolder(rowFooterAccountListBinding: RowFooterAccountListBinding): RecyclerView.ViewHolder(rowFooterAccountListBinding.root) {
        private val textViewRowFooterAccountList = rowFooterAccountListBinding.textViewRowFooterAccountList
        fun bind() {
            if (memoViewModel.userPhotoMemoList.value?.isEmpty()!!) {
                textViewRowFooterAccountList.text = "포모 메모가 없습니다"
            } else {
                textViewRowFooterAccountList.text = "등록된 포토 메모 ${memoViewModel.userPhotoMemoList.value?.size}개"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        when (viewType) {
            ITEM -> {
                val rowUserPhotoMemoBinding = RowUserPhotoMemoBinding.inflate(inflater)
                rowUserPhotoMemoBinding.root.layoutParams = params
                return UserPhotoMemoViewHolder(rowUserPhotoMemoBinding)
            }
            else -> {
                val rowFooterAccountListBinding = RowFooterAccountListBinding.inflate(inflater)
                rowFooterAccountListBinding.root.layoutParams = params
                return UserPhotoMemoFooterViewHolder(rowFooterAccountListBinding)
            }
        }
    }


    override fun getItemCount(): Int {
        return memoViewModel.userPhotoMemoList.value?.size!! + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            FOOTER
        } else {
            ITEM
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserPhotoMemoListAdapter.UserPhotoMemoViewHolder -> {
                val item = memoViewModel.userPhotoMemoList.value?.get(position)!!
                holder.bindItem(item)
            }
            is UserPhotoMemoListAdapter.UserPhotoMemoFooterViewHolder -> {
                holder.bind()
            }
        }
    }
}