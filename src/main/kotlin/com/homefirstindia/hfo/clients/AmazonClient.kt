package com.homefirstindia.hfo.clients

import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import com.homefirstindia.hfo.manager.v1.CredsManager
import com.homefirstindia.hfo.manager.v1.EnCredType
import com.homefirstindia.hfo.manager.v1.EnPartnerName
import com.homefirstindia.hfo.model.v1.Creds
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import com.homefirstindia.hfo.utils.LoggerUtils.log
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.*


@Configuration
class AmazonClient(
    @Autowired val appProperty: AppProperty,
    @Autowired val cryptoUtils: CryptoUtils,
    @Autowired private val credsManager: CredsManager,
) {

    private var _amazonCred: Creds? = null

    private val BUCKET_NAME_PROD = "homefirstindia-s3bucket"
    private val BUCKET_NAME_TEST = "hffc-teststaging-s3"

    @Throws(Exception::class)
    private fun amazonCreds(): Creds {
        if (_amazonCred == null) {
            _amazonCred = credsManager.fetchCredentials(
                EnPartnerName.AMAZON, EnCredType.PRODUCTION
            )
            _amazonCred ?: throw Exception("Failed to get amazon credentials.")
        }
        return _amazonCred!!
    }

    @Bean
    fun s3(): AmazonS3 {

        if (amazonCreds().isEncrypted) {
            amazonCreds().username = cryptoUtils.decryptAes(amazonCreds().username)
            amazonCreds().password = cryptoUtils.decryptAes(amazonCreds().password)
        }

        val awsCredentials: AWSCredentials = BasicAWSCredentials(amazonCreds().username,
            amazonCreds().password)

        return AmazonS3ClientBuilder
                .standard()
                .withRegion(appProperty.s3BucketRegion)
                .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
                .build()
    }

    @Throws(Exception::class)
    fun uploadFile(fileName: String, file: File, bucketPath: EnS3BucketPath): Boolean {
        try {
            log("==> File saving in S3 with Name: $fileName")

            val putObjectRequest = PutObjectRequest(appProperty.s3BucketName,
                "${bucketPath.stringValue}/$fileName", file)
            s3().putObject(putObjectRequest)

            log("==> File saved successfully in S3 with Name: $fileName")

            return true
        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }
        return false
    }

    @Throws(java.lang.Exception::class)
    fun uploadFileToS3(downloadUrl: String, fileName: String): Boolean {

        if (!downloadUrl.startsWith("https")) {
            log("uploadFileToS3 - Invalid download url: $downloadUrl")
            return false
        }

        val filePath: String = appProperty.filePath + fileName

        if (downloadFileFromUrl(URL(downloadUrl), filePath)) {
            if (uploadFile(fileName, File(filePath), EnS3BucketPath.AUDIO_RECORDING))
                File(filePath).delete()
            return true
        }

        return false

    }

    fun getPublicURL(fileName: String, bucketPath: EnS3BucketPath, minutes: Int): String {

        var publicUrl: String = NA

        try {
            val expiration = Date()
            var expTimeMillis = expiration.time
            expTimeMillis += (1000 * 60 * minutes).toLong()
            expiration.setTime(expTimeMillis)
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(
                getBucketName(),
                bucketPath.stringValue + "/" + fileName
            ).withMethod(HttpMethod.GET).withExpiration(expiration)

            val url= s3().generatePresignedUrl(generatePresignedUrlRequest)!!
            publicUrl = url.toString()
        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }
        return publicUrl
    }


    fun getBucketName(): String {
      return if (appProperty.isProduction()) BUCKET_NAME_PROD else BUCKET_NAME_TEST

    }

    @Throws(Exception::class)
    fun uploadFile(fileName: String, fileData: String, bucketPath: EnS3BucketPath): Boolean {
        try {

            log("==> File saving in S3 with Name: $fileName")

            val bytes = Base64.decodeBase64(fileData.toByteArray())
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            val metadata = ObjectMetadata().apply {
                contentType = MimeMap.mapExtToMime("." + FilenameUtils.getExtension(fileName))
                contentLength = bytes.size.toLong()
                addUserMetadata("x-amz-meta-title", fileName)
            }

            val tags = ArrayList<Tag>()
            tags.add(Tag("Classification", "default"))

            val request = PutObjectRequest(
                appProperty.s3BucketName, bucketPath.stringValue + "/" + fileName,
                byteArrayInputStream, metadata
            )

            request.tagging = ObjectTagging(tags)

            s3().putObject(request)

            log("==> File saved successfully in S3 with Name: $fileName")

            return true

        } catch (e: AmazonServiceException) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace()
        }
        return false
    }

}

enum class EnS3BucketPath(val stringValue: String) {
    PROPERTY_INSIGHT_REPORTS("HomefirstOne/Property/InsightReport"),
    PROPERTY_INSIGHT_DOCUMENT("HomefirstOne/Property/InsightPropertyDocument"),
    AUDIO_RECORDING("HomefirstOne/Recording/Audio"),
    SIGNED_DOCS("HomefirstOne/ESign/Signed"),
    UNSIGNED_DOCS("HomefirstOne/ESign/Unsigned"),
    DOCUMENT_IMAGES("HomefirstOne/Documents"),
    MASK_IMAGES("HomefirstOne/Documents/Masked"),
    PAN_IMAGES("HomefirstOne/Documents/PAN"),
    AADHAAR_IMAGES("HomefirstOne/Documents/Aadhaar"),
    VOTER_IMAGES("HomefirstOne/Documents/Voter"),
    PASSPORT_IMAGES("HomefirstOne/Documents/Passport"),
    DRIVING_LICENCE_IMAGES("HomefirstOne/Documents/DrivingLicence"),
    TEMP_A_IMAGES("HomefirstOne/Documents/TempA"),
    CIBIL_DOCS("HomefirstOne/Documents/BureauReport/CibilDocument"),
    LAI_PATH("HomefirstOne/LAI_DOCUMENTS"),
    BANK_ACCOUNT("HomefirstOne/Documents/BankAccount"),
    IDV_DOCS("HomefirstOne/Documents/BureauReport/IdvDocument"),
    ESTAMP("HomefirstOne/EStamp"),
    PROPERTY_SITE_PHOTOGRAPH("HomefirstOne/Property/SitePhotograph"),
    CSUB_PAYOUT("HomefirstOne/Connector/PayoutUploads"),
    BACKUP_OPPORTUNITY("HomefirstOne/SFObject/Opportunity"),
    CO_LENDING_DOCS("HomefirstOne/Extras/CoLending"),
    GENERAL_DOCS("HomefirstOne/Extras/GeneralDocuments"),
    BANNER("external/promotion/banner"),
    AMORT_CALCULATION("HomefirstOne/Extras/Amort"),
    LMS_EXPORT("HomefirstOne/LMS/Exports"),
    PROPERTY_IMAGES("HomefirstOne/Property/PropertyPhotograph")
}