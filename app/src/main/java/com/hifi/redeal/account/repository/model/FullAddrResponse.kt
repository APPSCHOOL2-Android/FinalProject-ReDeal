package com.hifi.redeal.account.repository.model

data class FullAddrResponse(
    val coordinateInfo : CoordinateInfo
)

data class CoordinateInfo(
    val coordinate: List<Coordinate>
)

data class Coordinate(
    val newLat: String,
    val newLon: String,
    val newBuildingName: String
)