package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.model.v1.Attachment
import com.homefirstindia.hfo.repository.v1.DocumentRepositoryMaster
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.tika.Tika
import org.json.JSONArray
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

@Component
class DocumentHelper(
    @Autowired val documentRepositoryMaster: DocumentRepositoryMaster,
    @Autowired val appProperty: AppProperty,
) {

    private fun log(value: String) {
        LoggerUtils.log("DocumentHelper.$value")
    }

    private fun printLog(value: String) {
        println("DocumentHelper.$value")
    }

    fun getAttachmentFromDocJunction(docType: EnDocumentType, objectId: String): Attachment? {

        documentRepositoryMaster.documentJunctionRepository.getAllByObjectId(objectId)?.let { docJunctions ->

            docJunctions.forEach { singleDocJun ->

                singleDocJun.document?.let {

                    if (docType.value == it.documentType)
                        return it.attachment

                } ?: run { return null }

            }

        } ?: run { return null }

        return null
    }

    @Throws(Exception::class)
    fun convertFileFromBase64(base64: String, fileExtension: FileTypesExtentions): File {

        val file = File("${appProperty.filePath}${System.currentTimeMillis()}${fileExtension.ext}")
        val pdfAsBytes: ByteArray = java.util.Base64.getDecoder().decode(base64)
        val os = FileOutputStream(file, false)
        os.write(pdfAsBytes)
        os.flush()
        os.close()

        return file

    }

    @Throws(Exception::class)
    fun mergePDF(fileData: JSONArray?): String? {

        val fileName = System.currentTimeMillis()
        val mergedFile = File("${appProperty.filePath}$fileName${FileTypesExtentions.PDF.ext}")

        // check that the data has at least one file
        if (fileData != null && fileData.length() > 0) {

            // create a new final pdf
            val pdfDoc = PdfDocument(PdfWriter(mergedFile))

            for (i in 0 until fileData.length()) {

                // decode the data and get the file type
                val decoder: ByteArray = Base64.decodeBase64(fileData.optString(i))
                val tika = Tika()

                val fileType = MimeMap.mapMimetoExt(tika.detect(decoder))

                // Use the bytes and create a temp file on local system
                val tempFileName = "${appProperty.filePath}temp$fileName$fileType"
                val tempPdfName = "${appProperty.filePath}temp$fileName.pdf"

                FileUtils.writeByteArrayToFile(File(tempFileName), decoder)
                var file: File
                var pdfDocument: PdfDocument?
                // if file is not a pdf then create a pdf as we can merge only multiple pdfs
                if (fileType !== MimeMap.PDF.extention) {
                    // create a temp PDF file and add the image to it
                    file = File(tempPdfName)
                    val pdfWriter = PdfWriter(file)
                    pdfDocument = PdfDocument(pdfWriter)
                    val document = Document(pdfDocument)
                    val imageData = ImageDataFactory.create(tempFileName)
                    val pdfImg = Image(imageData)
                    document.add(pdfImg.scaleToFit(PageSize.A4.width - 50, PageSize.A4.height))
                    document.close()
                    val imgFile = File(tempFileName)
                    imgFile.delete()
                } else {
                    // if file is a pdf then simply just pass it to the merger function
                    file = File(tempFileName)
                }

                // Merge the files
                val merger = PdfMerger(pdfDoc)
                val otherPdf = PdfDocument(PdfReader(file))
                merger.merge(otherPdf, 1, otherPdf.numberOfPages)
                file.delete()
                otherPdf.close()
            }
            pdfDoc.close()
        }

        // After all the files are merged return the Base64
        val fileContent: ByteArray = FileUtils.readFileToByteArray(
            File(appProperty.filePath + fileName + ".pdf")
        )
        val fileString = Base64.encodeBase64String(fileContent)
        mergedFile.delete()

        return fileString

    }

    fun downloadImageFromUrl(downloadUrl: String?): String? {

        return try {

            val filePath = "${appProperty.filePath}${"file"}${FileTypesExtentions.JPG.ext}"

            val inputStream = BufferedInputStream(URL(downloadUrl).openStream())

            val fileOS = FileOutputStream(filePath)
            val data = ByteArray(1024)
            var byteContent: Int
            while (inputStream.read(data, 0, 1024).also { byteContent = it } != -1) {
                fileOS.write(data, 0, byteContent)
            }
            fileOS.flush()
            fileOS.close()

            val base64 = getBase64FromFile(filePath)
            File(filePath).delete()

            base64

        } catch (ioe: Exception) {

            LoggerUtils.log("Error while downloading image from URL: $ioe")
            ioe.printStackTrace()
            null

        }

    }

}