package dev.reggie.tarot

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

        const val MIME_TYPE = "application/zip"
    const val DEFAULT_FILE_NAME = "tarot-app-backup.terotbackup"
    const val DB_ENTRY = "tarot.db"
    const val PREFS_ENTRY = "user_overrides.xml"

    fun createBackup(context: Context, dest: Uri): Boolean {
        val db = File(context.filesDir, "tarot.db")
        val prefsFile = File(context.filesDir.parentFile, "shared_prefs/user_overrides.xml")

        return try {
            context.contentResolver.openOutputStream(dest, "wt")?.use { out ->
                ZipOutputStream(out.buffered()).use { zip ->
                    if (db.exists()) {
                        zip.putNextEntry(ZipEntry(DB_ENTRY))
                        db.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                    if (prefsFile.exists()) {
                        zip.putNextEntry(ZipEntry(PREFS_ENTRY))
                        prefsFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreBackup(context: Context, source: Uri): Boolean {
        val dbFile = File(context.filesDir, "tarot.db")
        val prefsDir = File(context.filesDir.parentFile, "shared_prefs")
        if (!prefsDir.exists()) prefsDir.mkdirs()

        var dbWritten = false
        var prefsWritten = false

        try {
            context.contentResolver.openInputStream(source)?.use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            DB_ENTRY -> {
                                dbFile.outputStream().use { zip.copyTo(it) }
                                dbWritten = true
                            }
                            PREFS_ENTRY -> {
                                val out = File(prefsDir, "user_overrides.xml")
                                out.outputStream().use { zip.copyTo(it) }
                                prefsWritten = true
                            }
                        }
                        entry = zip.nextEntry
                    }
                }
            } ?: return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        if (dbWritten) {
            TarotApp.core.reload()
        }
        if (prefsWritten) {
            TarotApp.overrides.reloadFromDisk(context)
        }
        return true
    }
}