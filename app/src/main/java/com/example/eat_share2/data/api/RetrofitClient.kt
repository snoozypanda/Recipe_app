package com.example.eat_share2.data.api

import com.example.eat_share2.utils.AuthInterceptor
import com.example.eat_share2.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://your-api-base-url.com/api/" // Replace with your actual API URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create a function to get OkHttpClient with TokenManager
    private fun getOkHttpClient(tokenManager: TokenManager? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Add auth interceptor if tokenManager is provided
        tokenManager?.let {
            builder.addInterceptor(AuthInterceptor(it))
        }

        return builder.build()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(getOkHttpClient()) // Default client without auth
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Function to create authenticated API service
    fun createAuthenticatedApiService(tokenManager: TokenManager): ApiService {
        val authenticatedRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return authenticatedRetrofit.create(ApiService::class.java)
    }
}