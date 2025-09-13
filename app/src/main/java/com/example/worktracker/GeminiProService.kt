package com.example.worktracker

import android.content.SharedPreferences
import com.example.worktracker.di.AppModule // For KEY_GEMINI_API_KEY
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Added @Singleton and @Inject for Hilt, assuming it will be provided by Hilt
// If AppModule provides it directly, @Singleton might be on the @Provides function instead.
// For now, let's assume direct injection is desired for the service itself, or the @Provides function will handle Singleton.
@Singleton
class GeminiProService @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    // Removed eager initialization of generativeModel

    suspend fun generateContent(prompt: String): String? {
        val apiKey = sharedPreferences.getString(AppModule.KEY_GEMINI_API_KEY, null)

        if (apiKey.isNullOrBlank()) {
            return "Error: Gemini API Key not configured. Please set it in Preferences."
        }

        return try {
            // Initialize GenerativeModel here, using the fetched API key
            val generativeModel = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = apiKey
            )
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
            response.text
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues, API errors)
            e.printStackTrace() // Log the error
            // Return a more specific error message if possible, or a generic one
            "Error generating content: ${e.message}" // Or return null as before to let ViewModel decide
        }
    }
}
