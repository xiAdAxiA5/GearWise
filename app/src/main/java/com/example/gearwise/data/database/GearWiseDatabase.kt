package com.example.gearwise.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gearwise.data.dao.ElectronicItemDao
import com.example.gearwise.data.model.ElectronicItem

@Database(
    entities = [ElectronicItem::class],
    version = 1,
    exportSchema = false
)
abstract class GearWiseDatabase : RoomDatabase() {

    abstract fun electronicItemDao(): ElectronicItemDao

    companion object {
        @Volatile
        private var INSTANCE: GearWiseDatabase? = null

        fun getDatabase(context: Context): GearWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GearWiseDatabase::class.java,
                    "gearwise_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
