package com.example.gk_ltdd.model

data class User(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "user", // "admin" or "user"
    val imageUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val email: String = ""
)