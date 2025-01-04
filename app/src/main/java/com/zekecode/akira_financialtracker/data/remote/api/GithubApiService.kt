package com.zekecode.akira_financialtracker.data.remote.api

import com.zekecode.akira_financialtracker.data.remote.models.GitHubRelease
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}