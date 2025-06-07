package com.example.eat_share2.repository

import com.example.eat_share2.data.api.RetrofitClient
import com.example.eat_share2.data.models.LoginRequest
import com.example.eat_share2.data.models.LoginResponse
import com.example.eat_share2.data.models.SignupRequest
import com.example.eat_share2.data.models.SignupResponse
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

    suspend fun signup(fullName: String, email: String, username: String, password: String): Result<SignupResponse> {
        return try {
            val signupRequest = SignupRequest(fullName, email, username, password)
            val response: Response<SignupResponse> = RetrofitClient.apiService.signup(signupRequest)

            if (response.isSuccessful) {
                val signupResponse = response.body()
                if (signupResponse != null) {
                    // If signup returns a token, save it (auto-login after signup)
                    signupResponse.token?.let { token ->
                        tokenManager.saveToken(token)

                        // Save user data if available
                        signupResponse.user?.let { user ->
                            tokenManager.saveUserData(
                                userId = user.id,
                                username = user.username,
                                email = user.email,
                                fullName = user.fullName
                            )
                        }
                    }

                    Result.success(signupResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Signup failed"
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