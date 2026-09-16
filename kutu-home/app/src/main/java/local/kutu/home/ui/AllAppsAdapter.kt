package local.kutu.home.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import local.kutu.home.R
import local.kutu.home.model.AppModel

class AllAppsAdapter(
    private val items: List<AppModel>,
    private val onItemClick: (AppModel) -> Unit,
    private val onItemLongClick: (AppModel) -> Unit
) : RecyclerView.Adapter<AllAppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.grid_app_icon)
        val appLabel: TextView = view.findViewById(R.id.grid_app_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        holder.appIcon.setImageDrawable(app.icon)
        holder.appLabel.text = app.label

        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate().scaleX(1.1f).scaleY(1.1f).translationZ(8f).setDuration(150).start()
            } else {
                view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(150).start()
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(app)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(app)
            true
        }
    }

    override fun getItemCount(): Int = items.size
}
