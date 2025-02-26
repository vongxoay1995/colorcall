package com.colorcall.callerscreen.onboarding

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.colorcall.callerscreen.R

class ViewPagerAdapter(val context: Context, private val pages: List<PageData>) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtScannow: TextView?= itemView.findViewById(R.id.scan_now)
        val tvComment: TextView?= itemView.findViewById(R.id.tv_comment)
        val imgStar: ImageView?= itemView.findViewById(R.id.img_star)
        val title: TextView?= itemView.findViewById(R.id.title)


        fun startAnimations() {
            if (itemViewType == R.layout.layout_onboarding_4 || itemViewType == R.layout.layout_onboarding_5) {
                // Khởi tạo trạng thái ban đầu
                title?.visibility = View.INVISIBLE
                imgStar?.visibility = View.INVISIBLE
                tvComment?.visibility = View.INVISIBLE

                // Animation cho `title`
                val fadeInAnimation1 = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                fadeInAnimation1.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        title?.visibility = View.VISIBLE
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                title?.startAnimation(fadeInAnimation1)

                val fadeInAnimation2 = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                fadeInAnimation2.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        imgStar?.visibility = View.VISIBLE
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                Handler(Looper.getMainLooper()).postDelayed({
                    imgStar?.startAnimation(fadeInAnimation2)
                }, 350)

                // Animation cho `tvComment` và `tvName`
                val fadeInAnimation3 = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                fadeInAnimation3.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        tvComment?.visibility = View.VISIBLE
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                Handler(Looper.getMainLooper()).postDelayed({
                    tvComment?.startAnimation(fadeInAnimation3)
                }, 700)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pageData = pages[position]
        if (pageData.layoutResId == R.layout.layout_onboarding_1) {
            val color = ContextCompat.getColor(context, R.color.colorBlue)
            val spannableString = SpannableString(holder.txtScannow?.text)
            holder.txtScannow?.text?.toString()?.let {
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    4,
                    it.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            holder.txtScannow?.text = spannableString
        }
    }



    override fun getItemViewType(position: Int): Int = pages[position].layoutResId

    override fun getItemCount(): Int = pages.size
}