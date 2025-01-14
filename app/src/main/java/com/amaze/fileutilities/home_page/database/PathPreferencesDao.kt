package com.amaze.fileutilities.home_page.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PathPreferencesDao {

    @Query("SELECT * FROM pathpreferences")
    fun getAll(): List<PathPreferences>

    @Query("SELECT * FROM pathpreferences WHERE feature=:feature")
    fun findByFeature(feature: Int): List<PathPreferences>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(analysis: PathPreferences)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(analysis: List<PathPreferences>)

    @Delete
    fun delete(pathPreferences: PathPreferences)
}
