package com.hifi.redeal.memo

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentRecordMemoBinding
import com.hifi.redeal.databinding.RowPhotoMemoBinding
import com.hifi.redeal.databinding.RowRecordMemoBinding
import com.hifi.redeal.memo.model.PhotoMemoData
import com.hifi.redeal.memo.model.RecordMemoData
import com.hifi.redeal.memo.repository.PhotoMemoRepository
import com.hifi.redeal.memo.repository.RecordMemoRepository
import com.hifi.redeal.memo.utils.dpToPx
import com.hifi.redeal.memo.utils.getCurrentDuration
import com.hifi.redeal.memo.utils.getTotalDuration
import com.hifi.redeal.memo.vm.PhotoMemoViewModel
import com.hifi.redeal.memo.vm.RecordMemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class RecordMemoFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("yyyy년 M월 d일 a h시", Locale.getDefault())
    private lateinit var fragmentRecordMemoBinding : FragmentRecordMemoBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var recordMemoViewModel: RecordMemoViewModel
    var userIdx = 1L
    var clientIdx = 1L
    val audioUriList = mutableListOf<Uri>()
    var currentMediaPlayer:MediaPlayer? = null
    var isAudioPlaying = false
    val handler = Handler()
    var currentSeekBar:SeekBar? = null
    var currentTimeTextView:TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentRecordMemoBinding = FragmentRecordMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        clientIdx = arguments?.getLong("clientIdx")?:1L
        recordMemoViewModel = ViewModelProvider(this)[RecordMemoViewModel::class.java]

        recordMemoViewModel.run{
            recordMemoList.observe(viewLifecycleOwner){
                fragmentRecordMemoBinding.recordMemoRecyclerView.adapter?.notifyDataSetChanged()
            }
        }

        fragmentRecordMemoBinding.run{
            recordMemoViewModel.getRecordMemoList(userIdx, clientIdx)
            recordMemoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.RECORD_MEMO_FRAGMENT)
                }
            }
            addRecordMemoBtn.setOnClickListener {
                val newBundle = Bundle()
                newBundle.putLong("clientIdx", clientIdx)
                mainActivity.replaceFragment(MainActivity.ADD_RECORD_MEMO_FRAGMENT, true, newBundle)
            }
            recordMemoRecyclerView.run{
                adapter = RecordMemoRecyclerAdapter()
                layoutManager = LinearLayoutManager(context)
            }
        }
        return fragmentRecordMemoBinding.root
    }

    inner class RecordMemoRecyclerAdapter: RecyclerView.Adapter<RecordMemoRecyclerAdapter.RecordMemoViewHolder>(){
        inner class RecordMemoViewHolder(rowRecordMemoBinding: RowRecordMemoBinding): RecyclerView.ViewHolder(rowRecordMemoBinding.root){
            private val recordDateTextView = rowRecordMemoBinding.recordDateTextView
            private val recordMemoTextView = rowRecordMemoBinding.recordMemoTextView
            private val recordMemoFilenameTextView = rowRecordMemoBinding.recordMemoFilenameTextView
            private val recordMemoAudioPlayBtn = rowRecordMemoBinding.recordMemoAudioPlayBtn
            private val recordMemoResetRecordBtn = rowRecordMemoBinding.recordMemoResetRecordBtn
            private val recordMemoCurrentDurationTimeTextView = rowRecordMemoBinding.recordMemoCurrentDurationTimeTextView
            private val recordMemoTotalDurationTimeTextView = rowRecordMemoBinding.recordMemoTotalDurationTimeTextView
            private val recordMemoAudioSeekBar = rowRecordMemoBinding.recordMemoAudioSeekBar

            init{
                recordMemoAudioPlayBtn.setOnClickListener {
                    Log.d("testaaa", "flag1")
                    resetAudio()
                    currentSeekBar = recordMemoAudioSeekBar
                    currentTimeTextView = recordMemoCurrentDurationTimeTextView
                    currentMediaPlayer = MediaPlayer.create(requireContext(), audioUriList[adapterPosition])
                    currentMediaPlayer?.start()
                    isAudioPlaying = true
                    updateSeekBar()
                    currentMediaPlayer?.setOnCompletionListener {
                        resetAudio()
                    }
                }
                recordMemoResetRecordBtn.setOnClickListener {
                    // todo : 리셋 버튼 클릭 시 처리
                }
            }
            fun bindItem(item: RecordMemoData){
                recordDateTextView.text = dateFormat.format(item.date * 1000)
                recordMemoTextView.text = item.context
                recordMemoFilenameTextView.text = item.audioFilename
                recordMemoCurrentDurationTimeTextView.text = getCurrentDuration(0)
                recordMemoTotalDurationTimeTextView.text = "로딩 중"
                RecordMemoRepository.getRecordMemoRecordUrl(userIdx, item.audioSrc){uri ->
                    audioUriList.add(uri)
                    val tempMediaPlayer = MediaPlayer.create(requireContext(), uri)
                    recordMemoTotalDurationTimeTextView.text = getTotalDuration(tempMediaPlayer)
                    recordMemoAudioSeekBar.max = tempMediaPlayer?.duration!!
                    recordMemoAudioSeekBar.progress = 0
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordMemoViewHolder {
            val rowRecordMemoBinding = RowRecordMemoBinding.inflate(layoutInflater)
            val recordMemoViewHolder = RecordMemoViewHolder(rowRecordMemoBinding)

            rowRecordMemoBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return recordMemoViewHolder
        }

        override fun getItemCount(): Int {
            return recordMemoViewModel.recordMemoList.value?.size!!
        }

        override fun onBindViewHolder(holder: RecordMemoViewHolder, position: Int) {
            val item = recordMemoViewModel.recordMemoList.value?.get(position)!!
            holder.bindItem(item)
        }
    }

    fun resetAudio(){
        isAudioPlaying = false
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        currentSeekBar?.progress = 0
        currentTimeTextView?.text = getCurrentDuration(0)
        currentSeekBar = null
        currentTimeTextView = null
    }

    private fun updateSeekBar() {
        if (isAudioPlaying) {
            val currentPosition = currentMediaPlayer?.currentPosition!!
            currentSeekBar?.progress = currentPosition
            currentTimeTextView?.text = getCurrentDuration(currentPosition)
            handler.postDelayed({ updateSeekBar() }, 20)
            Log.d("testaaa", "time")
        }
    }
}