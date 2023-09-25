package com.hifi.redeal.account.repository

import com.hifi.redeal.account.repository.model.FullAddrResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TMapTapiService {

    @GET("tmap/geo/fullAddrGeo")
    fun tMapFullTextGeocoding(
        @Query("addressFlag") addressFlag: String = "F02",
        @Query("version") version: String = "1",
        @Query("fullAddr") fullAddr: String,
        @Query("page") page: String = "1",
        @Query("count") count: String = "1",
        @Query("appKey") appKey: String,
    ): Call<FullAddrResponse>
}