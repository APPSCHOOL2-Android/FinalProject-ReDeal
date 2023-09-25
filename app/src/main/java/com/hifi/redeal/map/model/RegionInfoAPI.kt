package com.hifi.redeal.map.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
 
interface RegionInfoAPI {
    @GET("/ned/data/admCodeList")
    fun getSiDo(
        @Query("key") key: String,
        @Query("format") format: String
    ): Call<ResultSearchRegion>

    @GET("/ned/data/admSiList")
    fun getSiGunGu(
        @Query("key") key: String,
        @Query("admCode") admCode: Int,
        @Query("format") format: String
    ): Call<ResultSearchRegion>

    @GET("/ned/data/admDongList")
    fun getDong(
        @Query("key") key: String,
        @Query("admCode") admCode: Int,
        @Query("format") format: String
    ): Call<ResultSearchRegion>

    @GET("/ned/data/admReeList")
    fun getRee(
        @Query("key") key: String,
        @Query("admCode") admCode: Int,
        @Query("format") format: String
    ): Call<ResultSearchRegion>
}