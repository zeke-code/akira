package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.ui.activities.MainActivity
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isUpdateAvailable = MutableLiveData<Boolean>()
    val isUpdateAvailable: LiveData<Boolean> get() = _isUpdateAvailable

    init {
        checkForUpdates("zeke-code", "akira")
    }

    fun isSetupComplete(): Boolean {
        return userRepository.isSetupComplete()
    }

    /**
     * Function to update budget if last time app was launched was the previous month.
     * If user entered a new month, refresh his budget by adding an entry to the database
     * with the new month.
     */
    fun checkAndUpdateBudget() {
        val currentYearMonth = getCurrentYearMonth()
        if (userRepository.getLastLaunchDate() != currentYearMonth) {
            viewModelScope.launch {
                val currentBudget = userRepository.getBudget().toDouble()
                financialRepository.insertBudget(
                    BudgetModel(
                        yearMonth = currentYearMonth,
                        amount = currentBudget
                    )
                )
                userRepository.setLastLaunchDateToNow()
            }
        }
    }

    /**
     * Function to check for updates and return the APK URL if an update is available
     */
    private fun checkForUpdates(owner: String, repo: String) {
        viewModelScope.launch {
            val latestRelease = userRepository.fetchLatestRelease(owner, repo)
            if (latestRelease == null) {
                Log.d("MainViewModel", "No releases found or error fetching latest release.")
                _isUpdateAvailable.postValue(false)
                return@launch
            }
            Log.d("MainViewModel", "Latest release response is: $latestRelease")
            latestRelease.let {
                Log.d("MainViewModel", "Tag name is: ${it.tagName}")
                val latestVersion = it.tagName.removePrefix("v")
                Log.d("MainViewModel", "Latest version is: $latestVersion")
                val currentVersion = userRepository.getAppVersionName()

                if (currentVersion.isNullOrEmpty()) {
                    Log.d("MainViewModel", "Can't retrieve current version...")
                    _isUpdateAvailable.postValue(false)
                    return@let
                }

                if (isNewerVersion(latestVersion, currentVersion)) {
                    _isUpdateAvailable.postValue(true)
                } else {
                    _isUpdateAvailable.postValue(false)
                }
            }
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        val latestParts = latestVersion.split(".")
        val currentParts = currentVersion.split(".")

        for (i in latestParts.indices) {
            val latestPart = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (latestPart > currentPart) {
                return true
            } else if (latestPart < currentPart) {
                return false
            }
        }
        return false
    }

    fun downloadAndInstallApk(context: Context, owner: String, repo: String) {
        val applicationContext = context.applicationContext

        cleanupOldUpdateFiles()

        if (!applicationContext.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:${applicationContext.packageName}"))
            if (context is Activity) {
                context.startActivityForResult(intent, MainActivity.REQUEST_INSTALL_PERMISSION)
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestRelease = userRepository.fetchLatestRelease(owner, repo)
                if (latestRelease != null) {
                    val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                    apkAsset?.let { asset ->
                        val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                        val fileName = "akira_update_${System.currentTimeMillis()}.apk"
                        val uri = Uri.parse(asset.browserDownloadUrl)

                        val request = DownloadManager.Request(uri).apply {
                            setTitle("Akira Update")
                            setDescription("Downloading new version...")
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            setAllowedOverRoaming(true)
                            setAllowedOverMetered(true)
                        }

                        val downloadId = downloadManager.enqueue(request)

                        val onComplete = object : BroadcastReceiver() {
                            override fun onReceive(ctxt: Context, intent: Intent) {
                                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        try {
                                            val downloadedFile = File(
                                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                                fileName
                                            )

                                            Log.d("MainViewModel", "File exists: ${downloadedFile.exists()}")
                                            Log.d("MainViewModel", "File path: ${downloadedFile.absolutePath}")

                                            val query = DownloadManager.Query().apply {
                                                setFilterById(downloadId)
                                            }
                                            val cursor = downloadManager.query(query)

                                            if (cursor.moveToFirst()) {
                                                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                                val status = cursor.getInt(columnIndex)

                                                when (status) {
                                                    DownloadManager.STATUS_SUCCESSFUL -> {
                                                        if (downloadedFile.exists()) {
                                                            val apkUri = FileProvider.getUriForFile(
                                                                applicationContext,
                                                                "${applicationContext.packageName}.provider",
                                                                downloadedFile
                                                            )

                                                            Log.d("MainViewModel", "APK URI: $apkUri")

                                                            val install = Intent(Intent.ACTION_VIEW).apply {
                                                                setDataAndType(apkUri, "application/vnd.android.package-archive")
                                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                                        Intent.FLAG_ACTIVITY_NO_HISTORY
                                                                addCategory(Intent.CATEGORY_DEFAULT)
                                                            }

                                                            Log.d("MainViewModel", "Starting installation intent")
                                                            applicationContext.startActivity(install)
                                                        } else {
                                                            Log.e("MainViewModel", "Downloaded file not found")
                                                        }
                                                    }
                                                    DownloadManager.STATUS_FAILED -> {
                                                        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                                        val reason = cursor.getInt(columnReason)
                                                        Log.e("MainViewModel", "Download failed: $reason")
                                                    }
                                                }
                                            }
                                            cursor.close()

                                        } catch (e: Exception) {
                                            Log.e("MainViewModel", "Error installing APK", e)
                                            e.printStackTrace()
                                        }
                                    }
                                    applicationContext.unregisterReceiver(this)
                                }
                            }
                        }

                        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

                        // Register receiver using ContextCompat for all Android versions
                        ContextCompat.registerReceiver(
                            applicationContext,
                            onComplete,
                            intentFilter,
                            ContextCompat.RECEIVER_NOT_EXPORTED
                        )
                    }
                } else {
                    Log.e("MainViewModel", "No release found or release data is null")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error downloading APK", e)
                e.printStackTrace()
            }
        }
    }

    private fun cleanupOldUpdateFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("akira_update_") && file.name.endsWith(".apk")) {
                        file.delete()
                        Log.d("MainViewModel", "Deleted old update file: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error cleaning up old update files", e)
            }
        }
    }
}