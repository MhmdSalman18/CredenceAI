package com.credenceai.app


import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.credenceai.app.BuildConfig

@HiltAndroidApp
class CredenceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}