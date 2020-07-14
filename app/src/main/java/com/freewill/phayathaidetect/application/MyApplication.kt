package com.freewill.phayathaidetect.application

import android.app.Application
import android.content.Context
import com.fxn.stash.Stash

class MyApplication : Application() {
    protected override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Stash.init(this)
    }
}