package com.hifi.redeal.memo.adapter

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowUserRecordMemoBinding
import com.hifi.redeal.memo.model.UserRecordMemoData
import com.hifi.redeal.memo.repository.MemoRepository
import com.hifi.redeal.memo.utils.getCurrentDuration
import com.hifi.redeal.memo.utils.getTotalDuration
import com.hifi.redeal.memo.utils.intervalBetweenDateText
import com.hifi.redeal.memo.vm.MemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class UserRecordMemoListAdapter(
    val mainActivity: MainActivity,
    val memoViewModel: MemoViewModel
): ListAdapter<UserRecordMemoData, RecyclerView.ViewHolder>(diffUtil){

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val drawableClientStateArr = arrayOf(
        R.drawable.circle_big_24px_primary20,
        R.drawable.circle_big_24px_primary50,
        R.drawable.circle_big_24px_primary80
    )
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<UserRecordMemoData>() {
            override fun areItemsTheSame(oldItem: UserRecordMemoData, newItem: UserRecordMemoData): Boolean {
                return oldItem.clientIdx == newItem.clientIdx
            }

            override fun areContentsTheSame(oldItem: UserRecordMemoData, newItem: UserRecordMemoData): Boolean {
                return oldItem == newItem
            }
        }

        var currentMediaPlayer:MediaPlayer? = null
        var isAudioPlaying = false
        val handler = Handler()
        var currentSeekBar:SeekBar? = null
        var currentTimeTextView: TextView? = null
        var currentPlayPosition: AppCompatButton? = null
        fun resetAudio(){
            isAudioPlaying = false
            handler.removeCallbacksAndMessages(null)
            currentMediaPlayer?.release()
            currentMediaPlayer = null
            currentSeekBar?.progress = 0
            currentTimeTextView?.text = getCurrentDuration(0)
            currentSeekBar = null
            currentTimeTextView = null
            currentPlayPosition?.setBackgroundResource(R.drawable.play_arrow_24px)
            currentPlayPosition = null
        }
    }
    val ITEM = 0
    val FOOTER = 1
    inner class UserRecordMemoViewHolder(rowUserRecordMemoBinding: RowUserRecordMemoBinding): RecyclerView.ViewHolder(rowUserRecordMemoBinding.root){
        private val userRecordDateTextView = rowUserRecordMemoBinding.userRecordDateTextView
        private val userRecordMemoTextView = rowUserRecordMemoBinding.userRecordMemoTextView
        private val userRecordMemoFilenameTextView = rowUserRecordMemoBinding.userRecordMemoFilenameTextView
        private val userRecordMemoAudioPlayBtn = rowUserRecordMemoBinding.userRecordMemoAudioPlayBtn
        private val userRecordMemoResetRecordBtn = rowUserRecordMemoBinding.userRecordMemoResetRecordBtn
        private val userRecordMemoCurrentDurationTimeTextView = rowUserRecordMemoBinding.userRecordMemoCurrentDurationTimeTextView
        private val userRecordMemoTotalDurationTimeTextView = rowUserRecordMemoBinding.userRecordMemoTotalDurationTimeTextView
        private val userRecordMemoAudioSeekBar = rowUserRecordMemoBinding.userRecordMemoAudioSeekBar
        private val userRecordMemoClientState = rowUserRecordMemoBinding.userRecordMemoClientState
        private val userRecordMemoEnterClientDetailBtn = rowUserRecordMemoBinding.userRecordMemoEnterClientDetailBtn
        private val userRecordMemoClientName = rowUserRecordMemoBinding.userRecordMemoClientName
        private val userRecordMemoClientManagerName = rowUserRecordMemoBinding.userRecordMemoClientManagerName

        init{
            userRecordMemoResetRecordBtn.setOnClickListener {
                if(isAudioPlaying){
                    resetAudio()
                }
            }
        }
        fun bindItem(item: UserRecordMemoData){
            userRecordDateTextView.text = intervalBetweenDateText(dateFormat.format(item.date.toDate()))
            userRecordMemoTextView.text = item.context.ifEmpty { "메모를 등록하지 않았어요" }

            userRecordMemoClientName.text = "로딩 중"
            userRecordMemoClientManagerName.text = "로딩 중"
            userRecordMemoEnterClientDetailBtn.setOnClickListener {
                resetAudio()
                val newBundle = Bundle()
                newBundle.putLong("clientIdx", item.clientIdx)
                mainActivity.replaceFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT, true, newBundle)
            }
            MemoRepository.getUserMemoClientInfo(mainActivity.uid, item.clientIdx){ documentSnapshot ->
                userRecordMemoClientName.text = documentSnapshot.get("clientName") as String
                userRecordMemoClientManagerName.text = documentSnapshot.get("clientManagerName") as String
                val clientState = documentSnapshot.get("clientState") as Long
                userRecordMemoClientState.setImageResource(drawableClientStateArr[clientState.toInt() - 1] )
            }

            userRecordMemoFilenameTextView.text = item.audioFilename
            userRecordMemoAudioSeekBar.progress = 0
            userRecordMemoAudioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentMediaPlayer?.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            userRecordMemoCurrentDurationTimeTextView.text = getCurrentDuration(0)
            userRecordMemoTotalDurationTimeTextView.text = "로딩 중"
            if(item.audioSrc != null) {
                var tempMediaPlayer = MediaPlayer.create(mainActivity, item.audioSrc)
                userRecordMemoTotalDurationTimeTextView.text = getTotalDuration(tempMediaPlayer)
                userRecordMemoAudioSeekBar.max = tempMediaPlayer?.duration!!
                userRecordMemoAudioPlayBtn.setOnClickListener {
                    if(currentPlayPosition == null || currentPlayPosition != userRecordMemoAudioPlayBtn){
                        resetAudio()
                        currentPlayPosition?.setBackgroundResource(R.drawable.play_arrow_24px)
                        currentPlayPosition = userRecordMemoAudioPlayBtn
                        currentSeekBar = userRecordMemoAudioSeekBar
                        currentTimeTextView = userRecordMemoCurrentDurationTimeTextView
                        currentMediaPlayer = MediaPlayer.create(mainActivity, item.audioSrc)
                        currentMediaPlayer?.start()
                        isAudioPlaying = true
                        handler.post(updateSeekBar())
                        userRecordMemoAudioPlayBtn.setBackgroundResource(R.drawable.pause_24px)
                        currentMediaPlayer?.setOnCompletionListener {
                            userRecordMemoAudioPlayBtn.setBackgroundResource(R.drawable.play_arrow_24px)
                            currentPlayPosition = null
                            resetAudio()
                        }
                    }else{
                        if(isAudioPlaying) {
                            currentMediaPlayer?.pause()
                            handler.removeCallbacksAndMessages(null)
                            isAudioPlaying = false
                            userRecordMemoAudioPlayBtn.setBackgroundResource(R.drawable.play_arrow_24px)
                        }else {
                            currentMediaPlayer?.start()
                            isAudioPlaying = true
                            handler.post(updateSeekBar())
                            userRecordMemoAudioPlayBtn.setBackgroundResource(R.drawable.pause_24px)
                        }
                    }
                }
            }
        }
    }

    inner class UserRecordMemoFooterViewHolder(rowFooterAccountListBinding: RowFooterAccountListBinding): RecyclerView.ViewHolder(rowFooterAccountListBinding.root) {
        private val textViewRowFooterAccountList = rowFooterAccountListBinding.textViewRowFooterAccountList
        fun bind() {
            if (memoViewModel.userRecordMemoList.value?.isEmpty()!!) {
                textViewRowFooterAccountList.text = "음성 메모가 없습니다"
            } else {
                textViewRowFooterAccountList.text = "등록된 음성 메모 ${memoViewModel.userRecordMemoList.value?.size}개"
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
                val rowUserRecordMemoBinding = RowUserRecordMemoBinding.inflate(inflater)
                rowUserRecordMemoBinding.root.layoutParams = params
                return UserRecordMemoViewHolder(rowUserRecordMemoBinding)
            }
            else -> {
                val rowFooterAccountListBinding = RowFooterAccountListBinding.inflate(inflater)
                rowFooterAccountListBinding.root.layoutParams = params
                return UserRecordMemoFooterViewHolder(rowFooterAccountListBinding)
            }
        }
    }


    override fun getItemCount(): Int {
        return memoViewModel.userRecordMemoList.value?.size!! + 1
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
            is UserRecordMemoListAdapter.UserRecordMemoViewHolder -> {
                val item = memoViewModel.userRecordMemoList.value?.get(position)!!
                holder.bindItem(item)
            }
            is UserRecordMemoListAdapter.UserRecordMemoFooterViewHolder -> {
                holder.bind()
            }
        }
    }
    private fun updateSeekBar() = object:Runnable {
        override fun run() {
            if (isAudioPlaying && currentMediaPlayer != null) {
                val currentPosition = currentMediaPlayer?.currentPosition!!
                currentSeekBar?.progress = currentPosition
                currentTimeTextView?.text = getCurrentDuration(currentPosition)
                handler.postDelayed(this, 20)
            }
        }
    }
}