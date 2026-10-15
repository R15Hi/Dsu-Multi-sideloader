package vegabobo.dsusideloader.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile

class FilenameUtils {

    companion object {

        /**
         * Append text to the end of all digits containing in a string
         * @param input String containing digits
         * @param textToAppend Text that will be appended
         * @return Formatted string, if there is no digits in "input", a empty string will be returned.
         */
        fun appendToDigitsToString(input: String, textToAppend: String): String {
            var newText = input.filter { it.isDigit() } + textToAppend
            if (newText == textToAppend) {
                newText = ""
            }
            return newText
        }

        /**
         * Tries to convert DocumentFile uri to real path
         * isn't guaranteed that will work with all kinds of path
         */
        fun getFilePath(uri: Uri, addQuotes: Boolean = false): String {
            val input = uri.path.toString()
            val safStorage = input.split("/document/")[1].replace("/tree/", "")
            val path = safStorage.split(":")[1]
            if (path.contains("/storage/emulated")) {
                return if (addQuotes) "'file://'$path" else "file://$path"
            }
            return if (safStorage.contains("primary")) {
                val storagePath = "file:///storage/emulated/0/"
                val finalPath = "$storagePath$path"
                return if (addQuotes) "'$finalPath'" else finalPath
            } else {
                val storagePath = "file:///storage/"
                val finalPath = storagePath + safStorage.replace(":", "/")
                if (addQuotes) "'$finalPath'" else finalPath
            }
        }

        fun queryName(resolver: ContentResolver, uri: Uri): String {
            if (uri.scheme == "file") {
                return uri.lastPathSegment ?: ""
            }
            return try {
                val returnCursor: Cursor? = resolver.query(uri, null, null, null, null)
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val name: String = if (nameIndex != -1) returnCursor.getString(nameIndex) else (uri.lastPathSegment ?: "")
                    returnCursor.close()
                    name
                } else {
                    uri.lastPathSegment ?: ""
                }
            } catch (e: Exception) {
                uri.lastPathSegment ?: ""
            }
        }

        fun getDigits(input: String): String {
            return appendToDigitsToString(input, "")
        }

        fun getLengthFromFile(context: Context, uri: Uri): Long {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val f = java.io.File(path)
                    if (f.exists()) return f.length()
                }
            }
            return try {
                DocumentFile.fromSingleUri(context, uri)?.length() ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }
}
