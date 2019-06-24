package com.twoeightnine.root.xvii.dagger.modules

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.network.ApiService
import com.twoeightnine.root.xvii.network.TokenAndVersionInterceptor
import com.twoeightnine.root.xvii.network.datausage.DataUsageInterceptor
import com.twoeightnine.root.xvii.utils.ApiUtils
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    private val timeout = 300L

    @Provides
    @Singleton
    fun provideTokenAndVersionInterceptor(): TokenAndVersionInterceptor = TokenAndVersionInterceptor()


    @Provides
    @Singleton
    fun provideDataUsageInterceptor(): DataUsageInterceptor = DataUsageInterceptor()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val log = HttpLoggingInterceptor()
        log.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return log
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            tokenAndVersionInterceptor: TokenAndVersionInterceptor,
            dataUsageInterceptor: DataUsageInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenAndVersionInterceptor)
            .addInterceptor(dataUsageInterceptor)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .build()


    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?) = false
        override fun shouldSkipField(f: FieldAttributes) = false
    }).create()

    @Provides
    @Singleton
    fun provideNetwork(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(App.API_URL)
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideApiUtils(api: ApiService): ApiUtils = ApiUtils(api)
}