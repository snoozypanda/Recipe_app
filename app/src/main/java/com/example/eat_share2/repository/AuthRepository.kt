package com.example.eat_share2.repository

import com.example.eat_share2.data.api.RetrofitClient
import com.example.eat_share2.data.models.LoginRequest
import com.example.eat_share2.data.models.LoginResponse
import com.example.eat_share2.utils.TokenManager
import retrofit2.Response

class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val loginRequest = LoginRequest(username, password)
            val response: Response<LoginResponse> = RetrofitClient.apiService.login(loginRequest)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null) {
                    // Save token to cache
                    tokenManager.saveToken(loginResponse.token)

                    // Save user data if available
                    loginResponse.user?.let { user ->
                        tokenManager.saveUserData(
                            userId = user.id,
                            username = user.username,
                            email = user.email,
                            fullName = user.fullName
                        )
                    }

                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}