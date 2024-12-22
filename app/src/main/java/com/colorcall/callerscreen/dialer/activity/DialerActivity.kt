package com.colorcall.callerscreen.dialer.activity

import com.colorcall.callerscreen.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity

open class DialerActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher,
    )

    override fun getAppLauncherName() = getString(R.string.app_name)
}