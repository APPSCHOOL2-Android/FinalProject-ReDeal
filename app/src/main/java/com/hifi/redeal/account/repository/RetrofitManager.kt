package com.hifi.redeal.account.repository

import com.hifi.redeal.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://apis.openapi.sk.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val token = BuildConfig.TMAP_APP_KEY

    val tMapTapiService: TMapTapiService by lazy { retrofit.create(TMapTapiService::class.java) }
}