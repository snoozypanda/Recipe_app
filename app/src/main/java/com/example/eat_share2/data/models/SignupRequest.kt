package com.example.eat_share2.data.models

data class SignupRequest(
    val fullName: String,
    val email: String,
    val username: String,
    val password: String
)