

package com.amaze.fileutilities.home_page.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.IOException
import java.util.Date
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppStorageStatsDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var appStorageStatsDao: AppStorageStatsDao
    private lateinit var installedAppsDao: InstalledAppsDao
    private lateinit var appEntry: InstalledApps

    private val appName = "testApp"
    private val appName2 = "testApp2"

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        appStorageStatsDao = db.appStorageStatsDao()
        installedAppsDao = db.installedAppsDao()
        installedAppsDao.insert(InstalledApps(appName, listOf()))
        val entry = installedAppsDao.findByPackageName(appName)
        Assert.assertNotNull(entry)
        appEntry = entry!!
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun insertTest() {
        val date = Date()
        val size = 1234L

        appStorageStatsDao.insert(appName, date, size)
        val allAppStorageStats = appStorageStatsDao.findAll()
        Assert.assertNotNull(
            "Did not contain correct entry with packageId ${appEntry.uid}, timestamp $date " +
                "and packageSize $size: $allAppStorageStats",
            allAppStorageStats.find {
                it.packageSize == size &&
                    it.timestamp == date &&
                    it.packageName == appEntry.packageName
            }
        )
    }

    @Test
    @Throws(IOException::class)
    fun deleteOlderThanDateTest() {
        val minDate = Date(1000)
        val size = 1234L

        val earlierDate = Date(100)
        val laterDate = Date(5000)
        appStorageStatsDao.insert(appName, earlierDate, size)
        appStorageStatsDao.insert(appName, laterDate, size)

        appStorageStatsDao.deleteOlderThan(minDate)
        val allStorageStats = appStorageStatsDao.findAll()
        Assert.assertEquals(
            "Database should only contain one AppStorageStats entry " +
                "but was $allStorageStats",
            1,
            allStorageStats.size
        )
        Assert.assertNotNull(
            "Database did not contain expected entry with timestamp $laterDate " +
                "but was $allStorageStats",
            allStorageStats.find {
                it.timestamp == laterDate && appEntry.packageName == it.packageName
            }
        )
    }

    @Test
    @Throws(IOException::class)
    fun findByPackageNameTest() {
        val range = LongRange(0, 3)
        val appNames = listOf(appName, appName2)
        for (i in range) {
            for (name in appNames) {
                appStorageStatsDao.insert(name, Date(i), i)
            }
        }

        val storageStats = appStorageStatsDao.findByPackageName(appNames.last())
        Assert.assertTrue(
            "Returned storage stats that were not associated to ${appNames.last()}: $storageStats",
            storageStats.all { it.packageName == appNames.last() }
        )
        Assert.assertEquals(
            "Did not return expected number of entries: $storageStats",
            range.count(),
            storageStats.size
        )
        for (i in range) {
            Assert.assertNotNull(
                "Did not find expected entry with packageSize $i and timestamp ${Date(i)}: " +
                    "$storageStats",
                storageStats.find {
                    it.packageSize == i && it.timestamp == Date(i)
                }
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun findOldestInPeriodTest() {
        val range = LongProgression.fromClosedRange(0, 30, 10)
        val appNames = listOf(appName, appName2)
        for (i in range) {
            for (name in appNames) {
                appStorageStatsDao.insert(name, Date(i), i)
            }
        }
        val periodStart = Date(4)
        val periodEnd = Date(43)
        val entry = appStorageStatsDao.findOldestWithinPeriod(
            appNames.first(), periodStart, periodEnd
        )
        Assert.assertNotNull(entry)
        val statToAppName = entry!!
        Assert.assertEquals(appNames.first(), statToAppName.packageName)
        Assert.assertEquals(Date(10), statToAppName.timestamp)
    }
}
