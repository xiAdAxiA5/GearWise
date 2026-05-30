package com.example.gearwise

import android.app.Application
import com.example.gearwise.data.database.GearWiseDatabase
import com.example.gearwise.data.repository.ItemRepository

class GearWiseApplication : Application() {

    val database by lazy { GearWiseDatabase.getDatabase(this) }
    val repository by lazy { ItemRepository(database.electronicItemDao()) }
}
