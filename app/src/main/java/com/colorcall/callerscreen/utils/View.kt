package com.colorcall.callerscreen.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.colorcall.callerscreen.R

fun View.shineAnimationView() {
    if (visibility != View.VISIBLE) {
        return
    }
    clearAnimation() // Clear any existing animations
    val animCycle =
        AnimationUtils.loadAnimation(context, R.anim.snake_animation) // Load the shake animation
    startAnimation(animCycle) // Start the animation
    alpha = 1f

    animCycle.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            postDelayed({
                if(visibility == View.VISIBLE) {
                    startAnimation(animCycle) // Restart the animation
                }
            }, 1000)
        }

        override fun onAnimationStart(animation: Animation) {
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    })
}
