package com.homefirstindia.hfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableCaching
class HomefirstOneSpringApplication

fun main(args: Array<String>) {
	runApplication<HomefirstOneSpringApplication>(*args)
}
