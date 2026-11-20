package com.kifs.wallora.studio.data

/**
 * WALLORA STUDIO upload draft.
 *
 * The wallpaper is the original clean asset.
 * The phone preview is a separate optional asset and must never be
 * substituted for the downloadable wallpaper.
 */
data class WallpaperUploadDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val category: String = "Wallpaper",
    val wallpaperUri: String? = null,
    val previewUri: String? = null,
    val tags: List<String> = emptyList(),
    val wallpaperType: WallpaperType = WallpaperType.IMAGE
) {
    val hasWallpaper: Boolean
        get() = !wallpaperUri.isNullOrBlank()

    val hasPreview: Boolean
        get() = !previewUri.isNullOrBlank()

    val canPublish: Boolean
        get() = title.isNotBlank() && hasWallpaper

    /** The original artwork is always the download source. */
    val downloadUri: String?
        get() = wallpaperUri
}

enum class WallpaperType {
    IMAGE,
    VIDEO
}
