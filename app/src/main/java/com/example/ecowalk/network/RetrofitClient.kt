package com.example.ecowalk.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // OSRM API for routing
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/"
    
    private val osrmClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    val osrmApi: OSRMApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OSRM_BASE_URL)
            .client(osrmClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OSRMApiService::class.java)
    }

    // Overpass API for green space data
    private const val OVERPASS_BASE_URL = "https://overpass-api.de/api/"
    
    private val overpassClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    val overpassApi: OverpassApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OVERPASS_BASE_URL)
            .client(overpassClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApiService::class.java)
    }

    // Nominatim API for geocoding (place name -> coordinates)
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    
    private val nominatimClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "EcoWalk-Android/1.0")
                .build()
            chain.proceed(request)
        }
        .build()
    
    val nominatimApi: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(nominatimClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)
    }
}
