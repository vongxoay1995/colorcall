package com.colorcall.callerscreen.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
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
        Glide.with(context).load(item.imageResId).into(imageView)
        titleText.text = item.title
        descText.text = item.des

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}