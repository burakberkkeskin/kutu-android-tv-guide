package local.kutu.home.model

import android.graphics.drawable.Drawable

data class AppModel(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)
