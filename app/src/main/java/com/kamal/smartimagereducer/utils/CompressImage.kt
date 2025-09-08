package com.kamal.smartimagereducer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import com.kamal.smartimagereducer.utils.ImageUtil.compressImage
import com.kamal.smartimagereducer.utils.ImageUtil.decodeSampledBitmapFromFile
import io.reactivex.Flowable
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable


class CompressImage(context: Context) {
    private var maxWidth = 612
    private var maxHeight = 816
    private var compressFormat = CompressFormat.JPEG
    private var quality = 70
    private var destinationDirectoryPath: String

    fun setMaxWidth(maxWidth: Int): CompressImage {
        this.maxWidth = maxWidth
        return this
    }

    fun setMaxHeight(maxHeight: Int): CompressImage {
        this.maxHeight = maxHeight
        return this
    }

    fun setCompressFormat(compressFormat: CompressFormat): CompressImage {
        this.compressFormat = compressFormat
        return this
    }

    fun setQuality(quality: Int): CompressImage {
        this.quality = quality
        return this
    }

    fun setDestinationDirectoryPath(destinationDirectoryPath: String): CompressImage {
        this.destinationDirectoryPath = destinationDirectoryPath
        return this
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun compressToFile(imageFile: File, compressedFileName: String = imageFile.name): File {
        return compressImage(
            imageFile, maxWidth, maxHeight, compressFormat, quality,
            destinationDirectoryPath + File.separator + compressedFileName
        )
    }

    @Throws(IOException::class)
    fun compressToBitmap(imageFile: File?): Bitmap {
        return decodeSampledBitmapFromFile(imageFile!!, maxWidth, maxHeight)
    }

    @JvmOverloads
    fun compressToFileAsFlowable(
        imageFile: File,
        compressedFileName: String = imageFile.name): Flowable<File> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToFile(imageFile, compressedFileName))
            } catch (e: IOException) {
                return@Callable Flowable.error<File>(e)
            }
        })
    }

    fun compressToBitmapAsFlowable(imageFile: File?): Flowable<Bitmap> {
        return Flowable.defer(Callable {
            try {
                return@Callable Flowable.just(compressToBitmap(imageFile))
            } catch (e: IOException) {
                return@Callable Flowable.error<Bitmap>(e)
            }
        })
    }

    init {
        destinationDirectoryPath = context.cacheDir.path + File.separator + "images"
    }

}