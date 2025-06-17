package com.example.zikk.model

data class Todo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean,
    val dateTime: String
)