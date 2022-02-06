/*
 * Copyright (C) 2021-2022 Team Amaze - Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com>. All Rights reserved.
 *
 * This file is part of Amaze File Utilities.
 *
 * 'Amaze File Utilities' is a registered trademark of Team Amaze. All other product
 * and company names mentioned are trademarks or registered trademarks of their respective owners.
 */

package com.amaze.fileutilities.home_page.database

import androidx.room.*

@Dao
interface InternalStorageAnalysisDao {

    @Query("SELECT * FROM internalstorageanalysis where is_mediastore=0")
    fun getAll(): List<InternalStorageAnalysis>

    @Query("SELECT * FROM internalstorageanalysis where depth<=:depth and is_mediastore=0")
    fun getAllShallow(depth: Int): List<InternalStorageAnalysis>

    @Query("SELECT * FROM internalstorageanalysis where is_mediastore=1")
    fun getAllMediaFiles(): List<InternalStorageAnalysis>

    @Query("SELECT * FROM internalstorageanalysis where is_empty=1")
    fun getAllEmptyFiles(): List<InternalStorageAnalysis>

    @Query("SELECT * FROM internalstorageanalysis WHERE sha256_checksum=:sha256Checksum")
    fun findBySha256Checksum(sha256Checksum: String): InternalStorageAnalysis?

    @Query(
        "SELECT * FROM internalstorageanalysis " +
            "WHERE is_mediastore = 1 and sha256_checksum=:sha256Checksum"
    )
    fun findMediaFileBySha256Checksum(sha256Checksum: String): InternalStorageAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(analysis: InternalStorageAnalysis)

    @Delete
    fun delete(user: InternalStorageAnalysis)
}