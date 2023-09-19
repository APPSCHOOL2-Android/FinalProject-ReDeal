package com.hifi.redeal.account

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.model.ClientData
import com.hifi.redeal.account.repository.AccountDetailRepository
import com.hifi.redeal.databinding.FragmentAccountDetailBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import java.text.SimpleDateFormat

class AccountDetailFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var fragmentAccountDetailBinding: FragmentAccountDetailBinding

    val accountDetailRepository = AccountDetailRepository()

    var clientIdx = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAccountDetailBinding = FragmentAccountDetailBinding.inflate(layoutInflater)

        if (clientIdx == 0L)
            clientIdx = arguments?.getLong("clientIdx") ?: 0

        accountDetailRepository.getClient(1, clientIdx) { client ->
            if (client != null) {
                accountDetailViewInit(client)
            }
        }

        fragmentAccountDetailBinding.run {
//            mapViewAccountDetail.start(object : KakaoMapReadyCallback() {
//                override fun onMapReady(kakaoMap: KakaoMap) {
//                    Toast.makeText(mainActivity, "맵 로딩 성공", Toast.LENGTH_SHORT).show()
//                }
//            })
            bottomNavigationViewAccountDetail.setupWithNavController(findNavController())
        }

        return fragmentAccountDetailBinding.root
    }

    fun accountDetailViewInit(client: ClientData) {
        fragmentAccountDetailBinding.run {
            materialToolbarAccountDetail.run {
                title = client.clientName

                setNavigationOnClickListener {
                    findNavController().popBackStack()
                }

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.accountDetailMenuItemShare -> {

                        }
                        R.id.accountDetailMenuItemEdit -> {
                            val bundle = Bundle()
                            bundle.putLong("clientIdx", clientIdx)
                            mainActivity.navigateTo(R.id.accountEditFragment, bundle)
                        }
                        R.id.accountDetailMenuItemDelete -> {
                            AlertDialog.Builder(mainActivity)
                                .setTitle("거래처 삭제")
                                .setMessage("거래처 정보를 삭제하시겠습니까?")
                                .setPositiveButton("확인") { _, _ ->
                                    accountDetailRepository.deleteClient(1, clientIdx) {
                                        Snackbar.make(root, "거래처가 삭제되었습니다", Snackbar.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                                }
                                .setNegativeButton("취소", null)
                                .show()
                        }
                    }
                    true
                }
            }

            val stateImgResourceList = arrayOf(
                R.drawable.circle_big_24px_primary20,
                R.drawable.circle_big_24px_primary50,
                R.drawable.circle_big_24px_primary80
            )

            val stateTextList = arrayOf(
                "거래 중", "거래 시도", "거래 중지"
            )

            val state = client.clientState?.toInt() ?: 1
            imageViewAccountDetailState.setImageResource(stateImgResourceList[state - 1])

            textViewAccountDetailState.text = stateTextList[state - 1]

            if (client.recentContactDate != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val contactTime = dateFormat.format(client.recentContactDate!!.toDate())

                textViewAccountDetailRecentContact.text = "${mainActivity.intervalBetweenDateText(contactTime)}"
            }

            if (client.recentVisitDate != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val visitTime = dateFormat.format(client.recentVisitDate!!.toDate())

                textViewAccountDetailRecentVisit.text = "${mainActivity.intervalBetweenDateText(visitTime)}"
            }

            if (client.isBookmark == true) {
                imageViewAccountDetailFavorite.setImageResource(R.drawable.star_fill_24px)
            } else {
                imageViewAccountDetailFavorite.setImageResource(R.drawable.star_nofill_24px)
            }

            imageViewAccountDetailFavorite.setOnClickListener {
                if (client.isBookmark == true) {
                    imageViewAccountDetailFavorite.setImageResource(R.drawable.star_nofill_24px)
                    client.isBookmark = false
                } else {
                    imageViewAccountDetailFavorite.setImageResource(R.drawable.star_fill_24px)
                    client.isBookmark = true
                }
                accountDetailRepository.updateBookmark(1, clientIdx, client.isBookmark ?: false)
            }

            textViewAccountDetailShortDescription.text = client.clientExplain
            textViewAccountDetailGeneralNumber.append(client.clientCeoPhone)
            textViewAccountDetailFaxNumber.append(client.clientFaxNumber)
            textViewAccountDetailAddress.text = "${client.clientAddress} ${client.clientDetailAdd}"
            textViewAccountDetailRepresentative.text = client.clientManagerName
            textViewAccountDetailDirectNumber.text = client.clientManagerPhone
            textViewAccountDetailMemoContent.text = client.clientMemo
        }
    }
}