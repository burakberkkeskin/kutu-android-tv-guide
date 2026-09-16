package local.kutu.home.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import local.kutu.home.R
import local.kutu.home.model.AppModel

class DockAdapter(
    private val items: MutableList<AppModel>,
    private val onItemFocused: (AppModel, Int) -> Unit,
    private val onItemClick: (AppModel, Int) -> Unit,
    private val onItemLongClick: (AppModel, Int) -> Unit
) : RecyclerView.Adapter<DockAdapter.ViewHolder>() {

    var movingPosition: Int = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconCard: FrameLayout = view.findViewById(R.id.icon_card)
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appTitle: TextView = view.findViewById(R.id.app_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dock_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        holder.appIcon.setImageDrawable(app.icon)
        holder.appTitle.text = app.label

        val isMoving = (position == movingPosition)
        if (isMoving) {
            holder.iconCard.setBackgroundResource(R.drawable.bg_tile_moving)
            holder.itemView.scaleX = 1.12f
            holder.itemView.scaleY = 1.12f
            holder.itemView.translationZ = 16f
            holder.appTitle.alpha = 1.0f
        } else {
            holder.iconCard.setBackgroundResource(R.drawable.bg_tile_selector)
            if (!holder.itemView.isFocused) {
                holder.itemView.scaleX = 1.0f
                holder.itemView.scaleY = 1.0f
                holder.itemView.translationZ = 0f
                holder.appTitle.alpha = 0.8f
            }
        }

        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (movingPosition == -1) {
                if (hasFocus) {
                    view.animate().scaleX(1.12f).scaleY(1.12f).translationZ(12f).setDuration(150).start()
                    holder.appTitle.animate().alpha(1.0f).setDuration(150).start()
                    holder.appTitle.isSelected = true // enable marquee if text is long
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos in items.indices) {
                        onItemFocused(items[pos], pos)
                    }
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(150).start()
                    holder.appTitle.animate().alpha(0.8f).setDuration(150).start()
                    holder.appTitle.isSelected = false
                }
            }
        }

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick(items[pos], pos)
            }
        }

        holder.itemView.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && movingPosition == -1) {
                onItemLongClick(items[pos], pos)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition in items.indices && toPosition in items.indices) {
            val item = items.removeAt(fromPosition)
            items.add(toPosition, item)
            movingPosition = toPosition
            notifyItemMoved(fromPosition, toPosition)
        }
    }
}
