package org.kreal.hkshare

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import org.kreal.hkshare.configure.Rout

class RoutAdapter(private val routList: List<Rout>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = if (position == (itemCount - 1)) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val imageView = ImageButton(parent.context)
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            imageView.setImageResource(R.drawable.ic_action_add)
//            imageView.setBackgroundColor(Color.TRANSPARENT)
            imageView.setBackgroundResource(R.drawable.image_select)
//            imageView.setTextAppearance(android.R.style.)//.Button.Borderless)
            ItemAddHolder(imageView)
        } else {
            ItemRoutHolder(LayoutInflater.from(parent.context).inflate(R.layout.rout_item, parent, false))
        }
    }

    override fun getItemCount(): Int = routList.size + 1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        holder?.also {
            when (it) {
                is ItemAddHolder -> {
                    it.itemView.setOnClickListener {

                    }
                }
                is ItemRoutHolder -> {
                    val info = routList[position]
                    it.apply {
                        textView.text = "${info.key}\r\n${info.file}"
                    }
                }
            }
        }
    }

    class ItemRoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val delete: ImageView = itemView.findViewById(R.id.item_delete)
        val switch: Switch = itemView.findViewById(R.id.item_switch)
    }

    class ItemAddHolder(itemView: ImageButton) : RecyclerView.ViewHolder(itemView)
}