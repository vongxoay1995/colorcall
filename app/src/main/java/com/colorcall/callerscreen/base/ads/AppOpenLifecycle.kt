package com.colorcall.callerscreen.base.ads

import com.colorcall.callerscreen.base.BaseActivity


interface AppOpenLifecycle {
    fun onStart(activity: BaseActivity<*>)
    fun onDestroy()
    fun loadOpenAds()
}