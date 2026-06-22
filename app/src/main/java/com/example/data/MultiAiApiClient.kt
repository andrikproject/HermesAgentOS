package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.IOException
import java.util.concurrent.TimeUnit

// --- OpenAI Class Types ---
@JsonClass(generateAdapter = true)
data class OpenAiChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenAiChatMessage>,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "stream") val stream: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiStreamChunk(
    @Json(name = "choices") val choices: List<OpenAiStreamChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiStreamChoice(
    @Json(name = "delta") val delta: OpenAiStreamDelta? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiStreamDelta(
    @Json(name = "content") val content: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiChatMessage? = null
)

interface OpenAiApiService {
    @POST
    suspend fun generateChatCompletion(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): OpenAiChatResponse
}

// --- Anthropic Class Types ---
@JsonClass(generateAdapter = true)
data class AnthropicMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class AnthropicRequest(
    @Json(name = "model") val model: String,
    @Json(name = "max_tokens") val maxTokens: Int = 1024,
    @Json(name = "system") val system: String? = null,
    @Json(name = "messages") val messages: List<AnthropicMessage>
)

@JsonClass(generateAdapter = true)
data class AnthropicResponse(
    @Json(name = "content") val content: List<AnthropicContentPart>? = null
)

@JsonClass(generateAdapter = true)
data class AnthropicContentPart(
    @Json(name = "type") val type: String? = null,
    @Json(name = "text") val text: String? = null
)

interface AnthropicApiService {
    @POST("v1/messages")
    suspend fun generateMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse
}

object MultiAiApiClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val openAiService: OpenAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Fallback base URL for pathless requests
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAiApiService::class.java)
    }

    val anthropicService: AnthropicApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AnthropicApiService::class.java)
    }

    /**
     * Fetches available models from an OpenAI-compatible /v1/models endpoint.
     * Supports: OpenAI, DeepSeek, Mistral AI, Groq, Cohere, xAI/Grok, HuggingFace,
     * Together AI, OpenRouter, and any other OpenAI-compatible API.
     *
     * Response format expected:
     *   { "data": [ { "id": "model-name", ... }, ... ] }
     * OpenRouter returns the same format.
     */
    fun fetchAvailableModels(endpoint: String, apiKey: String): List<String> {
        val models = mutableListOf<String>()
        try {
            val url = if (endpoint.endsWith("/")) "${endpoint}v1/models" else "$endpoint/v1/models"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val data = json.optJSONArray("data")
                    if (data != null) {
                        for (i in 0 until data.length()) {
                            val item = data.optJSONObject(i)
                            val id = item?.optString("id")
                            if (id != null) {
                                models.add(id)
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            // Network error - return empty list
        } catch (e: Exception) {
            // Parse error - return empty list
        }
        return models
    }
}
