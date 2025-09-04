package com.cogninote.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey
    val name: String,
    val color: String? = null,
    val description: String = "",
    val createdAt: Instant = Clock.System.now(),
    val usageCount: Int = 0,
    val isDeleted: Boolean = false
)
