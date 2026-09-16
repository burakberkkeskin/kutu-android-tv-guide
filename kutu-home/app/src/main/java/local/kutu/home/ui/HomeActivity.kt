package local.kutu.home.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.util.DisplayMetrics
import local.kutu.home.R
import local.kutu.home.model.AppModel

class HomeActivity : Activity() {

    private lateinit var dockRecycler: RecyclerView
    private lateinit var shelfContainer: FrameLayout
    private lateinit var moveHintBanner: TextView
    private lateinit var chipsRow: LinearLayout
    private lateinit var modalOverlay: FrameLayout

    // Panels
    private lateinit var dialogMirror: LinearLayout
    private lateinit var dialogAllApps: LinearLayout
    private lateinit var dialogContextMenu: LinearLayout
    private lateinit var allAppsRecycler: RecyclerView
    private lateinit var mirrorStatusDot: View
    private lateinit var mirrorStatusText: TextView

    private val allAppsList = mutableListOf<AppModel>()
    private val dockAppsList = mutableListOf<AppModel>()
    private var dockAdapter: DockAdapter? = null
    private var allAppsAdapter: AllAppsAdapter? = null

    // Move mode state
    private var isMoveMode = false
    private var moveStartIndex = -1
    private var preMoveList = mutableListOf<AppModel>()

    // Long-press suppression flag to prevent click after long press
    private var longPressHandled = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshAllApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dockRecycler = findViewById(R.id.dock_recycler)
        shelfContainer = findViewById(R.id.shelf_container)
        moveHintBanner = findViewById(R.id.move_hint_banner)
        chipsRow = findViewById(R.id.chips_row)
        modalOverlay = findViewById(R.id.modal_overlay)

