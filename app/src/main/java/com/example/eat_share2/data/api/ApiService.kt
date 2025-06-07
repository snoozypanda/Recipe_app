package com.example.eat_share2.data.api

import com.example.eat_share2.data.models.LoginRequest
import com.example.eat_share2.data.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}