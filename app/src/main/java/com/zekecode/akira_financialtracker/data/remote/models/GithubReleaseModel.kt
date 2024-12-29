package com.zekecode.akira_financialtracker.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "tag_name")
    val tagName: String,

    @Json(name = "assets")
    val assets: List<GitHubAsset>
)

@JsonClass(generateAdapter = true)
data class GitHubAsset(
    @Json(name = "name")
    val name: String,

    @Json(name = "browser_download_url")
    val browserDownloadUrl: String
)
