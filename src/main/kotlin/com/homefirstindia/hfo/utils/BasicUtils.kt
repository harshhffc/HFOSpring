package com.homefirstindia.hfo.utils

import com.homefirstindia.hfo.dto.v1.externalpartner.BasicAuthCreds
import com.homefirstindia.hfo.utils.LoggerUtils.log
import org.apache.commons.io.FileUtils
import java.io.*
import java.net.URL
import java.util.*
import javax.servlet.http.HttpServletRequest


fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun addRolePrefix(role: String) = "ROLE_$role"

fun getRandomUUID(): String = UUID.randomUUID().toString()

fun isNotNullOrNA(value: String?): Boolean {
    return (null != value && !value.equals(NA, ignoreCase = true) && !value.equals("Null", ignoreCase = true)
            && value.isNotEmpty())
}

enum class MimeMap(val mime: String, val extention: String) {
    PDF("application/pdf", ".pdf"),
    AUDIO("audio/mpeg", ".mp3");

    companion object {
        fun mapMimetoExt(mime: String): String {
            for (item in values()) {
                if (item.mime == mime) return item.extention
            }
            return "Unknown"
        }

        fun mapExtToMime(ext: String): String {
            for (item in values()) {
                if (item.extention == ext) return item.mime
            }
            return "Unknown"
        }
    }
}

@Throws(UnsupportedEncodingException::class)
fun getClientCreds(authorizationHeader: String): BasicAuthCreds {

    val decodedBytes = Base64.getDecoder().decode(authorizationHeader.replaceFirst("Basic ".toRegex(), ""))
    val clientCredsString = String(decodedBytes, Charsets.UTF_8)
    val tokenizer = StringTokenizer(clientCredsString, ":")
    val clientId = tokenizer.nextToken()
    val clientSecret = tokenizer.nextToken()

    return BasicAuthCreds(clientId, clientSecret)

}

fun downloadFileFromUrl(downloadUrl: String, filePath: String): Boolean {

    val fileOS: FileOutputStream?

    return try {

        val inputStream = BufferedInputStream(URL(downloadUrl).openStream())
        fileOS = FileOutputStream(filePath)
        val data = ByteArray(1024)
        var byteContent: Int
        while (inputStream.read(data, 0, 1024).also { byteContent = it } != -1) {
            fileOS.write(data, 0, byteContent)
        }

        fileOS.flush()
        fileOS.close()
        true

    } catch (ioe: Exception) {
        log("Error while saving file: $ioe")
        ioe.printStackTrace()
        false
    }
}

fun downloadFileFromUrl(url: URL, filePath: String): Boolean {

    return try {
        url.openStream().use { inp ->
            BufferedInputStream(inp).use { bis ->
                FileOutputStream(filePath).use { fos ->
                    val data = ByteArray(1024)
                    var count: Int
                    while (bis.read(data, 0, 1024).also { count = it } != -1) {
                        fos.write(data, 0, count)
                    }
                }
            }
        }
        true
    } catch (ioe: Exception) {
        log("Error while saving file: $ioe")
        ioe.printStackTrace()
        false
    }
}

fun generateFileName(identifier: String?, expectedFileName: String): String? {

    val fileName = StringBuilder()

    if (identifier.isNotNullOrNA()) {
        fileName.append(identifier)
        fileName.append("_")
    }

    fileName.append(System.currentTimeMillis())
    fileName.append("_")

    if (!expectedFileName.contains("."))
        return null

    fileName.append(expectedFileName)
    return fileName.toString()

}


fun isLTVInRange(
    loanAmount: Double,
    propertyValue: Double
): Boolean {

    return when (loanAmount) {
        in 1.0..3000000.0 -> (loanAmount / propertyValue) * 100.0 <= 90
        in 3000000.0..7000000.0 -> (loanAmount / propertyValue) * 100.0 <= 80
        else -> (loanAmount / propertyValue) * 100.0 <= 75
    }

}

fun getIPAddress(request: HttpServletRequest): String {

    var ipAddress = request.getHeader("X-FORWARDED-FOR")
    if (null == ipAddress) ipAddress = request.remoteAddr
    return ipAddress

}

@Throws(IOException::class)
fun getBase64FromFile(filePath: String?): String {
    val fileContent: ByteArray = FileUtils.readFileToByteArray(File(filePath!!))
    return Base64.getEncoder().encodeToString(fileContent)
}

fun isLoanTopUp(loanType: String): Boolean {
    val type = loanType.lowercase(Locale.getDefault())
    return type.contains("top-up") || type.contains("topup") || type.contains("top up")
}