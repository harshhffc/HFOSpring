package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.clients.AmazonClient
import com.homefirstindia.hfo.clients.EnS3BucketPath
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.LoggerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File


@EnableAsync
@Component
class CommunicationScheduler(
    @Autowired val appProperty: AppProperty,
    @Autowired val amazonClient: AmazonClient
) {

    private fun log(value: String) = LoggerUtils.log("Scheduler.$value")

    //    @Scheduled(cron = "0 32 18 * * *", zone = "IST") //TODO: Comment for production
//    @Scheduled(cron = "0 50 11 * * *", zone = "IST")  //TODO: Uncomment for production
    @Scheduled(cron = "0 14 17 * * *", zone = "IST")  // TODO: Uncomment for production
    @Async
    fun backUpLogs() {

        log("backup logs scheduler started")

        try {
            log("backUpLogs - process to move log files to S3")

            val containerName = "hfo"  // Replace with your actual container name
            val logsDirPath = "/usr/local/tomcat/logs"

            // List log files inside the container
            val listProcess = Runtime.getRuntime().exec("docker exec $containerName ls $logsDirPath")
            val logFiles = listProcess.inputStream.bufferedReader().readLines()

            var totalProcessingLogs = 0
            var totalProcessedLogs = 0

            for (fileName in logFiles) {
                if (fileName.endsWith(".log") || fileName.endsWith(".txt")) {
                    totalProcessingLogs++

                    // Read the log file from the container
                    val readProcess = Runtime.getRuntime().exec("docker exec $containerName cat $logsDirPath/$fileName")
                    val logContent = readProcess.inputStream.bufferedReader().readText()

                    // Create a temporary file to write the log content to
                    val tempFile = File.createTempFile(fileName, null)
                    tempFile.writeText(logContent)

                    if (amazonClient.uploadFile(
                            fileName, tempFile,
                            appProperty.s3BucketName,
                            if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
                        )
                    ) {
                        totalProcessedLogs++
                    }

                    tempFile.delete()
                }
            }

            log(
                "backUpLogs - back up completed | total log: ${logFiles.size} " +
                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
            )

        } catch (e: Exception) {
            log("backUpLogs - Error in backing logs: ${e.message}")
        }
    }


}
