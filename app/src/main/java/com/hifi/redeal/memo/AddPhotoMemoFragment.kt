package com.hifi.redeal.memo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAddPhotoMemoBinding
import com.hifi.redeal.memo.repository.PhotoMemoRepository
import com.hifi.redeal.memo.utils.dpToPx

class AddPhotoMemoFragment : Fragment() {
    private lateinit var fragmentAddPhotoMemoBinding: FragmentAddPhotoMemoBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var albumLauncher: ActivityResultLauncher<Intent>
    private var uriList = mutableListOf<Uri>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAddPhotoMemoBinding = FragmentAddPhotoMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        albumLauncher = albumSetting()
        fragmentAddPhotoMemoBinding.run {
            addPhotoMemoToolbar.run {
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.ADD_PHOTO_MEMO_FRAGMENT)
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.albumBtn -> {
                            clickAlbumLaunch(albumLauncher)
                        }
                    }
                    true
                }
            }
            addPhotoMemoBtn.setOnClickListener{
                val photoMemoContext = photoMemoTextInputEditText.text.toString()
                addPhotoMemoBtn.isEnabled = false
                addPhotoMemoBtn.setBackgroundResource(R.drawable.add_button_loading_container)
                addPhotoMemoBtn.text = "등록 중 ..."
                addPhotoMemoBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary20))
                PhotoMemoRepository.addPhotoMemo(1,1,photoMemoContext,uriList){
                    mainActivity.removeFragment(MainActivity.ADD_PHOTO_MEMO_FRAGMENT)
                }
            }
        }
        return fragmentAddPhotoMemoBinding.root
    }

    private fun resumeImageListView(){
        if(fragmentAddPhotoMemoBinding.addImageListLayout.visibility != View.VISIBLE){
            fragmentAddPhotoMemoBinding.addImageListLayout.visibility = View.VISIBLE
            fragmentAddPhotoMemoBinding.emptyPhotoTextView.visibility = View.GONE
        }

        fragmentAddPhotoMemoBinding.addImageListLayout.removeAllViews()

        var linearLayoutHorizontal = LinearLayout(requireContext())
        linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0,0,0,dpToPx(requireContext(), 6))
        linearLayoutHorizontal.layoutParams = layoutParams
        var imgCnt = 0
        uriList.forEach{
            if(imgCnt != 0 && imgCnt % 3 == 0) {
                fragmentAddPhotoMemoBinding.addImageListLayout.addView(linearLayoutHorizontal)
                val newLinearLayoutHorizontal = LinearLayout(requireContext())
                linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0,0,0,dpToPx(requireContext(), 6))
                linearLayoutHorizontal.layoutParams = layoutParams
                linearLayoutHorizontal = newLinearLayoutHorizontal
            }
            val imageViewLayoutParams = ViewGroup.MarginLayoutParams(
                dpToPx(requireContext(), 100),
                dpToPx(requireContext(), 100)
            )
            if(imgCnt%3 != 0) {
                imageViewLayoutParams.marginStart = dpToPx(requireContext(), 6)
            }
            val imageView = ImageView(requireContext())
            imageView.layoutParams = imageViewLayoutParams
            linearLayoutHorizontal.addView(imageView)
            Glide.with(imageView)
                .load(it.toString())
                .into(imageView)
            imgCnt++
        }
        fragmentAddPhotoMemoBinding.addImageListLayout.addView(linearLayoutHorizontal)
        val params = fragmentAddPhotoMemoBinding.photoMemoTextInputLayout.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = R.id.addImageListLayout
    }
    private fun albumSetting(): ActivityResultLauncher<Intent> {
        val albumLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val selectedImages = mutableListOf<Uri>()
                    if (result.data?.clipData != null) {
                        val clipData = result.data?.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            selectedImages.add(clipData.getItemAt(i).uri)
                        }
                    } else if (result.data?.data != null) {
                        selectedImages.add(result.data?.data!!)
                    }
                    uriList = selectedImages
                    resumeImageListView()
                }
            }
        return albumLauncher
    }

    private fun clickAlbumLaunch(albumLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        albumLauncher.launch(intent)
    }
}