package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableAsync
@Component
class CommunicationScheduler(
    @Autowired val appProperty: AppProperty,
    @Autowired val communicationBackgroundProcessHelper: CommunicationBackgroundProcessHelper
) {

    private fun log(value: String) = LoggerUtils.log("${this.javaClass.simpleName}.$value")

//    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) //TODO: Comment for production
    @Scheduled(cron = "0 15 16 * * *", zone = "IST")  //TODO: Uncomment for production
    @Async
    fun sendCallLogReport() {

        println("Schedular run at : ${DateTimeUtils.getCurrentDateTimeInIST()}")

        log("Schedular run at : ${DateTimeUtils.getCurrentDateTimeInIST()}")

//        if (!appProperty.runScheduler)
//            return
//
//        val monthStartDate = "${
//            DateTimeUtils.getLastNMonthFirstDate(
//                DateTimeFormat.yyyy_MM_dd,
//                DateTimeZone.IST, 0
//            )
//        } 00:00:00"
//
//        val currentDate = "${DateTimeUtils.getDateTimeByAddingDays(-1,
//            DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST)} 23:59:59"
//
//        log("sendCallLogReport - processing data between $monthStartDate to $currentDate")
//
//        communicationBackgroundProcessHelper.callLogExport(
//            monthStartDate, currentDate,
//            SalesforceObjectType.COLLECTION.value
//        )

    }

}