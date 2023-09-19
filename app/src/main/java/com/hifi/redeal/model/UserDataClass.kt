package com.hifi.redeal.model

data class UserDataClass(
    var userIdx: Long, // 유저 인덱스
    val userEmail: String, // 유저 아이디(email)
    val userPw: String, // 유저 비밀번호
    val userName: String // 유저 이름
)