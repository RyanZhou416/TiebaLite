package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import java.lang.ref.WeakReference

class TranslucentThemeColorAdapter(context: Context) : RecyclerView.Adapter<MyViewHolder>() {

    private val contextRef = WeakReference(context)
    private var colors: MutableList<Int> = mutableListOf()
    private var selectedColor: Int = 0

    var onItemClickListener: OnItemClickListener<Int>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val context: Context?
        get() = contextRef.get()

    fun setPalette(palette: Palette) {
        colors = mutableListOf<Int>().apply {
            val paletteColors = intArrayOf(
                palette.getVibrantColor(Color.TRANSPARENT),
                palette.getMutedColor(Color.TRANSPARENT),
                palette.getDominantColor(Color.TRANSPARENT)
            )
            for (color in paletteColors) {
                if (color != Color.TRANSPARENT) add(color)
            }
            addAll(COLORS.toList())
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(parent.context, R.layout.item_theme_color)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val color = colors[position]
        holder.setItemOnClickListener {
            selectedColor = color
            notifyDataSetChanged()
            onItemClickListener?.onClick(holder.itemView, color, position, 0)
        }
        val preview = holder.getView<MaterialCardView>(R.id.theme_preview)
        preview.setCardBackgroundColor(color)
        preview.setStrokeColor(ColorStateList.valueOf(color))
        holder.setVisibility(
            R.id.theme_selected,
            if (selectedColor == color) View.VISIBLE else View.GONE
        )
    }

    override fun getItemCount(): Int = colors.size

    companion object {
        private val COLORS = intArrayOf(
            Color.parseColor("#FF4477E0"),
            Color.parseColor("#FFFF9A9E"),
            Color.parseColor("#FFC51100"),
            Color.parseColor("#FF000000"),
            Color.parseColor("#FF512DA8")
        )
    }
}
