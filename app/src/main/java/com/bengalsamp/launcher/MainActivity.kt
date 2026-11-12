package com.bengalsamp.launcher

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.Locale

/**
 * Bengal Samp — একটাই স্ক্রিন।
 * ১. নাম লেখো → ২. UPDATE চাপো (গেম ফাইল ডাউনলোড) → ৩. PLAY চাপো (সরাসরি সার্ভারে ঢুকবে)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var downloadManager: DownloadManager

    private lateinit var etNickname: EditText
    private lateinit var spinnerServers: Spinner
    private lateinit var tvServerIp: TextView
    private lateinit var tvServerPlayers: TextView
    private lateinit var tvServerMode: TextView
    private lateinit var tvSampleWarning: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnCancelDownload: Button
    private lateinit var progressDownload: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvDownloadInfo: TextView
    private lateinit var tvGameFileStatus: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnCopyIp: Button
    private lateinit var etCustomName: EditText
    private lateinit var etCustomIp: EditText
    private lateinit var btnAddServer: Button
    private lateinit var btnDiscord: Button
    private lateinit var tvStatus: TextView

    private val servers = mutableListOf<Server>()
    private var selectedServer: Server? = null
    private var spinnerAdapter: ArrayAdapter<String>? = null

    private var downloadId: Long = -1L
    private var isDownloading = false
    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (isDownloading) {
                updateProgress()
                handler.postDelayed(this, 500)
            }
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != downloadId) return
            val status = queryDownloadStatus()
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                onDownloadFinished()
            } else if (status == DownloadManager.STATUS_FAILED) {
                onDownloadFailed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("bengal_samp_prefs", Context.MODE_PRIVATE)
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        etNickname = findViewById(R.id.etNickname)
        spinnerServers = findViewById(R.id.spinnerServers)
        tvServerIp = findViewById(R.id.tvServerIp)
        tvServerPlayers = findViewById(R.id.tvServerPlayers)
        tvServerMode = findViewById(R.id.tvServerMode)
        tvSampleWarning = findViewById(R.id.tvSampleWarning)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnCancelDownload = findViewById(R.id.btnCancelDownload)
        progressDownload = findViewById(R.id.progressDownload)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvDownloadInfo = findViewById(R.id.tvDownloadInfo)
        tvGameFileStatus = findViewById(R.id.tvGameFileStatus)
        btnPlay = findViewById(R.id.btnPlay)
        btnCopyIp = findViewById(R.id.btnCopyIp)
        etCustomName = findViewById(R.id.etCustomName)
        etCustomIp = findViewById(R.id.etCustomIp)
        btnAddServer = findViewById(R.id.btnAddServer)
        btnDiscord = findViewById(R.id.btnDiscord)
        tvStatus = findViewById(R.id.tvStatus)

        // আগের নাম থাকলে বসিয়ে দাও।
        etNickname.setText(prefs.getString("nickname", ""))

        loadServers()
        setupServerSpinner()
        setupButtons()
        registerDownloadReceiver()

        // আগের ডাউনলোড চলছিল কি না দেখো, আর ফাইল আগেই আছে কি না দেখো।
        restoreDownloadIfRunning()
        refreshGameFileState()
    }

    override fun onPause() {
        super.onPause()
        saveNickname()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
        try {
            unregisterReceiver(downloadReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver আগেই বন্ধ হয়ে গেছে — সমস্যা নেই।
        }
    }

    // registerReceiver-এর ৩-আর্গুমেন্ট ভার্সন শুধু Android 13+ (API 33+) এ
    // চলে — পুরনো ফোনে (minSdk 24) ২-আর্গুমেন্ট ভার্সন ব্যবহার করো।
    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(downloadReceiver, filter)
        }
    }

    // ------------------------------------------------------------------
    // সার্ভার তালিকা
    // ------------------------------------------------------------------

    private fun loadServers() {
        servers.clear()
        servers.add(
            Server(
                name = Config.DEFAULT_SERVER_NAME,
                ip = Config.DEFAULT_SERVER_IP,
                players = Config.DEFAULT_SERVER_PLAYERS,
                mode = Config.DEFAULT_SERVER_MODE,
                isSample = true
            )
        )
        // নিজে যোগ করা সার্ভারগুলো ফিরিয়ে আনো।
        val saved = prefs.getStringSet("custom_servers", emptySet()) ?: emptySet()
        for (entry in saved) {
            val parts = entry.split("|")
            if (parts.size >= 2) {
                servers.add(Server(name = parts[0], ip = parts[1], players = "—", mode = "—"))
            }
        }
    }

    private fun setupServerSpinner() {
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            servers.map { it.name }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerServers.adapter = spinnerAdapter
        spinnerServers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedServer = servers[position]
                showSelectedServer()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        if (servers.isNotEmpty()) {
            selectedServer = servers[0]
            showSelectedServer()
        }
    }

    private fun refreshServerSpinner() {
        spinnerAdapter?.clear()
        spinnerAdapter?.addAll(servers.map { it.name })
        spinnerAdapter?.notifyDataSetChanged()
    }

    private fun showSelectedServer() {
        val server = selectedServer ?: return
        tvServerIp.text = server.ip
        tvServerPlayers.text = server.players
        tvServerMode.text = server.mode
        tvSampleWarning.visibility = if (server.isSample) View.VISIBLE else View.GONE
    }

    private fun setupButtons() {
        btnUpdate.setOnClickListener { startDownload() }
        btnCancelDownload.setOnClickListener { cancelDownload() }
        btnPlay.setOnClickListener { onPlayClicked() }
        btnCopyIp.setOnClickListener { copyServerIp() }
        btnAddServer.setOnClickListener { addCustomServer() }
        btnDiscord.setOnClickListener { openUrl(Config.DISCORD_URL) }
    }

    // ------------------------------------------------------------------
    // UPDATE — গেম ফাইল ডাউনলোড
    // ------------------------------------------------------------------

    private fun gameFile(): File {
        return File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            Config.GAME_FILE_NAME
        )
    }

    private fun startDownload() {
        // ১. আসল লিংক বসানো হয়েছে কি না দেখো।
        if (Config.GAME_FILES_URL.contains("USERNAME/REPO")) {
            AlertDialog.Builder(this)
                .setTitle(R.string.error_url_not_set)
                .setMessage(R.string.error_url_not_set_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }
        // ২. ইন্টারনেট আছে কি না দেখো।
        if (!isOnline()) {
            tvStatus.text = getString(R.string.error_no_internet)
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_LONG).show()
            return
        }

        val request = DownloadManager.Request(Uri.parse(Config.GAME_FILES_URL))
            .setTitle(getString(R.string.app_name))
            .setDescription(getString(R.string.download_description))
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(
                this,
                Environment.DIRECTORY_DOWNLOADS,
                Config.GAME_FILE_NAME
            )

        downloadId = downloadManager.enqueue(request)
        prefs.edit().putLong("download_id", downloadId).apply()
        isDownloading = true
        btnUpdate.isEnabled = false
        btnUpdate.text = getString(R.string.update_button_downloading)
        btnCancelDownload.visibility = View.VISIBLE
        progressDownload.visibility = View.VISIBLE
        progressDownload.progress = 0
        tvProgressPercent.text = "0%"
        tvDownloadInfo.text = ""
        tvStatus.text = getString(R.string.status_downloading)
        handler.post(progressRunnable)
    }

    private fun updateProgress() {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor.use {
            if (!it.moveToFirst()) return
            val bytesIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (bytesIndex == -1 || totalIndex == -1 || statusIndex == -1) return
            val downloaded = it.getLong(bytesIndex)
            val total = it.getLong(totalIndex)
            val status = it.getInt(statusIndex)
            if (status == DownloadManager.STATUS_FAILED) {
                onDownloadFailed()
                return
            }
            if (total > 0) {
                val percent = ((downloaded * 100) / total).toInt()
                progressDownload.progress = percent
                tvProgressPercent.text = "$percent%"
                tvDownloadInfo.text = "${formatMb(downloaded)} / ${formatMb(total)}"
            } else {
                tvDownloadInfo.text = formatMb(downloaded)
            }
        }
    }

    private fun queryDownloadStatus(): Int {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor.use {
            if (!it.moveToFirst()) return DownloadManager.STATUS_FAILED
            val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex == -1) return DownloadManager.STATUS_FAILED
            return it.getInt(statusIndex)
        }
    }

    private fun onDownloadFinished() {
        isDownloading = false
        handler.removeCallbacks(progressRunnable)
        val file = gameFile()
        if (file.exists() && file.length() > 0) {
            progressDownload.progress = 100
            tvProgressPercent.text = "100%"
            tvDownloadInfo.text = formatMb(file.length())
            tvGameFileStatus.text = getString(
                R.string.game_file_ready,
                formatMb(file.length())
            )
            tvStatus.text = getString(R.string.status_downloaded)
            btnPlay.isEnabled = true
            btnUpdate.isEnabled = true
            btnUpdate.text = getString(R.string.update_button_retry)
            btnCancelDownload.visibility = View.GONE
            Toast.makeText(this, R.string.status_downloaded, Toast.LENGTH_SHORT).show()
        } else {
            onDownloadFailed()
        }
    }

    private fun onDownloadFailed() {
        isDownloading = false
        handler.removeCallbacks(progressRunnable)
        downloadId = -1L
        prefs.edit().remove("download_id").apply()
        progressDownload.progress = 0
        tvProgressPercent.text = "0%"
        tvStatus.text = getString(R.string.error_download_failed)
        btnUpdate.isEnabled = true
        btnUpdate.text = getString(R.string.update_button_retry)
        btnCancelDownload.visibility = View.GONE
        Toast.makeText(this, R.string.error_download_failed, Toast.LENGTH_LONG).show()
    }

    private fun cancelDownload() {
        if (downloadId != -1L) {
            downloadManager.remove(downloadId)
            downloadId = -1L
            prefs.edit().remove("download_id").apply()
        }
        // ডাউনলোড বাতিল মানেই ফাইলটা অসম্পূর্ণ — মুছে দাও, যাতে
        // ভাঙা ফাইল দিয়ে PLAY চালু না হয়।
        val file = gameFile()
        if (file.exists()) {
            file.delete()
        }
        isDownloading = false
        handler.removeCallbacks(progressRunnable)
        progressDownload.progress = 0
        progressDownload.visibility = View.GONE
        tvProgressPercent.text = ""
        tvDownloadInfo.text = ""
        tvStatus.text = getString(R.string.status_no_file)
        btnUpdate.isEnabled = true
        btnUpdate.text = getString(R.string.update_button)
        btnCancelDownload.visibility = View.GONE
        refreshGameFileState()
    }

    private fun restoreDownloadIfRunning() {
        val savedId = prefs.getLong("download_id", -1L)
        if (savedId == -1L) return
        downloadId = savedId
        val status = queryDownloadStatus()
        if (status == DownloadManager.STATUS_RUNNING ||
            status == DownloadManager.STATUS_PENDING ||
            status == DownloadManager.STATUS_PAUSED
        ) {
            isDownloading = true
            btnUpdate.isEnabled = false
            btnUpdate.text = getString(R.string.update_button_downloading)
            btnCancelDownload.visibility = View.VISIBLE
            progressDownload.visibility = View.VISIBLE
            tvStatus.text = getString(R.string.status_downloading)
            handler.post(progressRunnable)
        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            onDownloadFinished()
        } else {
            downloadId = -1L
            prefs.edit().remove("download_id").apply()
        }
    }

    private fun isGameFileReady(): Boolean {
        val file = gameFile()
        return file.exists() && file.length() > 0
    }

    private fun refreshGameFileState() {
        if (isGameFileReady()) {
            val file = gameFile()
            tvGameFileStatus.text = getString(
                R.string.game_file_ready,
                formatMb(file.length())
            )
            tvStatus.text = getString(R.string.status_downloaded)
            btnPlay.isEnabled = true
            btnUpdate.text = getString(R.string.update_button_retry)
        } else {
            tvGameFileStatus.text = getString(R.string.status_no_file)
            tvStatus.text = getString(R.string.status_ready)
            btnPlay.isEnabled = false
        }
    }

    // ------------------------------------------------------------------
    // PLAY — ReSA খোলো
    // ------------------------------------------------------------------

    private fun onPlayClicked() {
        val nickname = etNickname.text.toString().trim()
        if (nickname.isEmpty()) {
            Toast.makeText(this, R.string.toast_nickname_needed, Toast.LENGTH_SHORT).show()
            etNickname.requestFocus()
            return
        }
        saveNickname()
        if (!isGameFileReady()) {
            tvStatus.text = getString(R.string.status_no_file)
            Toast.makeText(this, R.string.status_no_file, Toast.LENGTH_LONG).show()
            return
        }

        val server = selectedServer ?: return

        // সার্ভারের IP কপি করে রাখো (গেম অ্যাপে লাগতে পারে)।
        copyServerIp(silent = true)

        // সরাসরি সার্ভারে ঢোকার চেষ্টা করো — সফল হলে আর কিছু করতে হবে না।
        if (launchGameDirectly(server, nickname)) {
            Toast.makeText(this, R.string.toast_opening_game, Toast.LENGTH_SHORT).show()
            return
        }

        // গেম অ্যাপ নেই — এক চাপে ডাউনলোডের বাটন দেখাও।
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_game_app_title)
            .setMessage(R.string.dialog_game_app_message)
            .setPositiveButton(R.string.dialog_download_game_app) { _, _ ->
                openUrl(Config.RESA_WEBSITE_URL)
            }
            .setCancelable(true)
            .show()
    }

    /**
     * গেম অ্যাপ সরাসরি বেছে নেওয়া সার্ভারে ঢোকানোর চেষ্টা করে।
     * সফল হলে true — ব্যবহারকারীকে আর কিছু করতে হবে না।
     */
    private fun launchGameDirectly(server: Server, nickname: String): Boolean {
        val ip = server.ip
        // ১. SA-MP standard join লিংক (samp://IP:port) — যে SAMP
        // ক্লায়েন্ট এই লিংক ধরে, সে সরাসরি সার্ভারে ঢুকিয়ে দেবে।
        for (template in Config.PLAY_LINK_TEMPLATES) {
            val link = template
                .replace("{IP}", ip)
                .replace("{NICKNAME}", Uri.encode(nickname))
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    return true
                }
            } catch (_: Exception) {
                // এই লিংকটা কাজ করেনি — পরের উপায় চেষ্টা করো।
            }
        }
        // ২. ইনস্টল করা ক্লায়েন্ট অ্যাপটা সার্ভার+নাম সহ খুলে দাও।
        for (packageName in Config.GAME_CLIENT_PACKAGES) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                ?: continue
            launchIntent.putExtra("server", ip)
            launchIntent.putExtra("nickname", nickname)
            launchIntent.putExtra("ip", ip)
            launchIntent.putExtra("address", ip)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(launchIntent)
                return true
            } catch (_: Exception) {
                // পরের প্যাকেজ চেষ্টা করো।
            }
        }
        return false
    }

    // ------------------------------------------------------------------
    // নিজের সার্ভার যোগ করো + IP কপি + লিংক
    // ------------------------------------------------------------------

    private fun addCustomServer() {
        val ip = etCustomIp.text.toString().trim()
        val name = etCustomName.text.toString().trim().ifEmpty { "আমার সার্ভার" }
        if (ip.isEmpty() || !ip.contains(":")) {
            Toast.makeText(this, R.string.toast_invalid_ip, Toast.LENGTH_SHORT).show()
            etCustomIp.requestFocus()
            return
        }
        val server = Server(name = name, ip = ip, players = "—", mode = "—")
        servers.add(server)

        val saved = prefs.getStringSet("custom_servers", emptySet())?.toMutableSet()
            ?: mutableSetOf()
        saved.add("$name|$ip")
        prefs.edit().putStringSet("custom_servers", saved).apply()

        refreshServerSpinner()
        spinnerServers.setSelection(servers.size - 1)
        etCustomName.text.clear()
        etCustomIp.text.clear()
        Toast.makeText(this, R.string.toast_server_added, Toast.LENGTH_SHORT).show()
    }

    private fun copyServerIp(silent: Boolean = false) {
        val server = selectedServer ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("server_ip", server.ip))
        if (!silent) {
            Toast.makeText(this, R.string.toast_ip_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // ------------------------------------------------------------------
    // ছোট সাহায্যকারী
    // ------------------------------------------------------------------

    private fun saveNickname() {
        prefs.edit()
            .putString("nickname", etNickname.text.toString().trim())
            .apply()
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun formatMb(bytes: Long): String {
        val mb = bytes.toDouble() / 1024.0 / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
