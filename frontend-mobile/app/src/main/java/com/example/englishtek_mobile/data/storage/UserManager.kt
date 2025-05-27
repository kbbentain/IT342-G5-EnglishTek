package com.example.englishtek_mobile.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.englishtek_mobile.data.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserManager(private val context: Context) {
    companion object {
        private val Context.dataStore by preferencesDataStore("user_preferences")
        private val USER_DATA_KEY = stringPreferencesKey("user_data")
        private val gson = Gson()
    }
    
    suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        context.dataStore.edit { preferences ->
            preferences[USER_DATA_KEY] = userJson
        }
    }
    
    fun getUser(): Flow<User?> = context.dataStore.data.map { preferences ->
        val userJson = preferences[USER_DATA_KEY]
        if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }
    
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_DATA_KEY)
        }
    }
}
