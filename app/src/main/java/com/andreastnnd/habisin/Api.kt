package com.andreastnnd.habisin

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Api {

    companion object {
        operator fun invoke() : Api {
            return Retrofit.Builder()
                .baseUrl("https://psyched-bonfire-314213.et.r.appspot.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)
        }
    }

    @Multipart
    @POST("api/recognize")
    fun uploadImage(
        @Part image: MultipartBody.Part, // File
    ): Call<UploadResponse>
}