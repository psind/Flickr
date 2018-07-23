package com.example.klickr.flickr.data

import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @ Parteek on 20/07/18.
 */
interface RetrofitService {

    @GET()
    fun searchImages(@Query("text") keyword: String, @Query("page") page: Int, @Query("per_page") count: Int): Observable<FlickrResponse>

    companion object {
        fun create(): RetrofitService {

            val headerInterceptor = Interceptor {
                val original = it.request()

                val request = original?.newBuilder()
                        ?.method(original.method(), original.body())
                        ?.build()

                it.proceed(request ?: original)
            }

            val httpClient = OkHttpClient.Builder().apply {
                addInterceptor(headerInterceptor)

            }.build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Utils.URL)
                    .client(httpClient)
                    .build()

            return retrofit.create(RetrofitService::class.java)
        }
    }
}