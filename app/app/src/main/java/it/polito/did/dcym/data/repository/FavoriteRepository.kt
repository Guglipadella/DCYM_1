package it.polito.did.dcym.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "favorites_prefs",
        Context.MODE_PRIVATE
    )

    private val _favoriteIds = MutableStateFlow<Set<Int>>(loadFavorites())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private fun loadFavorites(): Set<Int> {
        val savedString = prefs.getString(FAVORITES_KEY, "") ?: ""
        return if (savedString.isEmpty()) {
            emptySet()
        } else {
            savedString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    private fun saveFavorites(ids: Set<Int>) {
        prefs.edit().putString(FAVORITES_KEY, ids.joinToString(",")).apply()
    }

    fun toggleFavorite(productId: Int) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _favoriteIds.value = current
        saveFavorites(current)
    }

    fun isFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }

    companion object {
        private const val FAVORITES_KEY = "favorite_product_ids"

        @Volatile
        private var INSTANCE: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}