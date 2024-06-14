import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	war
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "com.homefirstindia"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("mysql:mysql-connector-java")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.json:json:20230227")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("commons-codec:commons-codec:1.15")
	implementation("commons-io:commons-io:2.11.0")
	implementation("com.squareup.okhttp3:okhttp:4.10.0")
	implementation("org.apache.tika:tika-core:2.5.0")
	implementation("org.apache.httpcomponents:httpclient:4.5.13")
	implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
	implementation("com.opencsv:opencsv:5.7.1")
	implementation("org.springframework.boot:spring-boot-starter-mail:2.7.5")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.452")
	implementation("com.itextpdf:kernel:7.2.3")
	implementation("com.itextpdf:layout:7.2.3")

	testImplementation("com.h2database:h2") //TODO: check this later
	implementation("mysql:mysql-connector-java")


}

//tasks.withType<KotlinCompile> {
//	kotlinOptions {
//		freeCompilerArgs = listOf("-Xjsr305=strict")
//		jvmTarget = "11"
//	}
//}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<BootWar> {
	enabled = true
	archiveFileName.set("HomefirstOneSpring-0.0.1-SNAPSHOT.war")
}

tasks.withType<BootRun> {
	systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active"))
}
