package com.example.eat_share2.data.api

import com.example.eat_share2.data.models.LoginRequest
import com.example.eat_share2.data.models.LoginResponse
import com.example.eat_share2.data.models.SignupRequest
import com.example.eat_share2.data.models.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<SignupResponse>
}