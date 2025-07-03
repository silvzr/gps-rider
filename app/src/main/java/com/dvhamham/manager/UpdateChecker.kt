package com.dvhamham.manager

fun checkUpdate(currentVersion: String, remoteVersion: String, isMajor: Boolean, onShowDialog: () -> Unit) {
    val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
    val remoteParts = remoteVersion.split(".").map { it.toIntOrNull() ?: 0 }
    if (remoteParts.size >= 3 && currentParts.size >= 3) {
        if (isMajor) {
            if (remoteParts[0] > currentParts[0]) {
                onShowDialog()
            }
        } else {
            if (remoteParts[0] == currentParts[0] && remoteParts[1] > currentParts[1]) {
                onShowDialog()
            }
        }
    }
}
