package cl.duoc.ourarea.api

import cl.duoc.ourarea.api.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Xano Authentication API Service Interface
 * Handles user authentication (login and signup)
 * Base URL: https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/
 *
 * Note: Xano returns only user_id in responses, not tokens
 */
interface XanoAuthApiService {

    /**
     * Register a new user
     * POST /auth/signup
     * @param request SignupRequest with name, email, password
     * @return AuthResponse with user_id only
     */
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<AuthResponse>

    /**
     * Login existing user
     * POST /auth/login
     * @param request LoginRequest with email and password
     * @return AuthResponse with user_id only
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
}
