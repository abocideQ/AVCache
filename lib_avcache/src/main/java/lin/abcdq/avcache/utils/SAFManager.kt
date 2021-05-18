package lin.abcdq.avcache.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream

/**
 * @author ABCDQ123  (https://github.com/ABCDQ123)
 */
internal object SAFManager {

    interface SAFRunnable {
        fun onRunning(percent: Int)

        fun onResponse(response: Boolean)
    }

    fun fileRead(context: Context, name_or_path: String): ArrayList<File> {
        return mediaStoreQuery(
            context,
            name_or_path
        )
    }

    fun fileDelete(context: Context, name_or_path: String) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.Q) return
        return mediaStoreDelete(
            context,
            name_or_path
        )
    }

    /**
     * app内部存储
     */
    fun appCacheDir(context: Context): String {//data/user/0/com.xxx.xxx/cache/
        return context.cacheDir.absolutePath
    }

    fun appFilesDir(context: Context): String {//data/user/0/com.xxx.xxx/files/
        return context.filesDir.absolutePath
    }

    /**
     * app外部存储
     */
    fun appStorageObbDir(context: Context): String {//storage/emulated/0/Android/obb/com.xxx.xxx/
        return context.obbDir.absolutePath
    }

    fun appStorageCacheDir(context: Context): String? {//storage/emulated/0/Android/data/com.xxx.xxx/cache/
        return context.externalCacheDir?.absolutePath
    }

    fun appStorageFileDir(
        context: Context,
        environment_type: String
    ): String? {//storage/emulated/0/Android/data/com.xxx.xxx/files/Documents.../
        return context.getExternalFilesDir(environment_type)?.absolutePath
    }

    /**
     * MediaStore
     */
    private fun mediaStoreFileCreate(
        context: Context,
        name_file: String,
        name_folder: String
    ): OutputStream? {
        try {
            var contentValues = ContentValues()
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, name_file)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/$name_folder"
            )
            var uri = context.contentResolver.insert(mediaStoreFiles(), contentValues)
            return context.contentResolver.openOutputStream(uri ?: return null) ?: return null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun mediaStoreFile2Download(
        context: Context,
        path_file: String,
        name_folder: String
    ): Boolean {
        return try {
            var selection = "_data=?"
            var args = arrayOf("$path_file")
            var contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/$name_folder"
            )
            context.contentResolver.update(mediaStoreFiles(), contentValues, selection, args)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //read permission
    private fun mediaStoreQuery(context: Context, name_or_path: String): ArrayList<File> {
        var list = ArrayList<File>()
        val selection = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString()
        )
        val order = "datetaken DESC"
        try {
            context.contentResolver.query(
                mediaStoreFiles(),
                null,
                selection,
                args,
                order
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    try {
                        val path: String = cursor.getString(cursor.getColumnIndexOrThrow("_data"))
                        val name: String =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                                ?: ""
                        if (name.contains("" + name_or_path) || path.contains("" + name_or_path)) {
                            list.add(File(path))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun mediaStoreDelete(context: Context, name_or_path: String) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.Q) return
        try {
            var selection = "_display_name=? OR _data=?"
            var args = arrayOf("$name_or_path", "$name_or_path")
            context.contentResolver.delete(mediaStoreFiles(), selection, args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mediaStoreImage(): Uri {
        return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    private fun mediaStoreVideo(): Uri {
        return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    private fun mediaStoreAudio(): Uri {
        return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    private fun mediaStoreDownload(): Uri? {
        if (Build.VERSION.SDK_INT < VERSION_CODES.Q) return null
        return MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    private fun mediaStoreFiles(): Uri {
        return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    }

    /**
     * 内部存储
     * @Deprecated
     */
    private fun dataDir(): String {//data/
        return Environment.getDataDirectory().absolutePath
    }

    private fun dataCacheDir(): String {//data/cache/
        return Environment.getDownloadCacheDirectory().absolutePath
    }

    /**
     * 外部存储
     * @Deprecated
     * target api > 29
     */
    private fun storageDir(): String {//storage/
//        return Environment.getStorageDirectory().absolutePath
        return Environment.getExternalStorageDirectory().absolutePath
    }

    /**
     * 手机系统存储
     * @Deprecated
     */
    private fun systemDir(): String {//system/
        return Environment.getRootDirectory().absolutePath
    }
}