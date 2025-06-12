package com.example.eat_share2.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.eat_share2.data.models.RatingUpdate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RatingManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "rating_prefs"
        private const val KEY_RATING_UPDATES = "rating_updates"
    }

    fun saveRatingUpdate(recipeId: String, newRating: Float, newReviewCount: Int) {
        val updates = getRatingUpdates().toMutableMap()
        updates[recipeId] = RatingUpdate(recipeId, newRating, newReviewCount)

        val json = gson.toJson(updates)
        prefs.edit().putString(KEY_RATING_UPDATES, json).apply()
    }

    fun getRatingUpdates(): Map<String, RatingUpdate> {
        val json = prefs.getString(KEY_RATING_UPDATES, null) ?: return emptyMap()

        return try {
            val type = object : TypeToken<Map<String, RatingUpdate>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getRatingUpdate(recipeId: String): RatingUpdate? {
        return getRatingUpdates()[recipeId]
    }

    fun clearRatingUpdates() {
        prefs.edit().remove(KEY_RATING_UPDATES).apply()
    }

    fun removeRatingUpdate(recipeId: String) {
        val updates = getRatingUpdates().toMutableMap()
        updates.remove(recipeId)

        val json = gson.toJson(updates)
        prefs.edit().putString(KEY_RATING_UPDATES, json).apply()
    }
}