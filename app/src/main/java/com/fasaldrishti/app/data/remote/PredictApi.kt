package com.fasaldrishti.app.data.remote

import com.fasaldrishti.app.data.remote.dto.DiseaseInfoDto
import com.fasaldrishti.app.data.remote.dto.PredictionResponse
import com.fasaldrishti.app.data.remote.dto.SecondaryAiRequest
import com.fasaldrishti.app.data.remote.dto.SecondaryAiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface PredictApi {
    @Multipart
    @POST("predict")
    suspend fun predictCropDisease(
        @Part file: MultipartBody.Part
    ): Response<PredictionResponse>

    @POST("predict/secondary")
    suspend fun getSecondaryAdvisory(
        @Body request: SecondaryAiRequest
    ): Response<SecondaryAiResponse>

    @GET("diseases/{class_id}")
    suspend fun getDiseaseInfo(
        @Path("class_id") classId: String
    ): Response<DiseaseInfoDto>

    @GET("diseases")
    suspend fun listDiseases(
        @Query("crop") crop: String? = null
    ): Response<Map<String, Any>>
}
