/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.Cobblemon
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileUtils {

    private fun getResourceStream(internal: String): InputStream {
        return Cobblemon::class.java.getResourceAsStream(internal)
            ?: throw Exception("Could not read $internal")
    }

    fun copyInternalToExternal(internal: String, external: Path) {
        getResourceStream(internal).use { stream ->
            Files.copy(stream, external, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    /**
     * Unzips a file to the target path
     */
    fun unzipFile(source: Path, target: Path) {
        ZipInputStream(FileInputStream(source.toFile())).use { zis ->
            var zipEntry = zis.nextEntry

            // While the zip has more entries in it
            while (zipEntry != null) {

                // Verify the path to protect from "zip slip" exploit
                val newPath = checkPath(zipEntry, target)

                if (!zipEntry.isDirectory) {
                    // Check if a parent directory needs to be created
                    if (newPath.parent != null) {
                        if (Files.notExists(newPath.parent)) {
                            Files.createDirectories(newPath.parent)
                        }
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.createDirectories(newPath)
                }
                // Next entry
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
        }
    }

    private fun checkPath(zipEntry: ZipEntry, targetDir: Path): Path {
        val targetDirResolved = targetDir.resolve(zipEntry.name)
        val normalizePath = targetDirResolved.normalize().toAbsolutePath()
        val targetDirPath = targetDir.normalize().toAbsolutePath()
        if (!normalizePath.startsWith(targetDirPath)) {
            throw IOException("Bad zip entry: " + zipEntry.name)
        }
        return normalizePath
    }

}