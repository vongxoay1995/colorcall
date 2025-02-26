package com.colorcall.callerscreen.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.ActivityOnboardingBinding
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityOnboardingBinding
    private var adapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupViewpager()
    }

    private fun setupViewpager() {
        val adapter = ViewPagerAdapter()
        mBinding.viewPager2.adapter = adapter
        mBinding.dotsIndicator.attachTo(mBinding.viewPager2)
    }
}