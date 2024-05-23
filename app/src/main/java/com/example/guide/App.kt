package com.example.guide

import android.app.Application
import androidx.room.Room
import com.example.guide.data.AppDatabase
import com.yandex.mapkit.MapKitFactory

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
    }


    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("12b42db8-bb92-4c01-aac5-16894bdc92b3")
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
            .build()
    }


}






