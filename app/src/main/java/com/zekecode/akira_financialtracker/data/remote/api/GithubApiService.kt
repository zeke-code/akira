package com.zekecode.akira_financialtracker.data.remote.api

import com.zekecode.akira_financialtracker.data.remote.models.GitHubRelease
import retrofit2.http.GET

interface GithubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
