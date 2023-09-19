package com.hifi.redeal.map.model

data class ResultSearchRegion(
    val admVoList: AdmVoList
)

data class AdmVoList(
    val message: String,
    val error: String,
    val totalCount: String,
    val pageNo: String,
    val admVoList: List<AdmVO>, // "admVoList"로 정의
    val numOfRows: String
)

data class AdmVO(
    val admCodeNm: String,
    val admCode: String,
    val lowestAdmCodeNm: String
)
