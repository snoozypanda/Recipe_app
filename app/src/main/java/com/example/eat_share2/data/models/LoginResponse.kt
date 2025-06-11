package com.example.eat_share2.data.models

data class LoginResponse(
    val token: String,
    val user: User? = null,
    val message: String? = null
)

data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val name: String? = null
)