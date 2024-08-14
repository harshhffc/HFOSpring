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
//    @Scheduled(cron = "0 39 17 * * *", zone = "IST")  // TODO: Uncomment for production
//    @Async
//    fun backUpLogs() {
//
//        log("backup logs scheduler started")
//
//        try {
//            log("backUpLogs - process to move log files to S3")
//
//            val dockerPath = "/usr/bin/docker"  // Adjust this if Docker is located elsewhere
//            val containerName = "hfo"
//            val logsDirPath = "/usr/local/tomcat/logs"
//
//            // List log files inside the container
//            val listProcess = ProcessBuilder(dockerPath, "exec", containerName, "ls", logsDirPath).start()
//            val logFiles = listProcess.inputStream.bufferedReader().readLines()
//            listProcess.waitFor()
//
//            var totalProcessingLogs = 0
//            var totalProcessedLogs = 0
//
//            for (fileName in logFiles) {
//                if (fileName.endsWith(".log") || fileName.endsWith(".txt")) {
//                    totalProcessingLogs++
//
//                    // Read the log file from the container
//                    val readProcess = ProcessBuilder(dockerPath, "exec", containerName, "cat", "$logsDirPath/$fileName").start()
//                    val logContent = readProcess.inputStream.bufferedReader().readText()
//                    readProcess.waitFor()
//
//                    // Create a temporary file to write the log content to
//                    val tempFile = File.createTempFile(fileName, null)
//                    tempFile.writeText(logContent)
//
//                    if (amazonClient.uploadFile(
//                            fileName, tempFile,
//                            appProperty.s3BucketName,
//                            if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
//                        )
//                    ) {
//                        totalProcessedLogs++
//                    }
//
//                    tempFile.delete()
//                }
//            }
//
//            log(
//                "backUpLogs - back up completed | total log: ${logFiles.size} " +
//                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
//            )
//
//        } catch (e: Exception) {
//            log("backUpLogs - Error in backing logs: ${e.message}")
//            e.printStackTrace()
//        }
//    }

    @Scheduled(cron = "0 32 12 * * *", zone = "IST")
    @Async
    fun backUpLogs() {

        log("backup logs scheduler started")

        try {
            log("Starting Docker command execution")

            val dockerPath = "/usr/bin/docker"
            val containerName = "hfo"
            val logsDirPathInContainer = "/usr/local/tomcat/logs"
            val hostTempDir = "/tmp/tomcat-logs"

            // Create the temporary directory if it doesn't exist
            val tempDir = File(hostTempDir)
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            // Build and start the process
            val processBuilder = ProcessBuilder(dockerPath, "cp", "$containerName:$logsDirPathInContainer", hostTempDir)
            processBuilder.redirectErrorStream(true)  // Combine stdout and stderr
            val process = processBuilder.start()

            // Capture and log process output
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            log("Docker command output: $output")
            log("Docker command exit code: $exitCode")

            if (exitCode != 0) {
                log("Error while executing docker cp command: $output")
                return
            }

            val logsDir = File(hostTempDir, "logs")
            val totalLogs = logsDir.listFiles()?.size ?: 0
            var totalProcessingLogs = 0
            var totalProcessedLogs = 0

            logsDir.listFiles()?.forEach { logFile ->
                if (logFile.name.endsWith(".log") || logFile.name.endsWith(".txt")) {
                    totalProcessingLogs++

                    val fileName = logFile.name

                    if (amazonClient.uploadFile(
                            fileName, logFile,
                            appProperty.s3BucketName,
                            EnS3BucketPath.LOGS_SERVER1
                        )
                    ) {
                        totalProcessedLogs++
                    }

                    logFile.delete()
                }
            }

            log(
                "backUpLogs - back up completed | total log: $totalLogs " +
                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
            )

        } catch (e: Exception) {
            log("backUpLogs - Error in backing logs: ${e.message}")
            e.printStackTrace()
        }

    }


}
