package com.example.eat_share2.data.api

import com.example.eat_share2.data.models.CreateRecipeRequest
import com.example.eat_share2.data.models.CreateRecipeResponse
import com.example.eat_share2.data.models.LoginRequest
import com.example.eat_share2.data.models.LoginResponse
import com.example.eat_share2.data.models.RecipeDetailApiResponse
import com.example.eat_share2.data.models.RecipeResponse
import com.example.eat_share2.data.models.SignupRequest
import com.example.eat_share2.data.models.SignupResponse
import com.example.eat_share2.data.models.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<SignupResponse>

    @GET("recipe")
    suspend fun getRecipes(): Response<RecipeResponse>

    @GET("recipe")
    suspend fun searchRecipes(@Query("search") query: String): Response<RecipeResponse>

    @GET("recipe")
    suspend fun getRecipesByCategory(@Query("category") category: String): Response<RecipeResponse>

    @GET("recipe/{id}")
    suspend fun getRecipeById(@Path("id") recipeId: String): Response<RecipeDetailApiResponse>

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserResponse>

    @POST("recipe")
    suspend fun createRecipe(@Body createRecipeRequest: CreateRecipeRequest): Response<CreateRecipeResponse>
}