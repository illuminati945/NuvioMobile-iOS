package com.nuvio.app.features.downloads

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIModalPresentationFormSheet
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private var activePickerDelegate: NSObject? = null

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual object DownloadsExternalFolderPlatform {
    private const val defaultFolderLabel = "Files app > On My iPhone > Nuvio Enhanced"
    private const val bookmarkKey = "nuvio_download_folder_bookmark"

    val defaultDownloadsPath: String
        get() {
            val root = NSHomeDirectory().trimEnd('/')
            val path = "$root/Documents/nuvio_downloads"
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(path)) {
                fileManager.createDirectoryAtPath(
                    path = path,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }
            val keepFile = "$path/.keep"
            if (!fileManager.fileExistsAtPath(keepFile)) {
                fileManager.createFileAtPath(keepFile, null, null)
            }
            return path
        }

    private val _state = MutableStateFlow(
        DownloadExternalFolderState(
            uri = selectedFolderUri(),
            displayName = folderDisplayName(),
            unavailable = false,
        )
    )
    actual val state: StateFlow<DownloadExternalFolderState> = _state.asStateFlow()

    actual fun chooseFolder(onResult: (Result<String?>) -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
            val rootVC = topViewController()
            if (rootVC == null) {
                onResult(Result.failure(IllegalStateException("Unable to open folder picker")))
                return@dispatch_async
            }

            val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                    activePickerDelegate = null
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    if (url == null) {
                        onResult(Result.success(null))
                        return
                    }

                    url.startAccessingSecurityScopedResource()
                    val path = url.path ?: url.absoluteString ?: ""
                    val displayName = url.lastPathComponent?.takeIf { it.isNotBlank() } ?: path.substringAfterLast('/')

                    runCatching {
                        val bookmark = url.bookmarkDataWithOptions(
                            options = 0u,
                            includingResourceValuesForKeys = null,
                            relativeToURL = null,
                            error = null,
                        )
                        if (bookmark != null) {
                            NSUserDefaults.standardUserDefaults.setObject(bookmark, forKey = bookmarkKey)
                        }
                    }

                    DownloadsStorage.saveExternalFolderUri(path)
                    _state.value = DownloadExternalFolderState(
                        uri = path,
                        displayName = displayName,
                        unavailable = false,
                    )
                    onResult(Result.success(displayName))
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    activePickerDelegate = null
                    onResult(Result.success(null))
                }
            }

            activePickerDelegate = delegate

            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeFolder),
                asCopy = false,
            ).apply {
                this.delegate = delegate
                this.allowsMultipleSelection = false
                this.modalPresentationStyle = UIModalPresentationFormSheet
            }

            rootVC.presentViewController(picker, animated = true, completion = null)
        }
    }

    actual fun selectedFolderUri(): String? =
        DownloadsStorage.loadExternalFolderUri()?.trim()?.takeIf { it.isNotBlank() } ?: defaultDownloadsPath

    actual fun clearFolder() {
        DownloadsStorage.saveExternalFolderUri(null)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(bookmarkKey)
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
        _state.value = DownloadExternalFolderState(
            uri = selectedFolderUri(),
            displayName = folderDisplayName(),
            unavailable = false,
        )
    }

    private fun folderDisplayName(): String {
        val storedUri = DownloadsStorage.loadExternalFolderUri()?.trim()
        return if (storedUri.isNullOrBlank() || storedUri == defaultDownloadsPath) {
            defaultFolderLabel
        } else {
            storedUri.substringAfterLast('/')
        }
    }

    private fun topViewController(
        root: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)?.rootViewController
    ): UIViewController? {
        if (root is UINavigationController) return topViewController(root.visibleViewController)
        if (root is UITabBarController) return topViewController(root.selectedViewController)
        if (root?.presentedViewController != null) return topViewController(root.presentedViewController)
        return root
    }
}
