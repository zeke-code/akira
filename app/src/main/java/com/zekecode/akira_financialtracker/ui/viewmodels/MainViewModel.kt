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
        // Then check for updates
        checkForUpdates("zeke-code", "akira")
    }

    fun isSetupComplete(): Boolean {
        return userRepository.isSetupComplete()
    }

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

    private fun checkForUpdates(owner: String, repo: String) {
        viewModelScope.launch {
            val latestRelease = userRepository.fetchLatestRelease(owner, repo)
            if (latestRelease == null) {
                Log.d("MainViewModel", "No releases found or error fetching latest release.")
                _isUpdateAvailable.postValue(false)
                return@launch
            }
            val latestVersion = latestRelease.tagName.removePrefix("v")
            val currentVersion = userRepository.getAppVersionName()

            if (currentVersion.isNullOrEmpty()) {
                _isUpdateAvailable.postValue(false)
                return@launch
            }

            _isUpdateAvailable.postValue(isNewerVersion(latestVersion, currentVersion))
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        val latestParts = latestVersion.split(".")
        val currentParts = currentVersion.split(".")

        for (i in latestParts.indices) {
            val latestPart = latestParts[i].toIntOrNull() ?: 0
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

        cleanupOldUpdateFiles(applicationContext)

        if (!applicationContext.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${applicationContext.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (context is Activity) {
                context.startActivityForResult(intent, MainActivity.REQUEST_INSTALL_PERMISSION)
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestRelease = userRepository.fetchLatestRelease(owner, repo)
                latestRelease?.assets?.find { it.name.endsWith(".apk") }?.let { asset ->
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
                                        val query = DownloadManager.Query().apply { setFilterById(downloadId) }
                                        downloadManager.query(query).use { cursor ->
                                            if (cursor.moveToFirst()) {
                                                val status = cursor.getInt(
                                                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                                                )

                                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                                    val downloadedFile = File(
                                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                                        fileName
                                                    )
                                                    Log.d("MainViewModel", "Download completed: ${downloadedFile.exists()}")
                                                } else if (status == DownloadManager.STATUS_FAILED) {
                                                    val reason = cursor.getInt(
                                                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)
                                                    )
                                                    Log.e("MainViewModel", "Download failed with reason: $reason")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("MainViewModel", "Error in download completion handler", e)
                                    }
                                }

                                try {
                                    applicationContext.unregisterReceiver(this)
                                } catch (e: Exception) {
                                    Log.e("MainViewModel", "Error unregistering receiver", e)
                                }
                            }
                        }
                    }

                    val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                    ContextCompat.registerReceiver(
                        applicationContext,
                        onComplete,
                        intentFilter,
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                } ?: run {
                    Log.e("MainViewModel", "No APK asset found in the release")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error downloading APK", e)
            }
        }
    }

    /**
     * Cleans up old update files from the public Downloads directory
     */
    private fun cleanupOldUpdateFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.listFiles()?.filter { file ->
                    file.name.startsWith("akira_update_") && file.name.endsWith(".apk")
                }?.forEach { file ->
                    if (file.delete()) {
                        Log.d("MainViewModel", "Deleted old update file: ${file.name}")
                    } else {
                        Log.e("MainViewModel", "Failed to delete file: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error cleaning up old update files", e)
                e.printStackTrace()
            }
        }
    }
}
