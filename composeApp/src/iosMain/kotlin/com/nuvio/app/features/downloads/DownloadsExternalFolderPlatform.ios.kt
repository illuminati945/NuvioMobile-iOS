package com.nuvio.app.features.downloads

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
internal actual object DownloadsExternalFolderPlatform {
    private const val defaultFolderLabel = "Files app > On My iPhone > Nuvio Enhanced"

    private val defaultDownloadsPath: String
        get() {
            val root = NSHomeDirectory().trimEnd('/')
            val path = "$root/Documents/nuvio_downloads"
            NSFileManager.defaultManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
            return path
        }

    private val _state = MutableStateFlow(
        DownloadExternalFolderState(
            uri = defaultDownloadsPath,
            displayName = defaultFolderLabel,
            unavailable = false,
        )
    )
    actual val state: StateFlow<DownloadExternalFolderState> = _state.asStateFlow()

    actual fun chooseFolder(onResult: (Result<String?>) -> Unit) {
        val path = defaultDownloadsPath
        val url = NSURL.fileURLWithPath(path)
        val filesUrl = NSURL(string = "shareddocuments://") ?: url

        if (UIApplication.sharedApplication.canOpenURL(filesUrl)) {
            UIApplication.sharedApplication.openURL(filesUrl, emptyMap<Any?, Any>()) { success ->
                if (!success) {
                    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
                }
            }
        } else {
            UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
        }

        onResult(Result.success(defaultFolderLabel))
    }

    actual fun selectedFolderUri(): String? =
        DownloadsStorage.loadExternalFolderUri()?.trim()?.takeIf { it.isNotBlank() } ?: defaultDownloadsPath

    actual fun clearFolder() {
        DownloadsStorage.saveExternalFolderUri(null)
        _state.value = DownloadExternalFolderState(
            uri = defaultDownloadsPath,
            displayName = defaultFolderLabel,
            unavailable = false,
        )
    }

    actual fun markUnavailable() {
        val current = _state.value
        if (current.uri != null && !current.unavailable) {
            _state.value = current.copy(unavailable = true)
        }
    }

    actual fun onProfileChanged() {
        val storedUri = DownloadsStorage.loadExternalFolderUri()?.trim()
        _state.value = DownloadExternalFolderState(
            uri = storedUri ?: defaultDownloadsPath,
            displayName = if (storedUri.isNullOrBlank()) {
                defaultFolderLabel
            } else {
                storedUri.substringAfterLast('/')
            },
            unavailable = false,
        )
    }
}

