package com.zekecode.akira_financialtracker.ui.viewmodels

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zekecode.akira_financialtracker.data.local.entities.BudgetModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.UserRepository
import com.zekecode.akira_financialtracker.utils.DateUtils.getCurrentYearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            Log.d("MainViewModel", "Latest release response is: ${latestRelease.toString()}")
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestRelease = userRepository.fetchLatestRelease(owner, repo)
                if (latestRelease != null) {
                    val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
                    apkAsset?.let { asset ->
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val uri = Uri.parse(asset.browserDownloadUrl)
                        val request = DownloadManager.Request(uri).apply {
                            setTitle("Downloading update")
                            setDescription("Downloading new version of the app")
                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, asset.name)
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setAllowedOverMetered(true)
                            setAllowedOverRoaming(true)
                        }

                        val downloadId = downloadManager.enqueue(request)

                        var downloading = true
                        while (downloading) {
                            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                            if (cursor.moveToFirst()) {
                                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                if (statusIndex != -1) {
                                    val status = cursor.getInt(statusIndex)
                                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                        downloading = false
                                        installApk(context, downloadManager.getUriForDownloadedFile(downloadId))
                                    }
                                }
                            }
                            cursor.close()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun installApk(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}