package com.colorcall.callerscreen.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.ActivityOnboardingBinding
import com.colorcall.callerscreen.main.MainActivity
import com.colorcall.callerscreen.utils.AdListener
import com.colorcall.callerscreen.utils.AppUtils
import com.colorcall.callerscreen.utils.BannerAdsUtils
import com.colorcall.callerscreen.utils.ConstantAds

class OnboardingActivity : AppCompatActivity(), AdListener {
    private lateinit var mBinding: ActivityOnboardingBinding
    private var bannerAdsUtils: BannerAdsUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        bannerAdsUtils = BannerAdsUtils(this, mBinding.layoutAds)
        setupViewpager()
        if (AppUtils.isNetworkConnected(this)) {
            loadAds()
        } else {
            mBinding.layoutAds.setVisibility(View.GONE)
        }
        AppUtils.setFullNav(this)
    }
    private fun loadAds() {
        bannerAdsUtils!!.setIdAds(ConstantAds.banner_onboarding)
        bannerAdsUtils!!.setAdListener(this)
        bannerAdsUtils!!.loadAds()
    }
    private fun setupViewpager() {
        val onboardingItems = listOf(
            PageData(R.drawable.ob1, getString(R.string.title_ob1), getString(R.string.des_ob1)),
            PageData(R.drawable.ob2, getString(R.string.title_ob2), getString(R.string.des_ob2)),
        )
        val adapter = ViewPagerAdapter(this, onboardingItems)

        mBinding.viewPager2.adapter = adapter
        mBinding.dotsIndicator.attachTo(mBinding.viewPager2)
        mBinding.btnContinue.setOnClickListener {
            if (mBinding.viewPager2.currentItem < onboardingItems.size - 1) {
                mBinding.viewPager2.currentItem += 1
            } else {
                moveMain()
            }
        }
        mBinding.viewPager2.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                mBinding.btnContinue.text = if (position == onboardingItems.size - 1) getString(R.string.get_started) else getString(R.string.next)
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun moveMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onAdloaded() {

    }

    override fun onAdFailed() {
    }
}