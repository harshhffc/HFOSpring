package com.homefirstindia.hfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

//@SpringBootApplication(exclude = [
//	SecurityAutoConfiguration::class
//])
@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
class HomefirstOneSpringApplication

fun main(args: Array<String>) {
	runApplication<HomefirstOneSpringApplication>(*args)
}
