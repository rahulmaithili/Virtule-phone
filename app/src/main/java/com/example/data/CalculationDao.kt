package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculation_records ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_records WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CalculationRecord): Long

    @Query("DELETE FROM calculation_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE calculation_records SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM calculation_records")
    suspend fun clearAll()
}
