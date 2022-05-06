package de.fhe.familycare

import android.app.Application
import android.util.Log

/**
 * main application class (unused)
 */
class FamilyCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("Application", "Application created")
    }
}