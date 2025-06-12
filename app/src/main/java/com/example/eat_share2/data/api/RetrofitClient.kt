package com.example.eat_share2.data.api

import com.example.eat_share2.utils.AuthInterceptor
import com.example.eat_share2.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://twelve-ideas-sneeze.loca.lt"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // We'll need to initialize this with a context later
    private var tokenManager: TokenManager? = null

    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Add auth interceptor if token manager is available
        tokenManager?.let { tm ->
            builder.addInterceptor(AuthInterceptor(tm))
        }

        builder.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}