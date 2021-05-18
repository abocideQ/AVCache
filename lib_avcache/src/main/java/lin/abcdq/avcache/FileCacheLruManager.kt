package lin.abcdq.avcache

import java.io.File
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.Executors

internal class FileCacheLruManager {

    companion object {

        private var DEFAULT_MAX_DISK_SIZE = (1024 * 1024 * 512).toLong()
        private var DEFAULT_MAX_DISK_COUNT = 50

        fun setMaxDiskSize(max: Long) {
            if (max < 1024 * 1024) return
            DEFAULT_MAX_DISK_SIZE = max
        }

        fun setMaxDiskCount(max: Int) {
            if (max < 0) return
            DEFAULT_MAX_DISK_COUNT = max
        }

        private val mFileThread = Executors.newSingleThreadExecutor()

        fun run(file: File) {
            mFileThread.execute { FileCacheLruManager().modified(file) }
        }
    }

    private fun modified(file: File) {
        if (!file.exists()) return
        val length = file.length()
        if (length == 0L) return
        val accessFile = RandomAccessFile(file, "rwd")
        accessFile.seek(length - 1)
        val lastByte = accessFile.readByte()
        accessFile.seek(length - 1)
        accessFile.write(lastByte.toInt())
        accessFile.close()
        trim(file.parentFile ?: return)
    }

    private fun trim(folder: File) {
        val files: Array<File> = folder.listFiles() ?: return
        val list: List<File> = files.asList()
        Collections.sort(list, ModifiedComparator())
        val diskSize = countSize(list)
        val diskCount = countCount(list)
        for (file in list) delete(file, diskSize, diskCount)
    }

    private fun countSize(files: List<File>): Long {
        var size: Long = 0
        for (file in files) size += file.length()
        return size
    }

    private fun countCount(files: List<File>): Int {
        return files.size
    }

    private fun delete(file: File, diskSize: Long, diskCount: Int) {
        if (diskSize > DEFAULT_MAX_DISK_SIZE || diskCount > DEFAULT_MAX_DISK_COUNT) file.delete()
    }

    private class ModifiedComparator : Comparator<File?> {
        override fun compare(l: File?, r: File?): Int {
            return compareLong(l?.lastModified()!!, r?.lastModified()!!)
        }

        private fun compareLong(first: Long, second: Long): Int {
            return if (first < second) -1 else if (first == second) 0 else 1
        }
    }
}