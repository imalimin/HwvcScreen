package com.alimin.hwvc.screen

import android.app.Application
import com.lmy.hwvcnative.HWVC

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HWVC.init(applicationContext)
    }
}