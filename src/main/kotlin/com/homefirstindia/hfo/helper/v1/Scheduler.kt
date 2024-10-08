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
//@Scheduled(cron = "0 25 13 * * *", zone = "IST")  // TODO: Uncomment for production
//@Async
//fun backUpLogs() {
//
//    log("backup logs scheduler started")
//
//    try {
//        log("backUpLogs - process to move log files to S3")
//
//        val dockerPath = "/usr/bin/docker"  // Update this path if Docker is located elsewhere
//        val containerName = "hfo"
//        val logsDirPath = "/usr/local/tomcat/logs"
//
//        // List log files inside the container
//        val listProcessBuilder = ProcessBuilder(dockerPath, "exec", containerName, "ls", logsDirPath)
//        listProcessBuilder.environment()["PATH"] = "/usr/bin:/bin:/usr/sbin:/sbin"
//        listProcessBuilder.redirectErrorStream(true)  // Redirect error stream to output for better logging
//        val listProcess = listProcessBuilder.start()
//
//        val listOutput = listProcess.inputStream.bufferedReader().readText()
//        val listError = listProcess.errorStream.bufferedReader().readText()
//        listProcess.waitFor()
//
//        if (listProcess.exitValue() != 0) {
//            log("Error listing log files: $listError")
//            throw RuntimeException("Error listing log files")
//        }
//
//        log("Log files found: $listOutput")
//        val logFiles = listOutput.lines()
//
//        var totalProcessingLogs = 0
//        var totalProcessedLogs = 0
//
//        for (fileName in logFiles) {
//            if (fileName.endsWith(".log") || fileName.endsWith(".txt")) {
//                totalProcessingLogs++
//
//                // Read the log file from the container
//                val readProcessBuilder = ProcessBuilder(dockerPath, "exec", containerName, "cat", "$logsDirPath/$fileName")
//                readProcessBuilder.environment()["PATH"] = "/usr/bin:/bin:/usr/sbin:/sbin"
//                readProcessBuilder.redirectErrorStream(true)  // Redirect error stream to output for better logging
//                val readProcess = readProcessBuilder.start()
//
//                val logContent = readProcess.inputStream.bufferedReader().readText()
//                val readError = readProcess.errorStream.bufferedReader().readText()
//                readProcess.waitFor()
//
//                if (readProcess.exitValue() != 0) {
//                    log("Error reading log file $fileName: $readError")
//                    continue
//                }
//
//                // Create a temporary file to write the log content to
//                val tempFile = File.createTempFile(fileName, null)
//                tempFile.writeText(logContent)
//
//                if (amazonClient.uploadFile(
//                        fileName, tempFile,
//                        appProperty.s3BucketName,
//                        if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
//                    )
//                ) {
//                    totalProcessedLogs++
//                }
//
//                tempFile.delete()
//            }
//        }
//
//        log(
//            "backUpLogs - back up completed | total log: ${logFiles.size} " +
//                    "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
//        )
//
//    } catch (e: Exception) {
//        log("backUpLogs - Error in backing logs: ${e.message}")
//        e.printStackTrace()
//    }
//}

//    @Scheduled(cron = "0 32 12 * * *", zone = "IST")
//    @Async
//    fun backUpLogs() {
//
//        log("backup logs scheduler started")
//
//        try {
//            log("Starting Docker command execution")
//
//            val dockerPath = "/usr/bin/docker"
//            val containerName = "hfo"
//            val logsDirPathInContainer = "/usr/local/tomcat/logs"
//            val hostTempDir = "/tmp/tomcat-logs"
//
//            // Create the temporary directory if it doesn't exist
//            val tempDir = File(hostTempDir)
//            if (!tempDir.exists()) {
//                tempDir.mkdirs()
//            }
//
//            // Build and start the process
//            val processBuilder = ProcessBuilder(dockerPath, "cp", "$containerName:$logsDirPathInContainer", hostTempDir)
//            processBuilder.redirectErrorStream(true)  // Combine stdout and stderr
//            val process = processBuilder.start()
//
//            // Capture and log process output
//            val output = process.inputStream.bufferedReader().readText()
//            val exitCode = process.waitFor()
//
//            log("Docker command output: $output")
//            log("Docker command exit code: $exitCode")
//
//            if (exitCode != 0) {
//                log("Error while executing docker cp command: $output")
//                return
//            }
//
//            val logsDir = File(hostTempDir, "logs")
//            val totalLogs = logsDir.listFiles()?.size ?: 0
//            var totalProcessingLogs = 0
//            var totalProcessedLogs = 0
//
//            logsDir.listFiles()?.forEach { logFile ->
//                if (logFile.name.endsWith(".log") || logFile.name.endsWith(".txt")) {
//                    totalProcessingLogs++
//
//                    val fileName = logFile.name
//
//                    if (amazonClient.uploadFile(
//                            fileName, logFile,
//                            appProperty.s3BucketName,
//                            EnS3BucketPath.LOGS_SERVER1
//                        )
//                    ) {
//                        totalProcessedLogs++
//                    }
//
//                    logFile.delete()
//                }
//            }
//
//            log(
//                "backUpLogs - back up completed | total log: $totalLogs " +
//                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
//            )
//
//        } catch (e: Exception) {
//            log("backUpLogs - Error in backing logs: ${e.message}")
//            e.printStackTrace()
//        }
//
//    }

