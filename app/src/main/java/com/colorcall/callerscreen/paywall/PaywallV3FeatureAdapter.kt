package com.colorcall.callerscreen.paywall

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colorcall.callerscreen.databinding.ItemPaywallV3FeatureBinding

class PaywallV3FeatureAdapter(
    private val features: List<PaywallV3Feature>
) : RecyclerView.Adapter<PaywallV3FeatureAdapter.FeatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val binding = ItemPaywallV3FeatureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(features[position])
    }

    override fun getItemCount(): Int = features.size

    class FeatureViewHolder(
        private val binding: ItemPaywallV3FeatureBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feature: PaywallV3Feature) {
            binding.imgFeatureV3.setImageResource(feature.iconRes)
            binding.tvFeatureV3.text = feature.title
        }
    }
}
