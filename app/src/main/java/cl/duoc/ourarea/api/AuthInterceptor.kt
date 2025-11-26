package cl.duoc.ourarea.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor to add authentication token to requests
 * This interceptor automatically adds the "Authorization: Bearer {token}" header
 * to all API requests if a token is available
 */
class AuthInterceptor : Interceptor {

    @Volatile
    private var token: String? = null

    /**
     * Set the authentication token
     * @param authToken JWT token from Xano
     */
    fun setToken(authToken: String?) {
        this.token = authToken
    }

    /**
     * Get the current authentication token
     * @return Current JWT token or null
     */
    fun getToken(): String? = token

    /**
     * Clear the authentication token (for logout)
     */
    fun clearToken() {
        this.token = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // If there's no token, proceed with original request
        val currentToken = token
        if (currentToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Check if request already has Authorization header
        if (originalRequest.header("Authorization") != null) {
            // Don't override existing Authorization header
            return chain.proceed(originalRequest)
        }

        // Add Bearer token to request
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $currentToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
