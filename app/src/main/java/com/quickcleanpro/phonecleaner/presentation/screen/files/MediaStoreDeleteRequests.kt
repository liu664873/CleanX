package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

internal fun requestMediaStoreDeleteOrDeleteDirectly(
    context: Context,
    uris: List<Uri>,
    launchRequest: (IntentSenderRequest) -> Unit,
    deleteDirectly: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        deleteDirectly()
        return
    }

    val mediaStoreUris = uris.filter { it.canUseMediaStoreDeleteRequest() }
    requestMediaStoreDeleteUrisOrDeleteDirectly(
        mediaStoreUris = mediaStoreUris,
        launchDeleteRequest = { requestUris ->
            val request = MediaStore.createDeleteRequest(context.contentResolver, requestUris)
            launchRequest(IntentSenderRequest.Builder(request.intentSender).build())
        },
        deleteDirectly = deleteDirectly
    )
}

internal fun requestMediaStoreDeleteUrisOrDeleteDirectly(
    mediaStoreUris: List<Uri>,
    launchDeleteRequest: (List<Uri>) -> Unit,
    deleteDirectly: () -> Unit
) {
    if (mediaStoreUris.isEmpty()) {
        deleteDirectly()
        return
    }
    runCatching {
        launchDeleteRequest(mediaStoreUris)
    }.onFailure {
        deleteDirectly()
    }
}

private fun Uri.canUseMediaStoreDeleteRequest(): Boolean =
    scheme == ContentResolver.SCHEME_CONTENT &&
        authority == MediaStore.AUTHORITY &&
        runCatching {
            ContentUris.parseId(this)
            true
        }.getOrDefault(false)
