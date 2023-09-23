package com.hifi.redeal.map.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.BuildConfig
import com.hifi.redeal.MainActivity
import com.hifi.redeal.account.repository.RetrofitManager
import com.hifi.redeal.account.repository.model.Coordinate
import com.hifi.redeal.account.repository.model.FullAddrResponse
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.model.KakaoMapAPI
import com.hifi.redeal.map.model.Place
import com.hifi.redeal.map.model.RegionInfoAPI
import com.hifi.redeal.map.model.ResultSearchAddr
import com.hifi.redeal.map.model.ResultSearchRegion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder

class MapRepository {
    companion object {

        fun searchAddr(address: String, callback: (List<Place>?) -> Unit) {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(KakaoMapAPI::class.java)
            val call = api.getSearchAddr(
                "KakaoAK " + BuildConfig.KAKAO_REST_API_KEY,
                address
            )

            call.enqueue(object : Callback<ResultSearchAddr> {
                override fun onResponse(
                    call: Call<ResultSearchAddr>,
                    response: Response<ResultSearchAddr>
                ) {
                    val result = response.body()!!.documents
                    callback(result)

                }

                override fun onFailure(call: Call<ResultSearchAddr>, t: Throwable) {
                    Log.w("Kakao API", "통신 실패: ${t.message}")
                    callback(null)
                }
            })

        }

        fun searchSiDo(callback: (MutableList<AdmVO>?) -> Unit) {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainActivity.REGION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(RegionInfoAPI::class.java)
            val call = api.getSiDo(
                BuildConfig.REGION_REST_API_KEY,
                "json"
            )


            call.enqueue(object : Callback<ResultSearchRegion> {
                override fun onResponse(
                    call: Call<ResultSearchRegion>,
                    response: Response<ResultSearchRegion>
                ) {
                    val admVo1 = AdmVO("경상남도", "48", "경상남도")
                    val admVo2 = AdmVO("경상남도", "47", "경상북도")
                    val admVo3 = AdmVO("전라남도", "46", "전라남도")
                    val admVo4 = AdmVO("전라남도", "45", "전라북도")
                    val admVo5 = AdmVO("충청남도", "44", "충청남도")
                    val admVo6 = AdmVO("충청남도", "42", "강원도")
                    val admVo7 = AdmVO("충청남도", "50", "제주특별자치도")
                    val admList = mutableListOf(admVo1, admVo2, admVo3, admVo4, admVo5, admVo6, admVo7)

                    val temp = response.body()?.admVOList?.admVOList as MutableList
                    admList.addAll(temp)
                    callback(admList)

                }


                override fun onFailure(call: Call<ResultSearchRegion>, t: Throwable) {
                    // 통신 실패
                    Log.w("Region API", "통신 실패: ${t.message}")
                    callback(null)
                }
            })

        }

        fun searchSiGunGu(admCode: Int, callback: (List<AdmVO>?) -> Unit) {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainActivity.REGION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(RegionInfoAPI::class.java)
            val call = api.getSiGunGu(
                BuildConfig.REGION_REST_API_KEY,
                admCode,
                "json"
            )


            call.enqueue(object : Callback<ResultSearchRegion> {
                override fun onResponse(
                    call: Call<ResultSearchRegion>,
                    response: Response<ResultSearchRegion>
                ) {
                    val result = response.body()?.admVOList?.admVOList
                    callback(result)

                }


                override fun onFailure(call: Call<ResultSearchRegion>, t: Throwable) {
                    Log.w("Region API", "통신 실패: ${t.message}")
                    callback(null)
                }
            })

        }
        fun searchDong(admCode: Int, callback: (List<AdmVO>?) -> Unit) {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainActivity.REGION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(RegionInfoAPI::class.java)
            val call = api.getDong(
                BuildConfig.REGION_REST_API_KEY,
                admCode,
                "json"
            )


            call.enqueue(object : Callback<ResultSearchRegion> {
                override fun onResponse(
                    call: Call<ResultSearchRegion>,
                    response: Response<ResultSearchRegion>
                ) {
                    val result = response.body()?.admVOList?.admVOList
                    callback(result)

                }


                override fun onFailure(call: Call<ResultSearchRegion>, t: Throwable) {
                    Log.w("Region API", "통신 실패: ${t.message}")
                    callback(null)
                }
            })

        }


        fun getFullAddrGeocoding(fullAddr: String, callback: (Coordinate?) -> Unit) {
            val encodedFullAddr = URLEncoder.encode(fullAddr, "UTF-8")

            RetrofitManager.tMapTapiService.tMapFullTextGeocoding(fullAddr = encodedFullAddr, appKey = BuildConfig.TMAP_APP_KEY)
                .enqueue(object : Callback<FullAddrResponse> {
                    override fun onResponse(
                        call: Call<FullAddrResponse>,
                        response: Response<FullAddrResponse>
                    ) {
                        if (response.isSuccessful) {
                            callback(response.body()?.coordinateInfo?.coordinate?.get(0))
                        }
                    }

                    override fun onFailure(call: Call<FullAddrResponse>, t: Throwable) {
                        Log.d("TMap API", "네트워크 통신 실패")
                    }
                })
        }


    }




}





