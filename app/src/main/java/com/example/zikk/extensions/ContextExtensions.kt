package com.example.zikk.extensions

import android.content.Context
import androidx.core.content.edit

// extensions/ContextExtensions.kt
private const val PREF_NAME = "app_prefs"
private const val KEY_LOGIN_TOKEN = "auth_token"

fun Context.saveLoginToken(token: String) {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() {
            putString(KEY_LOGIN_TOKEN, token)
        }
}

fun Context.getLoginToken(): String? {
    return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LOGIN_TOKEN, null)
}

fun Context.removeLoginToken() {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() {
            remove(KEY_LOGIN_TOKEN)
        }
}

fun Context.saveUserPhoneNumber(phoneNumber: String) {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() {
            putString("USER_PHONE_NUMBER", phoneNumber)
        }
}

fun Context.getUserPhoneNumber(): String? {
    return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString("USER_PHONE_NUMBER", null)
}

fun Context.removeUserPhoneNumber() {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() {
            remove("USER_PHONE_NUMBER")
        }
}

fun Context.saveUserRole(role: String) {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit(){
            putString("USER_ROLE", role)
        }
}

fun Context.getUserRole(): String? {
    return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString("USER_ROLE", null)
}

fun Context.removeUserRole() {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit() {
            remove("USER_ROLE")
        }
}