package com.homefirstindia.hfo.helper.v1

import com.homefirstindia.hfo.dto.v1.MFile
import com.homefirstindia.hfo.security.AppProperty
import com.homefirstindia.hfo.utils.LoggerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.io.File
import javax.mail.SendFailedException
import javax.mail.internet.MimeMessage


@Component
class MailHelper(
    @Autowired val mailSender: JavaMailSender,
    @Autowired val appProperty: AppProperty
) {

    private fun log(value: String) = LoggerUtils.log("MailHelper.$value")

    fun sendSimpleMessage(
        to: String,
        subject: String,
        text: String,
        isHtml: Boolean = false,
    ) : Boolean {

        return try {

            val message = SimpleMailMessage()
            message.from = appProperty.senderEmail
            message.setTo(to)
            message.subject = subject
            message.text = text
            mailSender.send(message)
            true

        } catch (e: SendFailedException) {
            log("sendSimpleMessage - Error : ${e.message}")
            false
        }

    }

    fun sendMimeMessage(
        to: Array<String>,
        subject: String,
        body: String,
        isHtml: Boolean = false,
        files: ArrayList<MFile>? = null,
        cc: Array<String>? = null
    ) : Boolean {

        return try {

            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setFrom(appProperty.senderEmail)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body, isHtml)

            if (cc?.isNotEmpty() == true)
                helper.setCc(cc)

            files?.forEach {
                val file = FileSystemResource(File(it.path))
                helper.addAttachment(it.name, file)
            }

            mailSender.send(message)
            true

        } catch (e: SendFailedException) {
            log("sendMimeMessage - Error : ${e.message}")
            false
        }

    }

}