package com.alimin.hwvc.screen

import com.lmy.common.BaseApplication
import com.lmy.hwvcnative.HWVC
import com.tencent.bugly.crashreport.CrashReport

class MyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        HWVC.init(applicationContext)
        CrashReport.initCrashReport(applicationContext, "4ece0a2b03", BuildConfig.DEBUG)
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }
}