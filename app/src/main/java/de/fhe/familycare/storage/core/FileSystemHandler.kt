package de.fhe.familycare.storage.core

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Class used to save profile pictures of FamilyMembers to file system
 */
class FileSystemHandler {
    companion object{
        private const val PREFIX_PROFILE_IMAGE_FILE: String = "FC_ProfileImage_"
        private const val EXTENSION_PROFILE_IMAGE_FILE: String = ".jpg"

        /**
         * saves a bitmap to a given file
         */
        fun saveFile(file: File, bmp: Bitmap): Boolean{
            // save image to internal file storage
            return try {
                FileOutputStream(file).use { out ->
                    bmp.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        out
                    ) // bmp is your Bitmap instance
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        @Throws(IOException::class)
        fun createImageFile(context: Context): File? {
            return createFile(
                context, PREFIX_PROFILE_IMAGE_FILE,
                EXTENSION_PROFILE_IMAGE_FILE, Environment.DIRECTORY_PICTURES
            )
        }

        /**
         * deletes a file using its path
         *
         * @param filePath expects format 'file:///folder/file'
         */
        fun deleteFile(filePath: String): Boolean {
            return try{
                val fileNameSting = filePath.takeLast(filePath.length - 7)
                val file = File(fileNameSting)
                file.delete()
                Log.i("FM", "image: $fileNameSting deleted")
                true
            }
            catch (e: Exception){
                e.printStackTrace()
                false
            }
        }

        /**
         *  Private Helper to create files
         */
        @Throws(IOException::class)
        private fun createFile(
            context: Context, prefix: String, extension: String,
            subdirectory: String
        ): File? {
            val imageFileName = prefix + Date().time + "_"
            val storageDir: File? = context.getExternalFilesDir(subdirectory)
            return File.createTempFile(imageFileName, extension, storageDir)
        }
    }
}