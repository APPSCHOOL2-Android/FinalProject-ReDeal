package com.hifi.redeal.memo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAddRecordMemoBinding
import com.hifi.redeal.memo.repository.PhotoMemoRepository
import com.hifi.redeal.memo.repository.RecordMemoRepository
import com.hifi.redeal.memo.utils.getCurrentDuration
import com.hifi.redeal.memo.utils.getTotalDuration
import java.io.File
import java.io.IOException


class AddRecordMemoFragment : Fragment() {
    private lateinit var fragmentAddRecordMemoBinding : FragmentAddRecordMemoBinding
    private lateinit var mainActivity: MainActivity

    private val RECORD_VIEW = 0
    private val PREVIEW_VIEW = 1

    private lateinit var recordFileLocation: File
    private lateinit var mediaRecorder: MediaRecorder

    private var audioFileName: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording: Boolean = false
    private var isSaveFile = false
    private var isAudioPlaying = false

    private val handler = Handler()
    private var audioFileUri: Uri? = null
    private var clientIdx = 1L

    private lateinit var audioLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAddRecordMemoBinding = FragmentAddRecordMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        clientIdx = arguments?.getLong("clientIdx")?:1L
        audioLauncher = recordingSetting()
        prepareRecorder()
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
            addRecordFileBtn.setOnClickListener {
                clickRecordAudio(audioLauncher)
            }
            addRecordMemoAddBtn.setOnClickListener{
                val recordMemoContext = addRecordMemoTextInputEditText.text.toString()
                addRecordMemoAddBtn.isEnabled = false
                addRecordMemoAddBtn.setBackgroundResource(R.drawable.add_button_loading_container)
                addRecordMemoAddBtn.text = "등록 중 ..."
                addRecordMemoAddBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary20))
                RecordMemoRepository.addRecordMemo(1,clientIdx,recordMemoContext,audioFileUri!!, audioFileName!!){
                    mainActivity.removeFragment(MainActivity.ADD_RECORD_MEMO_FRAGMENT)
                }
            }
        }
        return fragmentAddRecordMemoBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        resetRecorder(true)
        audioFileUri = null
    }

    override fun onStart() {
        super.onStart()
        fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = RECORD_VIEW;
    }

    private fun prepareRecorder() {
        isSaveFile = false
        audioFileUri = null
        stopAudioPlayback()
        fragmentAddRecordMemoBinding.recordChronometer.base = SystemClock.elapsedRealtime()
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        audioFileName = "record_${System.currentTimeMillis()}"
        setRecordingLocation(audioFileName!!)
        mediaRecorder.setOutputFile(recordFileLocation.getAbsolutePath())

        try {
            mediaRecorder.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun confirmAndSave() {
        val builder = AlertDialog.Builder(activity)
        val recordingNameEditor = EditText(activity)
        recordingNameEditor.setText(audioFileName)
        recordingNameEditor.setPadding(40)
        recordingNameEditor.selectAll()
        builder.setView(recordingNameEditor)

        builder.setTitle("파일 이름 입력")
        builder.setNeutralButton("확인"){ dialog: DialogInterface, _: Int ->
            val tempName = recordingNameEditor.text.toString()
            fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = PREVIEW_VIEW;
            if (tempName.isNotEmpty()) {
                val oldLocation = recordFileLocation
                setRecordingLocation(tempName)

                if (!oldLocation.renameTo(recordFileLocation)) {
                    Toast.makeText(activity, "중복된 이름이 있습니다.", Toast.LENGTH_LONG).show()
                    recordFileLocation = oldLocation
                }

                isSaveFile = true

                audioFileName = tempName

                audioFileUri = Uri.fromFile(recordFileLocation)
                val durationMediaPlayer = MediaPlayer.create(requireContext(), audioFileUri)
                fragmentAddRecordMemoBinding.audioSeekBar.max = durationMediaPlayer?.duration!!
                fragmentAddRecordMemoBinding.currentDurationTimeTextView.text = getCurrentDuration(0)
                fragmentAddRecordMemoBinding.audioSeekBar.progress = 0
                fragmentAddRecordMemoBinding.totalDurationTimeTextView.text = getTotalDuration(durationMediaPlayer)
                Snackbar.make(requireView(), "$audioFileName 저장 완료", Snackbar.LENGTH_LONG).show()
                fragmentAddRecordMemoBinding.addRecordFileTextView.text = "$audioFileName.mp4"
                dialog.dismiss()
            }
        }

        builder.setNegativeButton("취소"){ dialogInterface: DialogInterface, i: Int ->
            fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = RECORD_VIEW
            resetRecorder(true)
        }
        builder.create().show()
    }
    private fun setRecordingLocation(recordingName: String) {
        recordFileLocation = File("${mainActivity.getExternalFilesDir(null)}/recordings", "$recordingName.mp4")
    }

    private fun stopAudioPlayback() {
        val i = Intent("com.android.music.musicservicecommand")
        i.putExtra("command", "pause")

        activity?.sendBroadcast(i)
    }

    private fun resetRecorder(prepare: Boolean) {
        mediaRecorder.release()
        releaseMediaPlayer()
        isRecording = false
        fragmentAddRecordMemoBinding.addRecordFileTextView.text = "새로 파일을 등록하거나 녹음 하실 수 있어요"

        if (!isSaveFile) {
            if (recordFileLocation.exists()) {
                recordFileLocation.delete()
            }
        }

        if (prepare) {
            prepareRecorder()
        }
    }


    private fun playAudio() {
        if(isAudioPlaying){
            releaseMediaPlayer()
        }
        isAudioPlaying = true
        mediaPlayer = MediaPlayer.create(requireContext(), audioFileUri)
        mediaPlayer?.start()
        updateSeekBar()
        mediaPlayer?.setOnCompletionListener {
            releaseMediaPlayer()
        }
    }

    private fun releaseMediaPlayer() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        isAudioPlaying = false
    }

    private fun updateSeekBar() {
        if (isAudioPlaying) {
            val currentPosition = mediaPlayer?.currentPosition!!
            fragmentAddRecordMemoBinding.audioSeekBar.progress = currentPosition
            fragmentAddRecordMemoBinding.currentDurationTimeTextView.text = getCurrentDuration(currentPosition)
            handler.postDelayed({ updateSeekBar() }, 20)
        }
    }

    private fun recordingSetting(): ActivityResultLauncher<Intent> {
        val audioContract = ActivityResultContracts.StartActivityForResult()
        val audioLauncher = registerForActivityResult(audioContract){
            if(it.resultCode == AppCompatActivity.RESULT_OK){
                if(it.data?.data != null){
                    audioFileUri = it.data?.data
                    val durationMediaPlayer = MediaPlayer.create(requireContext(), audioFileUri)
                    fragmentAddRecordMemoBinding.audioSeekBar.max = durationMediaPlayer?.duration!!
                    fragmentAddRecordMemoBinding.currentDurationTimeTextView.text = getCurrentDuration(0)
                    fragmentAddRecordMemoBinding.audioSeekBar.progress = 0
                    fragmentAddRecordMemoBinding.totalDurationTimeTextView.text = getTotalDuration(durationMediaPlayer)
                    audioFileName = getFileNameFromUri(it.data?.data!!)?:"이름 불러오기 실패"
                    fragmentAddRecordMemoBinding.addRecordFileTextView.text = audioFileName
                    fragmentAddRecordMemoBinding.mStateViewSwitcher.displayedChild = PREVIEW_VIEW
                }
            }
        }

        return audioLauncher
    }

    private fun clickRecordAudio(recordingLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/*"
        recordingLauncher.launch(intent)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val cursor = requireActivity().contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(filePathColumn[0])
                val fileName = it.getString(columnIndex)
                cursor.close()
                return fileName
            }
            cursor.close()
        }
        return null
    }
}