        dialogMirror = findViewById(R.id.dialog_mirror)
        dialogAllApps = findViewById(R.id.dialog_all_apps)
        dialogContextMenu = findViewById(R.id.dialog_context_menu)
        allAppsRecycler = findViewById(R.id.all_apps_recycler)
        mirrorStatusDot = findViewById(R.id.mirror_status_dot)
        mirrorStatusText = findViewById(R.id.mirror_status_text)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)

        setupChips()
        loadInstalledApps()
        loadDockApps()
        setupDockRecycler()
    }

    override fun onResume() {
        super.onResume()
        refreshAllApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageReceiver)
        } catch (_: Exception) {}
    }

    private fun refreshAllApps() {
        loadInstalledApps()
        allAppsAdapter?.notifyDataSetChanged()
        refreshDockApps()
    }

    private fun setupChips() {
        findViewById<TextView>(R.id.chip_mirror).setOnClickListener {
            openMirrorDialog()
        }

        findViewById<TextView>(R.id.chip_settings).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                // Fallback
            }
        }

        findViewById<TextView>(R.id.chip_all_apps).setOnClickListener {
            openAllAppsDialog()
        }

        findViewById<TextView>(R.id.btn_close_mirror_dialog).setOnClickListener {
            closeModals()
        }

        findViewById<TextView>(R.id.btn_start_mirror_service).setOnClickListener {
            try {
                val intent = Intent("local.kutu.mirror.ACTION_START").apply {
                    setPackage("local.kutu.mirror")
                }
                startActivity(intent)
            } catch (_: Exception) {}
            checkMirrorStatus()
        }
    }

    private fun loadInstalledApps() {
        allAppsList.clear()
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val activities = pm.queryIntentActivities(intent, 0)
        for (ri in activities) {
            val pkg = ri.activityInfo.packageName
            // Exclude self and helper utilities
            if (pkg == packageName || pkg == "local.kutu.mirror") continue

            val label = ri.loadLabel(pm).toString()
            val icon = ri.loadIcon(pm)
            val isSystem = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            allAppsList.add(AppModel(pkg, ri.activityInfo.name, label, icon, isSystem))
        }
        allAppsList.sortBy { it.label.lowercase() }
    }

    private fun loadDockApps() {
        dockAppsList.clear()
        val prefs = getSharedPreferences("kutu_home_prefs", Context.MODE_PRIVATE)
        val savedDockStr = prefs.getString("home/dock_v1", null)

        val targetPackages = if (savedDockStr.isNullOrEmpty()) {
            // Seed defaults
            listOf(
                "com.google.android.youtube.tv",
                "com.netflix.ninja",
                "com.disney.disneyplus",
                "com.amazon.amazonvideo.livingroom",
                "com.wbd.stream",
                "com.beonetv.tod",
                "com.google.android.youtube.tvmusic"
            )
        } else {
            savedDockStr.split(",").filter { it.isNotBlank() }
        }

        for (pkg in targetPackages) {
            val found = allAppsList.firstOrNull { it.packageName == pkg }
            if (found != null) {
                dockAppsList.add(found)
            }
        }

        // If still empty (e.g. initial setup without match), take first 7 installed apps
        if (dockAppsList.isEmpty() && allAppsList.isNotEmpty()) {
            dockAppsList.addAll(allAppsList.take(7))
        }

        saveDockApps()
    }

    private fun refreshDockApps() {
        val currentPkgs = dockAppsList.map { it.packageName }
        dockAppsList.clear()
        for (pkg in currentPkgs) {
            val found = allAppsList.firstOrNull { it.packageName == pkg }
            if (found != null) {
                dockAppsList.add(found)
            }
        }
        updateShelfWidth()
        dockAdapter?.notifyDataSetChanged()
    }

    private fun updateShelfWidth() {
        val shelfLp = shelfContainer.layoutParams ?: return
        val recyclerLp = dockRecycler.layoutParams ?: return
        if (dockAppsList.size > 7) {
            shelfLp.width = ViewGroup.LayoutParams.MATCH_PARENT
            recyclerLp.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            shelfLp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            recyclerLp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        shelfContainer.layoutParams = shelfLp
        dockRecycler.layoutParams = recyclerLp
    }

    private fun saveDockApps() {
        val prefs = getSharedPreferences("kutu_home_prefs", Context.MODE_PRIVATE)
        val str = dockAppsList.joinToString(",") { it.packageName }
        prefs.edit().putString("home/dock_v1", str).apply()
        updateShelfWidth()
    }

    private fun scrollToCenter(position: Int) {
        if (position !in dockAppsList.indices || dockAppsList.size <= 7) return
        val layoutManager = dockRecycler.layoutManager as? LinearLayoutManager ?: return
        val view = layoutManager.findViewByPosition(position)
        if (view != null) {
            val recyclerWidth = dockRecycler.width
            if (recyclerWidth > 0) {
                val viewLeft = view.left
                val viewRight = view.right
                // If the item is comfortably inside the center 60% of the screen, keep the row steady!
                val safeLeft = recyclerWidth * 0.20f
                val safeRight = recyclerWidth * 0.80f
                if (viewLeft >= safeLeft && viewRight <= safeRight) {
                    return // No jarring movement when navigating within view
                }
            }
        }

        try {
            val smoothScroller = object : LinearSmoothScroller(this) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    // Calmer, softer glide (default is 25f)
                    return 150f / displayMetrics.densityDpi
                }

                override fun calculateDtToFit(
                    viewStart: Int,
                    viewEnd: Int,
                    boxStart: Int,
                    boxEnd: Int,
                    snapPreference: Int
                ): Int {
                    return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
                }
            }
            smoothScroller.targetPosition = position
            layoutManager.startSmoothScroll(smoothScroller)
        } catch (_: Exception) {}
    }

    private fun setupDockRecycler() {
        updateShelfWidth()
        dockRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dockAdapter = DockAdapter(
            dockAppsList,
            onItemFocused = { _, pos ->
                scrollToCenter(pos)
            },
            onItemClick = { app, pos ->
                if (longPressHandled) {
                    longPressHandled = false
                    return@DockAdapter
                }
                if (isMoveMode) {
                    confirmMove()
                } else {
                    launchApp(app)
                }
            },
            onItemLongClick = { app, pos ->
                longPressHandled = true
                if (!isMoveMode) {
                    openContextMenuForDockApp(app, pos)
                }
            }
        )
        dockRecycler.adapter = dockAdapter
    }

    private fun launchApp(app: AppModel) {
        try {
            val intent = packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            // App launch failure handling
        }
    }

    // --- Move Mode ---
    private fun startMoveMode(position: Int) {
        isMoveMode = true
        moveStartIndex = position
        preMoveList = ArrayList(dockAppsList)
        dockAdapter?.movingPosition = position
        dockAdapter?.notifyItemChanged(position)
        moveHintBanner.visibility = View.VISIBLE
        dockRecycler.smoothScrollToPosition(position)
    }

    private fun confirmMove() {
        isMoveMode = false
        val pos = dockAdapter?.movingPosition ?: -1
        dockAdapter?.movingPosition = -1
        if (pos != -1) dockAdapter?.notifyItemChanged(pos)
        moveHintBanner.visibility = View.GONE
        saveDockApps()
    }

    private fun cancelMove() {
        isMoveMode = false
        dockAppsList.clear()
        dockAppsList.addAll(preMoveList)
        dockAdapter?.movingPosition = -1
        dockAdapter?.notifyDataSetChanged()
        moveHintBanner.visibility = View.GONE
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isMoveMode && event.action == KeyEvent.ACTION_DOWN) {
            val currentPos = dockAdapter?.movingPosition ?: -1
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (currentPos > 0) {
                        dockAdapter?.moveItem(currentPos, currentPos - 1)
                        dockRecycler.smoothScrollToPosition(currentPos - 1)
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (currentPos < dockAppsList.size - 1) {
                        dockAdapter?.moveItem(currentPos, currentPos + 1)
                        dockRecycler.smoothScrollToPosition(currentPos + 1)
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    confirmMove()
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    cancelMove()
                    return true
                }
            }
        }

        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (dialogContextMenu.visibility == View.VISIBLE) {
                hideContextMenuDialog()
                return true
            }
            if (modalOverlay.visibility == View.VISIBLE) {
                closeModals()
                return true
            }
            // Do not exit launcher on back button
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    // --- Modals & Context Menus ---
    private fun closeModals() {
        shelfContainer.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        chipsRow.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        modalOverlay.visibility = View.GONE
        dialogMirror.visibility = View.GONE
        dialogAllApps.visibility = View.GONE
        dialogContextMenu.visibility = View.GONE
        dockRecycler.requestFocus()
    }

    private fun hideContextMenuDialog() {
        dialogContextMenu.visibility = View.GONE
        if (dialogAllApps.visibility == View.VISIBLE) {
            allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
            allAppsRecycler.requestFocus()
        } else {
            closeModals()
        }
    }

    private fun openMirrorDialog() {
        shelfContainer.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        chipsRow.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        modalOverlay.visibility = View.VISIBLE
        dialogMirror.visibility = View.VISIBLE
        dialogAllApps.visibility = View.GONE
        dialogContextMenu.visibility = View.GONE
        checkMirrorStatus()
        findViewById<TextView>(R.id.btn_close_mirror_dialog).requestFocus()
    }

    private fun checkMirrorStatus() {
        val isInstalled = try {
            packageManager.getPackageInfo("local.kutu.mirror", 0)
            true
        } catch (_: Exception) {
            false
        }

        if (isInstalled) {
            mirrorStatusDot.setBackgroundResource(R.drawable.bg_chip_focused)
            mirrorStatusText.text = getString(R.string.mirror_status_ready)
            mirrorStatusText.setTextColor(getColor(R.color.status_green))
        } else {
            mirrorStatusDot.setBackgroundResource(R.drawable.bg_chip_normal)
            mirrorStatusText.text = "Kutu alıcısı kurulu değil"
            mirrorStatusText.setTextColor(getColor(R.color.status_orange))
        }
    }

    private fun openAllAppsDialog() {
        shelfContainer.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        chipsRow.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        modalOverlay.visibility = View.VISIBLE
        dialogMirror.visibility = View.GONE
        dialogAllApps.visibility = View.VISIBLE
        dialogContextMenu.visibility = View.GONE

        loadInstalledApps()
        allAppsRecycler.layoutManager = GridLayoutManager(this, 5)
        allAppsAdapter = AllAppsAdapter(
            allAppsList,
            onItemClick = { app ->
                launchApp(app)
                closeModals()
            },
            onItemLongClick = { app ->
                openContextMenuForAllApps(app)
            }
        )
        allAppsRecycler.adapter = allAppsAdapter
        allAppsRecycler.requestFocus()
    }

    private fun lockMenuFocus(container: LinearLayout) {
        val count = container.childCount
        if (count == 0) return
        for (i in 0 until count) {
            container.getChildAt(i).id = View.generateViewId()
        }
        for (i in 0 until count) {
            val child = container.getChildAt(i)
            val prevChild = if (i > 0) container.getChildAt(i - 1) else child
            val nextChild = if (i < count - 1) container.getChildAt(i + 1) else child
            child.nextFocusLeftId = child.id
            child.nextFocusRightId = child.id
            child.nextFocusUpId = prevChild.id
            child.nextFocusDownId = nextChild.id
        }
        container.post {
            container.getChildAt(0)?.requestFocus()
        }
    }

    private fun openContextMenuForDockApp(app: AppModel, position: Int) {
        shelfContainer.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        chipsRow.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        modalOverlay.visibility = View.VISIBLE
        dialogContextMenu.visibility = View.VISIBLE
        dialogMirror.visibility = View.GONE
        dialogAllApps.visibility = View.GONE

        findViewById<TextView>(R.id.context_menu_app_title).text = app.label
        val container = findViewById<LinearLayout>(R.id.context_menu_items_container)
        container.removeAllViews()

        // 1. Taşı
        addMenuItem(container, getString(R.string.menu_move)) {
            closeModals()
            startMoveMode(position)
        }

        // 2. Ana ekrandan kaldır
        addMenuItem(container, getString(R.string.menu_remove_dock)) {
            dockAppsList.removeAt(position)
            dockAdapter?.notifyItemRemoved(position)
            saveDockApps()
            closeModals()
        }

        // 3. Uygulama bilgisi
        addMenuItem(container, getString(R.string.menu_app_info)) {
            openAppInfo(app.packageName)
            hideContextMenuDialog()
        }

        // 4. Kaldır (eğer sistem uygulaması değilse)
        if (!app.isSystemApp) {
            addMenuItem(container, getString(R.string.menu_uninstall)) {
                uninstallApp(app.packageName)
                hideContextMenuDialog()
            }
        }

        lockMenuFocus(container)
    }

    private fun openContextMenuForAllApps(app: AppModel) {
        shelfContainer.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        chipsRow.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        allAppsRecycler.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        modalOverlay.visibility = View.VISIBLE
        dialogContextMenu.visibility = View.VISIBLE
        dialogMirror.visibility = View.GONE

        findViewById<TextView>(R.id.context_menu_app_title).text = app.label
        val container = findViewById<LinearLayout>(R.id.context_menu_items_container)
        container.removeAllViews()

        val isInDock = dockAppsList.any { it.packageName == app.packageName }
        if (isInDock) {
            addMenuItem(container, getString(R.string.menu_remove_dock)) {
                val idx = dockAppsList.indexOfFirst { it.packageName == app.packageName }
                if (idx != -1) {
                    dockAppsList.removeAt(idx)
                    dockAdapter?.notifyItemRemoved(idx)
                    saveDockApps()
                }
                hideContextMenuDialog()
            }
        } else {
            addMenuItem(container, getString(R.string.menu_add_dock)) {
                dockAppsList.add(app)
                dockAdapter?.notifyItemInserted(dockAppsList.size - 1)
                saveDockApps()
                hideContextMenuDialog()
            }
        }

        addMenuItem(container, getString(R.string.menu_app_info)) {
            openAppInfo(app.packageName)
            hideContextMenuDialog()
        }

        if (!app.isSystemApp) {
            addMenuItem(container, getString(R.string.menu_uninstall)) {
                uninstallApp(app.packageName)
                hideContextMenuDialog()
            }
        }

        lockMenuFocus(container)
    }

    private fun addMenuItem(container: LinearLayout, text: String, onClick: () -> Unit) {
        val tv = layoutInflater.inflate(R.layout.item_menu_option, container, false) as TextView
        tv.text = text
        tv.setOnClickListener { onClick() }
        container.addView(tv)
    }

    private fun openAppInfo(pkg: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun uninstallApp(pkg: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$pkg")
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }
}
