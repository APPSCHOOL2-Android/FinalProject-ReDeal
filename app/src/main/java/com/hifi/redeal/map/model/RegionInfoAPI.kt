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
}