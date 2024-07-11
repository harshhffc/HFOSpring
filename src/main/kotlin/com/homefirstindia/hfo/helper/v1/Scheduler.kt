package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.DateTimeUtils
import com.homefirstindia.hfo.utils.LoggerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.scheduling.annotation.EnableScheduling


@Component
@EnableScheduling
class CommunicationScheduler(
    @Autowired val appProperty: AppProperty
) {

    private fun log(value: String) = LoggerUtils.log("Scheduler.$value")

//    @Scheduled(cron = "0 32 18 * * *", zone = "IST") //TODO: Comment for production
    @Scheduled(cron = "0 36 18 * * *", zone = "IST")  //TODO: Uncomment for production
    fun sendCallLogReport() {

        println("add value=========${add()}")

        log("sendCallLogReport: process started")
        log("Schedular run at : ${DateTimeUtils.getCurrentDateTimeInIST()}")

    }


    fun add(): Int {
        return 1+1
    }

}
