package com.tinkoff.gifapp.data

import android.util.Log
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GifAPI {

    @GET("{section}/{pageNumber}")
    fun getGif(
        @Path("section") section: String,
        @Path("pageNumber") pageNumber: Int,
        @Query("json") jsonFlag : Boolean = true
    ): Single<GifResp>

    companion object Factory {
        fun create(): GifAPI {
            val loggingInterceptor = HttpLoggingInterceptor(
                logger = object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d("OkHttp", message)
                    }
                }
            )
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://developerslife.ru/")
                .client(client)
                .build()

            return retrofit.create(GifAPI::class.java)
        }
    }
}