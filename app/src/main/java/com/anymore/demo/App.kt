package com.anymore.demo

import android.app.Application
import timber.log.Timber

/**
 * Created by liuyuanmao on 2019/11/15.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}