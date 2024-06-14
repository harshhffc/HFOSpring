package com.homefirstindia.hfo.utils

import com.homefirstindia.hfo.utils.LoggerUtils.log
import org.apache.commons.codec.binary.Base64
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun convertMultiPartFileToFile(multipartFile: MultipartFile, directoryPath: String): File? {

    val file = File(directoryPath + multipartFile.originalFilename!!)

    return try {
        FileOutputStream(file).use { outputStream -> outputStream.write(multipartFile.bytes) }
        file
    } catch (ex: IOException) {
        log("Error converting the multi-part file to file= ${ex.message}")
        null
    }

}

fun convertBase64ToFile(base64: String, file: File): File? {

    if (!isNotNullOrNA(base64)) return null

    val data = Base64.decodeBase64(base64)

    try {
        val outputStream = BufferedOutputStream(FileOutputStream(file))
        outputStream.write(data)
        outputStream.flush()
        outputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    return file

}
