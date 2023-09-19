package com.hifi.redeal.map.model

// 장소명, 주소, 좌표만 받는 클래스
data class ResultSearchAddr(
    var documents: List<Place>
)

data class Place(
    var place_name: String,
    var address_name: String,
    var road_address_name: String,
    var x: String, // longitude
    var y: String, // latitude
)