package com.example.carlog.di

import android.content.Context
import android.content.SharedPreferences
import com.example.carlog.network.RetrofitService
import com.example.carlog.utils.MySharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "https://carlog.runasp.net/api/"

    @Singleton
    @Provides
    fun provideRetrofit(@ApplicationContext context: Context): RetrofitService {
        val client = OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(150, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .callTimeout(50, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(provideAuthorizationInterceptor())
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(RetrofitService::class.java)
    }

    private fun provideAuthorizationInterceptor() = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        val url = originalUrl.newBuilder().build()
        val requestBuilder = originalRequest.newBuilder().url(url)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer ${MySharedPreferences.getUserToken()}")
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
}
