/**
 * ownCloud Android client application
 *
 * @author David A. Velasco
 * @author David González Verdugo
 * @author Christian Schabesberger
 * @author Shashvat Kedia
 * @author Juan Carlos Garrote Gascón
 * <p>
 * Copyright (C) 2021 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.data.storage

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import java.io.File

sealed class LocalStorageProvider(private val rootFolderName: String) {

    abstract fun getPrimaryStorageDirectory(): File

    class LegacyStorageProvider(
        rootFolderName: String
    ) : LocalStorageProvider(rootFolderName) {

        override fun getPrimaryStorageDirectory(): File = Environment.getExternalStorageDirectory()
    }

    class ScopedStorageProvider(
        rootFolderName: String,
        private val context: Context
    ) : LocalStorageProvider(rootFolderName) {

        override fun getPrimaryStorageDirectory(): File = context.filesDir
    }

    /**
     * Return the root path of primary shared/external storage directory for this application.
     * For example: /storage/emulated/0/owncloud
     */
    private fun getRootFolderPath(): String = getPrimaryStorageDirectory().absolutePath + File.separator + rootFolderName

    /**
     * Get local storage path for accountName.
     */
    fun getAccountDirectoryPath(
        accountName: String?
    ): String = getRootFolderPath() + File.separator + getEncodedAccountName(accountName)

    /**
     * Get local path where OCFile file is to be stored after upload. That is,
     * corresponding local path (in local owncloud storage) to remote uploaded
     * file.
     */
    fun getDefaultSavePathFor(
        accountName: String?,
        remotePath: String
    ): String = getAccountDirectoryPath(accountName) + remotePath

    /**
     * Get absolute path to tmp folder inside datafolder in sd-card for given accountName.
     */
    fun getTemporalPath(
        accountName: String?
    ): String = getRootFolderPath() + "/tmp/" + getEncodedAccountName(accountName)

    fun getLogsPath(): String = getRootFolderPath() + LOGS_FOLDER_NAME

    fun getDefaultCameraSourcePath(): String {
        return DocumentFile.fromFile(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        ).createDirectory(CAMERA_FOLDER)?.uri.toString()
    }

    /**
     * Optimistic number of bytes available on sd-card.
     *
     * @return Optimistic number of available bytes (can be less)
     */
    @SuppressLint("UsableSpace")
    fun getUsableSpace(): Long = getPrimaryStorageDirectory().usableSpace

    /**
     * Checks if there is user data which does not have a corresponding account in the Account manager.
     */
    private fun getDanglingAccountDirs(remainingAccounts: Array<Account>): List<File> {
        val rootFolder = File(getRootFolderPath())
        val danglingDirs = mutableListOf<File>()
        if (rootFolder.listFiles() != null) {
            for (dir in rootFolder.listFiles()) {
                var dirIsOk = false
                if (dir.name.equals("tmp")) {
                    dirIsOk = true
                } else {
                    for (a in remainingAccounts) {
                        if (dir.name.equals(getEncodedAccountName(a.name))) {
                            dirIsOk = true
                        }
                    }
                }
                if (!dirIsOk) {
                    danglingDirs.add(dir)
                }
            }
        }
        return danglingDirs
    }

    open fun deleteUnusedUserDirs(remainingAccounts: Array<Account>) {
        val danglingDirs = getDanglingAccountDirs(remainingAccounts)
        for (dd in danglingDirs) {
            dd.deleteRecursively()
        }
    }

    /**
     * URL encoding is an 'easy fix' to overcome that NTFS and FAT32 don't allow ":" in file names,
     * that can be in the accountName since 0.1.190B
     */
    private fun getEncodedAccountName(accountName: String?): String = Uri.encode(accountName, "@")

    companion object {
        private const val CAMERA_FOLDER = "/Camera"
        private const val LOGS_FOLDER_NAME = "/logs/"
    }
}
