package com.example.carlog.utils

import android.content.Context
import android.content.SharedPreferences

object MySharedPreferences {
    private var mAppContext: Context? = null
    private const val SHARED_PREFERENCES_NAME = "hospital_data" // Modified to remove spaces
    private const val USER_NAME = "user_name" // Modified to remove spaces
    private const val USER_EMAIL = "user_email"
    private const val USER_TOKEN = "user_token"
    private const val USER_ID = "user_id"
    private const val USER_ADDRESS = "user_address"

    fun init(appContext: Context) {
        mAppContext = appContext.applicationContext
    }

    private fun getSharedPreferences(): SharedPreferences {
        return mAppContext?.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            ?: throw IllegalStateException("SharedPreferences not initialized")
    }
    fun setUserAddress(address: String){
        getSharedPreferences().edit().putString(USER_ADDRESS,address).apply()
    }
    fun getUserAddress() : String{
        return getSharedPreferences().getString(USER_ADDRESS,"")!!
    }

    fun setUserEmail(email: String) {
        getSharedPreferences().edit().putString(USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String {
        return getSharedPreferences().getString(USER_EMAIL, "")!!
    }

    fun setUserName(name: String) {
        getSharedPreferences().edit().putString(USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return getSharedPreferences().getString(USER_NAME, "")!!
    }

    fun setUserId(id: Int) {
        getSharedPreferences().edit().putInt(USER_ID, id).apply()
    }

    fun getUserId(): Int {
        return getSharedPreferences().getInt(USER_ID, 0)
    }

    fun setUserToken(token: String) {
        getSharedPreferences().edit().putString(USER_TOKEN, token).apply()
    }

    fun getUserToken(): String? {
        return getSharedPreferences().getString(USER_TOKEN, "")
    }

    fun clearUserData() {
        val editor = getSharedPreferences().edit()
        editor.remove(USER_EMAIL)
        editor.remove(USER_ID)
        editor.remove(USER_NAME)
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}
