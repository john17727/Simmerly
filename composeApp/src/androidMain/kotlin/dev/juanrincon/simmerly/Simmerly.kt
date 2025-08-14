package dev.juanrincon.simmerly

import android.app.Application
import dev.juanrincon.simmerly.di.initKoin
import org.koin.android.ext.koin.androidContext

class Simmerly: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@Simmerly)
        }
    }

}