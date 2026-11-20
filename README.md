# WALLORA Native Android Preview

This is a separate native Android/Kotlin + Jetpack Compose preview project. It does **not** replace the current `~/WALLORA` Capacitor app.

## Implemented preview features
- AMOLED black + solid yellow theme
- Wallpaper grid, categories, collections, search, Trending, Fresh Drops
- Favorites stored with DataStore
- Recently viewed order stored with DataStore
- Downloads initiated through Android DownloadManager and recorded in the Downloads tab
- Full-screen wallpaper preview with live time/date overlay
- Home-screen, lock-screen, or both wallpaper setting through Android WallpaperManager
- Compose animation/press feedback


## Build
Requires Android SDK Platform 35, Build Tools 35.0.0, Java 17, and Gradle 8.9.

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

The temporary package ID is `com.kifs.wallora.nativepreview` so it can be installed beside the current WALLORA build. It is a migration/test package, not the final production package.


## Important limitations
- Catalog is still the current eight-item sample; it is not a real store backend or large curated catalog.
- Images use remote Unsplash URLs and need internet connectivity.
- Download history currently records initiated downloads; Android DownloadManager handles the actual transfer and notification.
- The app has not yet been compiled in this environment because Android SDK and Gradle are unavailable here. Use the included GitHub Actions workflow to build a test APK.
