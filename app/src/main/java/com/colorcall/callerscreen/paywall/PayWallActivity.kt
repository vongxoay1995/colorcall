package com.colorcall.callerscreen.paywall

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colorcall.callerscreen.R
import com.colorcall.callerscreen.databinding.ActivityOnboardingBinding
import com.colorcall.callerscreen.databinding.ActivityPayWallBinding

class PayWallActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityPayWallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPayWallBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupWordSpannable()
        mBinding.icClose.setOnClickListener {finish() }
    }

    fun setupWordSpannable() {
        val policy: String = getString(R.string.policy)
        val subscriptionTerm: String = getString(R.string.terms)
        val text: String = getString(R.string.term_paywall, subscriptionTerm, policy)
        var spannable = SpannableString(text)
        spannable = getWorkSpannable(spannable, text, policy, true)
        spannable = getWorkSpannable(spannable, text, subscriptionTerm, false)
        mBinding.terms.text = spannable
        mBinding.terms.movementMethod = LinkMovementMethod.getInstance()
    }
    fun getWorkSpannable(
        spannable: SpannableString,
        text: String,
        policy: String,
        isPolicy: Boolean
    ): SpannableString {
        val startIndex = text.indexOf(policy)
        val endIndex = startIndex + policy.length
        val clickableSpan = WordClickableSpan2(true)
        clickableSpan.setPolicy(isPolicy)
        spannable.setSpan(clickableSpan, startIndex, endIndex, 0)
        return spannable
    }
}