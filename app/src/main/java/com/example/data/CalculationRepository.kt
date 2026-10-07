package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {

    val allHistory: Flow<List<CalculationRecord>> = dao.getAllHistory()
    val favorites: Flow<List<CalculationRecord>> = dao.getFavorites()

    suspend fun insert(record: CalculationRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long, currentFav: Boolean) {
        dao.updateFavorite(id, !currentFav)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
