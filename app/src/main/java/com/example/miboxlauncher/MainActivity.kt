package com.example.miboxlauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }
    private val navy = Color.rgb(12, 20, 36)
    private val panel = Color.rgb(28, 41, 62)
    private val accent = Color.rgb(71, 166, 255)
    private lateinit var root: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var clock: TextView? = null
    private var manage = false
    private data class App(val pkg: String, val title: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        render()
        handler.post(object : Runnable {
            override fun run() {
                clock?.text = SimpleDateFormat("HH:mm  •  d MMMM EEEE", Locale("tr", "TR")).format(Date())
                handler.postDelayed(this, 30000)
            }
        })
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun bg(color: Int, radius: Int = 18) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun text(value: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        this.text = value; textSize = size; setTextColor(Color.WHITE)
        if (bold) typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER_VERTICAL
    }
    private fun button(value: String, action: () -> Unit) = text(value, 17f, true).apply {
        gravity = Gravity.CENTER; background = bg(panel, 12)
        isFocusable = true; isFocusableInTouchMode = false
        setPadding(dp(18), dp(10), dp(18), dp(10))
        setOnClickListener { action() }
        setOnFocusChangeListener { _, focused ->
            background = bg(if (focused) accent else panel, 12)
            scaleX = if (focused) 1.05f else 1f
            scaleY = if (focused) 1.05f else 1f
        }
    }
    private fun apps(): List<App> {
        val pm = packageManager
        val intents = listOf(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER))
        val found = linkedMapOf<String, App>()
        intents.forEach { intent ->
            pm.queryIntentActivities(intent, 0).forEach { item ->
                val pkg = item.activityInfo.packageName
                if (pkg != packageName && !found.containsKey(pkg)) found[pkg] = App(pkg, item.loadLabel(pm).toString())
            }
        }
        val order = prefs.getString("order", "")!!.split("|").filter { it.isNotBlank() }
        return found.values.sortedWith(compareBy<App> { val n = order.indexOf(it.pkg); if (n < 0) Int.MAX_VALUE else n }.thenBy { it.title.lowercase(Locale.ROOT) })
    }
    private fun hidden(): Set<String> = prefs.getStringSet("hidden", emptySet())!!.toSet()
    private fun persistOrder(items: List<App>) = prefs.edit().putString("order", items.joinToString("|") { it.pkg }).apply()

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(42), dp(24), dp(42), dp(20))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(navy, Color.rgb(21, 42, 70), navy))
        }
        setContentView(root)
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(text(if (manage) "Uygulamaları Düzenle" else "Mi Box Modern", 29f, true), LinearLayout.LayoutParams(0, dp(52), 1f))
        if (!manage) {
            clock = text("", 17f)
            header.addView(clock)
        } else clock = null
        root.addView(header)
        root.addView(text(if (manage) "OK: işlemler  •  Sıralama ve gizleme bu menüden yapılır" else "Favori uygulamaların, tam istediğin düzende.", 15f).apply { setTextColor(Color.LTGRAY) })
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(20), 0, dp(18)) }
        fun addControl(label: String, action: () -> Unit) {
            controls.addView(button(label, action), LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)).apply { marginEnd = dp(14) })
        }
        addControl(if (manage) "← Ana Ekran" else "⚙ Düzenle") { manage = !manage; render() }
        addControl("Android Ayarları") { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        if (!manage) addControl("Tüm Uygulamalar") { manage = true; render() }
        root.addView(controls)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val grid = GridLayout(this).apply { columnCount = 5; alignmentMode = GridLayout.ALIGN_BOUNDS }
        val entries = apps().filter { manage || it.pkg !in hidden() }
        if (entries.isEmpty()) grid.addView(text("Gösterilecek uygulama bulunamadı. Düzenle menüsünü açın.", 18f))
        entries.forEach { app ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(10))
                background = bg(panel)
                isFocusable = true
                setOnFocusChangeListener { _, focused ->
                    background = bg(if (focused) accent else panel)
                    scaleX = if (focused) 1.06f else 1f; scaleY = if (focused) 1.06f else 1f
                }
                setOnClickListener { if (manage) showOptions(app) else launch(app.pkg) }
            }
            try {
                val icon = ImageView(this).apply {
                    setImageDrawable(packageManager.getApplicationIcon(app.pkg))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                tile.addView(icon, LinearLayout.LayoutParams(dp(62), dp(62)))
            } catch (_: PackageManager.NameNotFoundException) { }
            val title = text(app.title, 15f, true).apply { gravity = Gravity.CENTER; maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END }
            tile.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)))
            if (manage && app.pkg in hidden()) tile.alpha = 0.45f
            grid.addView(tile, GridLayout.LayoutParams().apply {
                width = 0; height = dp(142)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            })
        }
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(text("OK: Aç  •  Yön tuşları: Gezin  •  Düzenle: Gizle / Taşı / Kaldır", 13f).apply { setTextColor(Color.LTGRAY) })
        root.post { (root.findFocus() ?: root).let { if (it == root) controls.getChildAt(0)?.requestFocus() } }
    }

    private fun launch(pkg: String) {
        val intent = packageManager.getLeanbackLaunchIntentForPackage(pkg) ?: packageManager.getLaunchIntentForPackage(pkg)
        if (intent != null) try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this, "Uygulama açılamadı", Toast.LENGTH_SHORT).show() }
        else Toast.makeText(this, "Başlatıcı bulunamadı", Toast.LENGTH_SHORT).show()
    }
    private fun showOptions(app: App) {
        val items = apps()
        val isHidden = app.pkg in hidden()
        val choices = arrayOf(if (isHidden) "Ana ekranda göster" else "Ana ekrandan gizle", "Sola taşı", "Sağa taşı", "Uygulamayı aç", "Kaldırma ekranını aç", "Uygulama bilgisi")
        AlertDialog.Builder(this).setTitle(app.title).setItems(choices) { _, which ->
            when (which) {
                0 -> { val next = hidden().toMutableSet(); if (isHidden) next.remove(app.pkg) else next.add(app.pkg); prefs.edit().putStringSet("hidden", next).apply(); render() }
                1, 2 -> {
                    val reordered = items.toMutableList(); val from = reordered.indexOfFirst { it.pkg == app.pkg }
                    val to = from + if (which == 1) -1 else 1
                    if (from >= 0 && to in reordered.indices) { java.util.Collections.swap(reordered, from, to); persistOrder(reordered) }
                    render()
                }
                3 -> launch(app.pkg)
                4 -> try { startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.pkg}"))) } catch (_: Exception) { appInfo(app.pkg) }
                5 -> appInfo(app.pkg)
            }
        }.show()
    }
    private fun appInfo(pkg: String) {
        try { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))) }
        catch (_: Exception) { Toast.makeText(this, "Ayarlar açılamadı", Toast.LENGTH_SHORT).show() }
    }
    override fun onResume() { super.onResume(); if (::root.isInitialized) render() }
    override fun onBackPressed() { if (manage) { manage = false; render() } else super.onBackPressed() }
}
