package com.alimin.hwvc.screen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lmy.common.R
import com.lmy.common.adapter.RecyclerAdapter

class SettingsAdapter : RecyclerAdapter<List<String>, SettingsAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, item: List<String>, position: Int) {
        holder.onBind(item, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            this,
            LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        )
    }

    class ViewHolder(internal var adapter: SettingsAdapter, itemView: View) :
        RecyclerAdapter.BaseViewHolder<List<String>>(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.titleView)
        private val bodyView: TextView = itemView.findViewById(R.id.bodyView)
        private val valueView: TextView = itemView.findViewById(R.id.valueView)

        override fun onBind(item: List<String>, position: Int) {
            titleView.text = item[0]
            bodyView.text = item[1]
            valueView.text = item[2]
        }
    }
}