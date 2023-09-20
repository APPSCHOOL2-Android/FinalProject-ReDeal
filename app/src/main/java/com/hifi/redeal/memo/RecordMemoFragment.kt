package com.hifi.redeal.memo

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentRecordMemoBinding
import com.hifi.redeal.databinding.RowRecordMemoBinding
import com.hifi.redeal.memo.model.RecordMemoData
import com.hifi.redeal.memo.utils.getCurrentDuration
import com.hifi.redeal.memo.utils.getTotalDuration
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
    var currentMediaPlayer:MediaPlayer? = null
    var isAudioPlaying = false
    val handler = Handler()
    var currentSeekBar:SeekBar? = null
    var currentTimeTextView:TextView? = null
    private var currentPlayPosition: AppCompatButton? = null
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
            recordMemoViewModel.getRecordMemoList(userIdx, clientIdx, mainActivity)
            recordMemoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.RECORD_MEMO_FRAGMENT)
                }
            }
            addRecordMemoBtn.setOnClickListener {
                val newBundle = Bundle()
                newBundle.putLong("clientIdx", clientIdx)
                currentPlayPosition = null
                resetAudio()
                mainActivity.replaceFragment(MainActivity.ADD_RECORD_MEMO_FRAGMENT, true, newBundle)
            }
            recordMemoRecyclerView.run{
                adapter = RecordMemoRecyclerAdapter()
                layoutManager = LinearLayoutManager(context)
            }
        }
        return fragmentRecordMemoBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        resetAudio()
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
                recordMemoResetRecordBtn.setOnClickListener {
                    if(isAudioPlaying){
                        resetAudio()
                    }
                }
            }
            fun bindItem(item: RecordMemoData){
                recordDateTextView.text = dateFormat.format(item.date * 1000)
                recordMemoTextView.text = item.context
                recordMemoFilenameTextView.text = item.audioFilename
                recordMemoAudioSeekBar.progress = 0
                recordMemoAudioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                recordMemoCurrentDurationTimeTextView.text = getCurrentDuration(0)
                recordMemoTotalDurationTimeTextView.text = "로딩 중"
                if(item.audioSrc != null) {
                    var tempMediaPlayer = MediaPlayer.create(requireContext(), item.audioSrc)
                    recordMemoTotalDurationTimeTextView.text = getTotalDuration(tempMediaPlayer)
                    recordMemoAudioSeekBar.max = tempMediaPlayer?.duration!!
                    recordMemoAudioPlayBtn.setOnClickListener {
                        if(currentPlayPosition == null || currentPlayPosition != recordMemoAudioPlayBtn){
                            resetAudio()
                            currentPlayPosition?.setBackgroundResource(R.drawable.play_arrow_24px)
                            currentPlayPosition = recordMemoAudioPlayBtn
                            currentSeekBar = recordMemoAudioSeekBar
                            currentTimeTextView = recordMemoCurrentDurationTimeTextView
                            currentMediaPlayer = MediaPlayer.create(requireContext(), item.audioSrc)
                            currentMediaPlayer?.start()
                            isAudioPlaying = true
                            handler.post(updateSeekBar())
                            recordMemoAudioPlayBtn.setBackgroundResource(R.drawable.pause_24px)
                            currentMediaPlayer?.setOnCompletionListener {
                                recordMemoAudioPlayBtn.setBackgroundResource(R.drawable.play_arrow_24px)
                                currentPlayPosition = null
                                resetAudio()
                            }
                        }else{
                            if(isAudioPlaying) {
                                currentMediaPlayer?.pause()
                                handler.removeCallbacksAndMessages(null)
                                isAudioPlaying = false
                                recordMemoAudioPlayBtn.setBackgroundResource(R.drawable.play_arrow_24px)
                            }else {
                                currentMediaPlayer?.start()
                                isAudioPlaying = true
                                handler.post(updateSeekBar())
                                recordMemoAudioPlayBtn.setBackgroundResource(R.drawable.pause_24px)
                            }
                        }
                    }
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
        handler.removeCallbacksAndMessages(null)
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        currentSeekBar?.progress = 0
        currentTimeTextView?.text = getCurrentDuration(0)
        currentSeekBar = null
        currentTimeTextView = null
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