package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_records")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val type: String, // "STANDARD", "SCIENTIFIC", "CONVERSION"
    val detail: String = "", // e.g. "deg", "rad", "kg -> lb"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
