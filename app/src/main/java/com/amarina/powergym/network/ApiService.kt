package com.amarin.powergym.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("activities/new")
    suspend fun getNewActivities(@Query("userId")userId: Int): Call<List<String>>

}
