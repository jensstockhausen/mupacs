plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "de.famst"
version = "0.0.1-SNAPSHOT"
description = "micro PACS"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
	mavenCentral()
	maven {
		url = uri("https://www.dcm4che.org/maven2")
	}
    // repository for weasis
    maven {
        url = uri("https://raw.githubusercontent.com/nroduit/mvn-repo/refs/heads/master")
    }
}


val dcm4cheVersion = "5.34.1"
val cliVersion = "1.11.0"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.data:spring-data-rest-hal-explorer")

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    implementation("commons-cli:commons-cli:${cliVersion}")
    implementation("com.h2database:h2")

    implementation("org.dcm4che:dcm4che-core:${dcm4cheVersion}")
    implementation("org.dcm4che:dcm4che-net:${dcm4cheVersion}")
    implementation("org.dcm4che:dcm4che-json:${dcm4cheVersion}")

    implementation("jakarta.json:jakarta.json-api:2.1.3")
    implementation("org.eclipse.parsson:parsson:1.1.7")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports {
        junitXml.required.set(false)
        html.required.set(true)
    }
}
