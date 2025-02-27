package com.colorcall.callerscreen.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.ActivityOnboardingBinding
import com.colorcall.callerscreen.main.MainActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupViewpager()
    }

    private fun setupViewpager() {
        val onboardingItems = listOf(
            PageData(R.drawable.ob1, "Welcome", "This is the first step to greatness."),
            PageData(R.drawable.ob2, "Explore", "Discover amazing features with us."),
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
}