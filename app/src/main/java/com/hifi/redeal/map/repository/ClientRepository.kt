package com.hifi.redeal.map.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.BuildConfig
import com.hifi.redeal.MainActivity
import com.hifi.redeal.map.model.KakaoMapAPI
import com.hifi.redeal.map.model.Place
import com.hifi.redeal.map.model.ResultSearchAddr
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository {
    companion object{
        fun getClientListByUser(userIdx: String, callback1: (Task<QuerySnapshot>) -> Unit) {
            val database = Firebase.firestore

            val clientDataRef = database.collection("userData").document(userIdx).collection("clientData")

            clientDataRef.get().addOnCompleteListener(callback1)

        }

        fun searchAddr(address: String,callback: (List<Place>?) -> Unit) {
            val retrofit = Retrofit.Builder()   // Retrofit 구성
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(KakaoMapAPI::class.java)   // 통신 인터페이스를 객체로 생성
            val call = api.getSearchAddr("KakaoAK "+ BuildConfig.KAKAO_REST_API_KEY, address)   // 검색 조건 입력

            // API 서버에 요청
            call.enqueue(object: Callback<ResultSearchAddr> {
                override fun onResponse(
                    call: Call<ResultSearchAddr>,
                    response: Response<ResultSearchAddr>
                ) {
                    // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                    Log.d("Test", "Raw: ${response.raw()}")
                    Log.d("Test", "Body: ${response.body()}")

                    val result = response.body()!!.documents
                    callback(result)

                }

                override fun onFailure(call: Call<ResultSearchAddr>, t: Throwable) {
                    // 통신 실패
                    Log.w("MainActivity", "통신 실패: ${t.message}")
                    callback(null)
                }
            })

        }

    }
}


