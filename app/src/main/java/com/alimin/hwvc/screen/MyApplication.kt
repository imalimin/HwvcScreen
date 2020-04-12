package com.alimin.hwvc.screen

import com.lmy.common.BaseApplication
import com.lmy.hwvcnative.HWVC

class MyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        HWVC.init(applicationContext)
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}