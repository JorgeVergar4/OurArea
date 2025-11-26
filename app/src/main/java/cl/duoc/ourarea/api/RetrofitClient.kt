package cl.duoc.ourarea.api

import cl.duoc.ourarea.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object to configure and provide Retrofit instances for Xano API
 *
 * Xano Endpoints:
 * - Events API: https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/
 * - Auth API (signup/login): https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/
 * - Additional API: https://x8ki-letl-twmt.n7.xano.io/api:To8zQg-7/
 * - Additional API: https://x8ki-letl-twmt.n7.xano.io/api:6dvTuZu9/
 */
object RetrofitClient {

    // Xano API Base URLs
    private const val XANO_EVENTS_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/"
    private const val XANO_AUTH_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/"

    /**
     * OkHttpClient configuration with logging
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Retrofit instance for Events API
     * Base URL: https://x8ki-letl-twmt.n7.xano.io/api:8B8nOhtv/
     */
    private val eventsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(XANO_EVENTS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Retrofit instance for Auth API
     * Base URL: https://x8ki-letl-twmt.n7.xano.io/api:p4Kx6qbK/
     */
    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(XANO_AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Events API Service instance
     * Use this for all event-related operations (GET, POST, PATCH, DELETE events)
     */
    val eventsApiService: XanoApiService by lazy {
        eventsRetrofit.create(XanoApiService::class.java)
    }

    /**
     * Auth API Service instance
     * Use this for authentication operations (signup, login, getCurrentUser)
     */
    val authApiService: XanoAuthApiService by lazy {
        authRetrofit.create(XanoAuthApiService::class.java)
    }

    /**
     * Default API service (uses Events API)
     */
    val apiService: XanoApiService by lazy {
        eventsApiService
    }
}
