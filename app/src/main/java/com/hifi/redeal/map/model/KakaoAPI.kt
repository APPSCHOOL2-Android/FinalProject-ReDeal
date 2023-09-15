package com.hifi.redeal.map.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
 
interface KakaoAPI {
    @GET("v2/local/search/address.json")
    fun getSearchAddr(
        @Header("Authorization") key: String,
        @Query("query") query: String
 
    ): Call<ResultSearchAddr>
}