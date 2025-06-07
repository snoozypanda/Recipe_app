package com.example.eat_share2.data.models

data class SignupResponse(
    val token: String? = null,
    val user: User? = null,
    val message: String? = null,
    val success: Boolean = false
)