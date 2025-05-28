package com.namnh.novelreaderapp.item

data class User (
    var uid: String = "", // ID  người dùng
    var username: String = "", // Tên người dùng
    var email: String = "", // Email của người dùng
    var avatar: String = "",// URL của hình đại diện người dùng
    var role: String = "" // Vai trò của người dùng
)