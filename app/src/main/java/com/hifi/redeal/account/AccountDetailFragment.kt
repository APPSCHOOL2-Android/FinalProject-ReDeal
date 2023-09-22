package com.hifi.redeal.account

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.repository.AccountDetailRepository
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.account.repository.model.Coordinate
import com.hifi.redeal.databinding.DialogAccountShareBinding
import com.hifi.redeal.databinding.FragmentAccountDetailBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.mapwidget.InfoWindowOptions
import com.kakao.vectormap.mapwidget.component.GuiImage
import com.kakao.vectormap.mapwidget.component.GuiLayout
import com.kakao.vectormap.mapwidget.component.GuiText
import com.kakao.vectormap.mapwidget.component.Orientation
import java.lang.NullPointerException
import java.text.SimpleDateFormat


class AccountDetailFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var fragmentAccountDetailBinding: FragmentAccountDetailBinding

    lateinit var kakaoMap: KakaoMap

    val accountDetailRepository = AccountDetailRepository()

    var clientIdx = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAccountDetailBinding = FragmentAccountDetailBinding.inflate(layoutInflater)

        mainActivity.activityMainBinding.bottomNavigationViewMain.visibility = View.GONE

        if (clientIdx == 0L)
            clientIdx = arguments?.getLong("clientIdx") ?: 0

        accountDetailRepository.getClient(mainActivity.uid, clientIdx) { client ->
            fragmentAccountDetailBinding.mapViewAccountDetail.start(object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    kakaoMap = map
                    if (client != null) {
                        accountDetailRepository.getFullAddrGeocoding(client.clientAddress ?: "") {
                            if (it != null) {
                                mapInit(it, client.clientName ?: "")
                            }
                        }
                    }
                }
            })

            if (client != null) {
                accountDetailViewInit(client)
            }
        }

        fragmentAccountDetailBinding.run {
//            bottomNavigationViewAccountDetail.setupWithNavController(findNavController())
            bottomNavigationViewAccountDetail.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.transactionFragment -> {
                        val bundle = Bundle()
                        bundle.putLong("clientIdx", clientIdx)
                        mainActivity.replaceFragment(
                            MainActivity.TRANSACTION_FRAGMENT,
                            true,
                            bundle
                        )
                    }

                    R.id.recordMemoFragment -> {
                        val bundle = Bundle()
                        bundle.putLong("clientIdx", clientIdx)
                        mainActivity.replaceFragment(
                            MainActivity.RECORD_MEMO_FRAGMENT,
                            true,
                            bundle
                        )
                    }

                    R.id.photoMemoFragment -> {
                        val bundle = Bundle()
                        bundle.putLong("clientIdx", clientIdx)
                        mainActivity.replaceFragment(MainActivity.PHOTO_MEMO_FRAGMENT, true, bundle)
                    }
                }
                true
            }
        }

        return fragmentAccountDetailBinding.root
    }

    fun mapInit(coordinate: Coordinate, clientName: String) {
        fragmentAccountDetailBinding.run {
            val latLng = LatLng.from(coordinate.newLat.toDouble(), coordinate.newLon.toDouble())

            val cameraUpdate = CameraUpdateFactory.newCenterPosition(latLng, 16)
            kakaoMap.moveCamera(cameraUpdate)

//            val labelOptions = LabelOptions.from(latLng).setStyles(R.drawable.location_on_primary10)
//            val labelLayer = kakaoMap.labelManager?.layer
//            labelLayer?.addLabel(labelOptions)

            val body = GuiLayout(Orientation.Horizontal)
            body.setPadding(0, -20, 0, -20)

            val bgImage = GuiImage(R.drawable.window_info_bg, true)
            bgImage.setFixedArea(39, 39, 39, 39)
            body.setBackground(bgImage)

            val text = GuiText(clientName)
            text.textSize = 30
            text.textColor = Color.DKGRAY
            body.addView(text)

            val options = InfoWindowOptions.from(latLng)
            options.setBody(body)
            options.setBodyOffset(0f, -41f)
            val infoWindowOptions = options.setTail(GuiImage(R.drawable.window_info_tail, false))

            kakaoMap.mapWidgetManager?.infoWindowLayer?.addInfoWindow(infoWindowOptions)

            buttonAccountDetailDirectionsCar.setOnClickListener {
                AlertDialog.Builder(mainActivity)
                    .setTitle("길 안내")
                    .setMessage("티맵 길 안내를 시작하시겠습니까?")
                    .setPositiveButton("확인") { _, _ ->
                        if (mainActivity.tMapTapi.isTmapApplicationInstalled) {
                            mainActivity.tMapTapi.invokeRoute(coordinate.newBuildingName, coordinate.newLon.toFloat(), coordinate.newLat.toFloat())
                        } else {
                            Snackbar.make(fragmentAccountDetailBinding.root, "티맵 앱 설치 후 다시 시도해주세요", Snackbar.LENGTH_SHORT)
                                .setAction("설치하기") {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse("market://details?id=com.skt.tmap.ku")
                                    startActivity(intent)
                                }.apply {
                                    anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                                }
                                .show()
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    fun accountDetailViewInit(client: ClientData) {
        fragmentAccountDetailBinding.run {
            materialToolbarAccountDetail.run {
                title = client.clientName

                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT)
//                    findNavController().popBackStack()
                }

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.accountDetailMenuItemShare -> {
                            val dialogAccountShareBinding = DialogAccountShareBinding.inflate(layoutInflater)

                            AlertDialog.Builder(mainActivity)
                                .setTitle("거래처 공유")
                                .setView(dialogAccountShareBinding.root)
                                .setPositiveButton("공유하기") { _, _ ->
                                    accountDetailRepository.sendShareNotification(
                                        mainActivity.uid,
                                        clientIdx,
                                        dialogAccountShareBinding.editTextDialogAccountShareEmail.text.toString()
                                    ) { isSuccessful ->
                                        if (isSuccessful) {
                                            Snackbar.make(fragmentAccountDetailBinding.root, "거래처 정보가 공유되었습니다", Snackbar.LENGTH_SHORT)
                                                .apply {
                                                    anchorView = fragmentAccountDetailBinding.bottomNavigationViewAccountDetail
                                                }
                                                .show()
                                        } else {
                                            Snackbar.make(fragmentAccountDetailBinding.root, "거래처 정보 공유에 실패했습니다", Snackbar.LENGTH_SHORT)
                                                .apply {
                                                    anchorView = fragmentAccountDetailBinding.bottomNavigationViewAccountDetail
                                                }
                                                .show()
                                        }
                                    }
                                }
                                .setNegativeButton("취소", null)
                                .show()
                        }
                        R.id.accountDetailMenuItemEdit -> {
                            val bundle = Bundle()
                            bundle.putLong("clientIdx", clientIdx)
                            mainActivity.replaceFragment(MainActivity.ACCOUNT_EDIT_FRAGMENT, true, bundle)
//                            mainActivity.navigateTo(R.id.accountEditFragment, bundle)
                        }
                        R.id.accountDetailMenuItemDelete -> {
                            AlertDialog.Builder(mainActivity)
                                .setTitle("거래처 삭제")
                                .setMessage("거래처 정보를 삭제하시겠습니까?")
                                .setPositiveButton("확인") { _, _ ->
                                    accountDetailRepository.deleteClient(mainActivity.uid, clientIdx) {
                                        Snackbar.make(root, "거래처가 삭제되었습니다", Snackbar.LENGTH_SHORT).apply {
                                            anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                                        }.show()
                                        mainActivity.removeFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT)
//                                        findNavController().popBackStack()
                                    }
                                }
                                .setNegativeButton("취소", null)
                                .show()
                        }
                    }
                    true
                }
            }

            buttonAccountDetailGeneralCall.setOnClickListener {
                if (client.clientCeoPhone?.isNotEmpty() == true) {
                    AlertDialog.Builder(mainActivity)
                        .setTitle("대표 전화")
                        .setMessage("대표 전화로 통화하시겠습니까?")
                        .setPositiveButton("확인") { _, _ ->
                            startActivity(
                                Intent(
                                    "android.intent.action.CALL",
                                    Uri.parse("tel:${client.clientCeoPhone}")
                                )
                            )
                        }
                        .setNegativeButton("취소", null)
                        .show()
                } else {
                    Snackbar.make(root, "등록된 대표 전화가 없습니다", Snackbar.LENGTH_SHORT).apply {
                        anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                    }.show()
                }
            }

            buttonAccountDetailDirectCall.setOnClickListener {
                if (client.clientManagerPhone?.isNotEmpty() == true) {
                    AlertDialog.Builder(mainActivity)
                        .setTitle("담당자 연락처 통화")
                        .setMessage("담당자 연락처로 통화하시겠습니까?")
                        .setPositiveButton("확인") { _, _ ->
                            startActivity(
                                Intent(
                                    "android.intent.action.CALL",
                                    Uri.parse("tel:${client.clientManagerPhone}")
                                )
                            )
                        }
                        .setNegativeButton("취소", null)
                        .show()
                } else {
                    Snackbar.make(root, "등록된 연락처가 없습니다", Snackbar.LENGTH_SHORT).apply {
                        anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                    }.show()
                }
            }

            buttonAccountDetailSendMessage.setOnClickListener {
                if (client.clientManagerPhone?.isNotEmpty() == true) {
                    AlertDialog.Builder(mainActivity)
                        .setTitle("담당자 연락처 메시지")
                        .setMessage("담당자 연락처로 전송할 메시지를 작성하시겠습니까?")
                        .setPositiveButton("확인") { _, _ ->
                            startActivity(
                                Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.parse("smsto:${client.clientManagerPhone}")
                                )
                            )
                        }
                        .setNegativeButton("취소", null)
                        .show()
                } else {
                    Snackbar.make(root, "등록된 연락처가 없습니다", Snackbar.LENGTH_SHORT).apply {
                        anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                    }.show()
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
                accountDetailRepository.updateBookmark(mainActivity.uid, clientIdx, client.isBookmark ?: false)
            }

            var ceoPhoneNumber = PhoneNumberUtils.formatNumber(client.clientCeoPhone, "KR")
            if (ceoPhoneNumber == null)
                ceoPhoneNumber = client.clientCeoPhone

            var managerPhoneNumber = PhoneNumberUtils.formatNumber(client.clientManagerPhone, "KR")
            if (managerPhoneNumber == null)
                managerPhoneNumber = client.clientManagerPhone

            var faxNumber = PhoneNumberUtils.formatNumber(client.clientFaxNumber, "KR")
            if (faxNumber == null)
                faxNumber = client.clientFaxNumber

            textViewAccountDetailShortDescription.text = client.clientExplain
            textViewAccountDetailGeneralNumber.append(ceoPhoneNumber)
            textViewAccountDetailFaxNumber.append(faxNumber)
            textViewAccountDetailAddress.text = "${client.clientAddress} ${client.clientDetailAdd}"
            textViewAccountDetailRepresentative.text = client.clientManagerName
            textViewAccountDetailDirectNumber.text = managerPhoneNumber
            textViewAccountDetailMemoContent.text = client.clientMemo
        }
    }
}