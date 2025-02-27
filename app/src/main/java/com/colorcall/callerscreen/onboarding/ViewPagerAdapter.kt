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
import androidx.viewpager.widget.PagerAdapter
import com.colorcall.callerscreen.R

class ViewPagerAdapter(val context: Context, private val items: List<PageData>) : PagerAdapter() {

    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.layout_onboarding, container, false)

        val item = items[position]
        val imageView = view.findViewById<ImageView>(R.id.img_ob)
        val titleText = view.findViewById<TextView>(R.id.title)
        val descText = view.findViewById<TextView>(R.id.des)

        imageView.setImageResource(item.imageResId)
        titleText.text = item.title
        descText.text = item.des

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}