package com.hifi.redeal.memo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentPhotoMemoBinding
import com.hifi.redeal.databinding.RowPhotoMemoBinding
import com.hifi.redeal.memo.model.PhotoMemoData
import com.hifi.redeal.memo.repository.PhotoMemoRepository
import com.hifi.redeal.memo.utils.dpToPx
import com.hifi.redeal.memo.utils.intervalBetweenDateText
import com.hifi.redeal.memo.vm.PhotoMemoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PhotoMemoFragment : Fragment() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private lateinit var photoMemoViewModel: PhotoMemoViewModel
    private lateinit var fragmentPhotoMemoBinding: FragmentPhotoMemoBinding
    private lateinit var mainActivity: MainActivity
    private val userIdx = Firebase.auth.uid!!
    private var clientIdx = 1L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentPhotoMemoBinding = FragmentPhotoMemoBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        clientIdx = arguments?.getLong("clientIdx")?:1L
        photoMemoViewModel = ViewModelProvider(this)[PhotoMemoViewModel::class.java]

        photoMemoViewModel.run{
            photoMemoList.observe(viewLifecycleOwner){
                fragmentPhotoMemoBinding.photoMemoRecyclerView.adapter?.notifyDataSetChanged()
            }
        }

        fragmentPhotoMemoBinding.run{
            photoMemoViewModel.getPhotoMemoList(userIdx, clientIdx)
            photoMemoToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.PHOTO_MEMO_FRAGMENT)
                }
            }
            photoMemoAddBtn.setOnClickListener {
                val newBundle = Bundle()
                newBundle.putLong("clientIdx", clientIdx)
                mainActivity.replaceFragment(MainActivity.ADD_PHOTO_MEMO_FRAGMENT, true, newBundle)
            }
            photoMemoRecyclerView.run{
                adapter = PhotoMemoRecyclerAdapter()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL))
            }
        }
        return fragmentPhotoMemoBinding.root
    }

    inner class PhotoMemoRecyclerAdapter:RecyclerView.Adapter<PhotoMemoRecyclerAdapter.PhotoMemoViewHolder>(){
        inner class PhotoMemoViewHolder(rowPhotoMemoBinding: RowPhotoMemoBinding):RecyclerView.ViewHolder(rowPhotoMemoBinding.root){
            private val imageListLayout = rowPhotoMemoBinding.imageListLayout
            private val photoDateTextView = rowPhotoMemoBinding.photoDateTextView
            private val photoMemoTextView = rowPhotoMemoBinding.photoMemoTextView
            fun bindItem(item:PhotoMemoData){
                photoDateTextView.text = intervalBetweenDateText(dateFormat.format(item.date.toDate()))
                photoMemoTextView.text = item.context.ifEmpty { "메모를 등록하지 않았어요" }
                var linearLayoutHorizontal = LinearLayout(requireContext())
                linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0,0,0,dpToPx(requireContext(), 6))
                linearLayoutHorizontal.layoutParams = layoutParams
                imageListLayout.removeAllViews()
                for(i in 0 until item.srcArr.size){
                    if(i != 0 && i % 3 == 0) {
                        imageListLayout.addView(linearLayoutHorizontal)
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
                    PhotoMemoRepository.getPhotoMemoImgUrl(userIdx, item.srcArr[i]){url ->
                        Glide.with(imageView)
                            .load(url)
                            .into(imageView)
                    }
                }
                imageListLayout.addView(linearLayoutHorizontal)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoMemoViewHolder {
            val rowPhotoMemoBinding = RowPhotoMemoBinding.inflate(layoutInflater)
            val photoMemoViewHolder = PhotoMemoViewHolder(rowPhotoMemoBinding)

            rowPhotoMemoBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return photoMemoViewHolder
        }

        override fun getItemCount(): Int {
            return photoMemoViewModel.photoMemoList.value?.size!!
        }

        override fun onBindViewHolder(holder: PhotoMemoViewHolder, position: Int) {
            val item = photoMemoViewModel.photoMemoList.value?.get(position)!!
            holder.bindItem(item)
        }
    }
}