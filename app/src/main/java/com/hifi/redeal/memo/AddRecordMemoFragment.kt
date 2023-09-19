package com.hifi.redeal.memo

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAddRecordMemoBinding
import com.hifi.redeal.memo.utils.getTotalDuration
import java.io.File
import java.io.IOException
import java.util.UUID


class AddRecordMemoFragment : Fragment() {

    private lateinit var fragmentAddRecordMemoBinding : FragmentAddRecordMemoBinding
    private lateinit var mainActivity: MainActivity

    private val RECORD_VIEW = 0
    private val PREVIEW_VIEW = 1
    private val LOADING_VIEW = 2

    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording: Boolean = false
    private lateinit var recordFileLocation: File
    private var saveFile = false
    private var mGeneratedName: String? = null
    private var mInitialized: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAddRecordMemoBinding = FragmentAddRecordMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        prepareRecorder()
        mInitialized = true

        fragmentAddRecordMemoBinding.run{
            addRecordMemoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.ADD_RECORD_MEMO_FRAGMENT)
                }
            }
            recordBtn.setOnClickListener {
                if (isRecording) {
                    mediaRecorder.stop()
                    recordChronometer.stop()
                    recordBtn.setBackgroundResource(R.drawable.audio_start_btn)
                    isRecording = false
                    confirmAndSave()
                } else {
                    mediaRecorder.start()
                    recordChronometer.base = SystemClock.elapsedRealtime()
                    recordChronometer.start()
                    recordBtn.setBackgroundResource(R.drawable.audio_stop_btn)
                    isRecording = true
                }
            }
            resetRecordBtn.setOnClickListener {
                resetRecorder(true)
                mStateViewSwitcher.displayedChild = RECORD_VIEW;
            }
            audioPlayBtn.setOnClickListener {
                playAudio()
            }
        }
        return fragmentAddRecordMemoBinding.root
    }

    private fun confirmAndSave() {
        val builder = AlertDialog.Builder(activity)
        val recordingNameEditor = EditText(activity)
        recordingNameEditor.setText(mGeneratedName)
        recordingNameEditor.selectAll()
        builder.setView(recordingNameEditor)

        builder.setTitle("저장")
        builder.setNeutralButton("확인"){ dialog: DialogInterface, i: Int ->
            val name = recordingNameEditor.text.toString()
            fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = PREVIEW_VIEW;
            if (name.isNotEmpty()) {
                // Set the recording's new location, close the dialog, and save
                val oldLocation = recordFileLocation
                setRecordingLocation(name)

                if (!oldLocation.renameTo(recordFileLocation)) {
                    Toast.makeText(activity, "저장 실패", Toast.LENGTH_LONG).show()

                    // Couldn't rename; work with the previous name
                    recordFileLocation = oldLocation
                }

                dialog.dismiss()

                saveFile = true

                val fileUri = Uri.fromFile(recordFileLocation)
                Snackbar.make(requireView(), "$name 저장 완료", Snackbar.LENGTH_LONG).show()
                val durationMediaPlayer = MediaPlayer.create(requireContext(), fileUri)
                durationMediaPlayer?.duration?.let { fragmentAddRecordMemoBinding.audioSeekBar?.max = it }
                fragmentAddRecordMemoBinding.currentDurationTimeTextView.text = "0:00"
                fragmentAddRecordMemoBinding.totalDurationTimeTextView.text = getTotalDuration(durationMediaPlayer)
                if (mainActivity.REQUEST_INTENTS.contains(activity?.intent?.action)) {
                    // Recorder was started via a request for audio; set result and finish
                    activity?.setResult(Activity.RESULT_OK, Intent().setData(fileUri))
                    activity?.finish()
                } else {
                    // todo : 저장 완료 후 동작 코드
                    // resetRecorder(true)
                }
            }
        }

        builder.setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->
            fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = RECORD_VIEW;
            resetRecorder(true)
        }
        builder.create().show()
    }

    private fun prepareRecorder() {
        saveFile = false

        fragmentAddRecordMemoBinding.recordChronometer.base = SystemClock.elapsedRealtime()

        // Make sure we're not recording music playing in the background; ask the MediaPlaybackService to pause playback
        stopAudioPlayback()

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        mGeneratedName = UUID.randomUUID().toString()
        setRecordingLocation(mGeneratedName!!)
        mediaRecorder.setOutputFile(recordFileLocation.getAbsolutePath())

        try {
            mediaRecorder.prepare()
            Log.d("testaaa", "prepare")
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            Log.d("testaaa", "IllegalStateException")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("testaaa", "IOException")
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        resetRecorder(mInitialized && isVisibleToUser)
    }

    private fun setRecordingLocation(recordingName: String) {
        recordFileLocation = File(getRecordingStorageDirectory(), "$recordingName.mp4")
    }

    fun getRecordingStorageDirectory():File {
        val dir = File(mainActivity.getExternalFilesDir(null), "recordings")
        dir.mkdirs();
        return dir;
    }
    private fun stopAudioPlayback() {
        val i = Intent("com.android.music.musicservicecommand")
        i.putExtra("command", "pause")

        activity?.sendBroadcast(i)
    }

    private fun resetRecorder(prepare: Boolean) {
        mediaRecorder.release()

        isRecording = false

        if (!saveFile) {
            deleteRecording()
        }

        if (prepare) {
            prepareRecorder()
        }
    }

    private fun deleteRecording() {
        if (recordFileLocation.exists()) {
            recordFileLocation.delete()
        }
    }

    fun playAudio() {
        if(mediaPlayer?.isPlaying != null && mediaPlayer?.isPlaying!!){
            mediaPlayer?.release()
        }
        val fileUri = Uri.fromFile(recordFileLocation)
        mediaPlayer = MediaPlayer.create(requireContext(), fileUri)
        fragmentAddRecordMemoBinding.audioSeekBar?.progress = 0
        updateSeekBar()
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            // Release the MediaPlayer after playback is completed
            releaseMediaPlayer()
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }

    override fun onStart() {
        super.onStart()
        fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = LOADING_VIEW;
        prepareRecorder();
        fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = RECORD_VIEW;
    }

    private fun updateSeekBar() {
        fragmentAddRecordMemoBinding.audioSeekBar?.progress = mediaPlayer?.currentPosition!!
        handler.postDelayed({
            updateSeekBar()
        }, 1000)
    }
}