//    @Scheduled(cron = "0 20 15 * * *", zone = "IST")  // TODO: Uncomment for production
//    @Async
//    fun backUpLogs() {
//        try {
//            log("backUpLogs - process to move log files to S3")
//
//            // Define the path where logs will be copied
//            val hostLogsDir = File("/tmp/container-logs")
//            if (!hostLogsDir.exists()) {
//                hostLogsDir.mkdirs()
//            }
//
//            // Copy logs from Docker container to host
//            val containerLogsPath = "/usr/local/tomcat/logs"
//            val containerName = "hfo"
//            val copyCommand = "/usr/bin/docker cp $containerName:$containerLogsPath ${hostLogsDir.absolutePath}"
//
//            val process = Runtime.getRuntime().exec(copyCommand)
//            process.waitFor()
//
//            if (process.exitValue() != 0) {
//                throw IOException("Failed to copy logs from container: ${process.errorStream.reader().readText()}")
//            }
//
//
//            // Process logs from host directory
//
//            val logsDir = File("/tmp/container-logs","logs")
//
//            log("size======${logsDir.listFiles()?.size}")
//            log("files======${logsDir.listFiles()}")
//
//            val totalLogs = logsDir.listFiles()!!.size
//            var totalProcessingLogs = 0
//            var totalProcessedLogs = 0
//
//            for (logFile in logsDir.listFiles()!!) {
//
//                log("log file name : ${logFile.name} \n")
//
//                if (logFile.name.endsWith(".log") || logFile.name.endsWith(".txt")) {
//                    totalProcessingLogs++
//
//                    val fileName = logFile.name
//                    if (amazonClient.uploadFile(
//                            fileName, logFile,
//                            appProperty.s3BucketName,
//                            if (appProperty.runScheduler) EnS3BucketPath.LOGS_SERVER1 else EnS3BucketPath.LOGS_SERVER2
//                        )
//                    ) {
//                        totalProcessedLogs++
//                    }
//
//                    logFile.delete()
//                }
//            }
//
//            log(
//                "backUpLogs - back up completed | total log: $totalLogs " +
//                        "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs"
//            )
//
//        } catch (e: Exception) {
//            log("backUpLogs - Error in backing logs: ${e.message}")
//        }
//    }


    @Scheduled(cron = "0 49 14 * * *", zone = "IST")  //TODO: Uncomment for production
    @Async
    fun backUpLogs() {

        try {

            log("backUpLogs - process to move log files to S3")

            val logsDir =  File("/usr/local/tomcat/logs")

            val totalLogs = logsDir.listFiles()!!.size
            var totalProcessingLogs = 0
            var totalProcessedLogs = 0

            for (logFile in logsDir.listFiles()!!) {

                if (logFile.name.endsWith(".log")
                    || logFile.name.endsWith(".txt")) {

                    totalProcessingLogs++

                    getLogFileDate(logFile.name)

                    val fileName = logFile.name

                    if (amazonClient.uploadFile(fileName, logFile,
                             appProperty.s3BucketName ,
                             EnS3BucketPath.LOGS_SERVER1)
                    )
                        totalProcessedLogs++

                    logFile.delete()

                }

            }

            log("backUpLogs - back up completed | total log: $totalLogs " +
                    "| total processing log: $totalProcessingLogs | total processed log: $totalProcessedLogs")

        } catch (e: Exception) {
            log("backUpLogs - Error in backing logs: ${e.message}")
        }

    }

    private fun getLogFileDate(name: String): String {
        return name.replace(".log", "")
            .replace(".txt", "")
            .replace("catalina.", "")
            .replace("localhost.", "")
            .replace("manager.", "")
            .replace("localhost_access_log.", "")
    }

}
