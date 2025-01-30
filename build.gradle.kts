import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jacoco)
    alias(libs.plugins.kapt)
}

group = "com.dugaza"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
//    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
//    implementation(libs.spring.boot.starter.oauth2.client)
//    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.thumbnailator)

    implementation(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    kapt(variantOf(libs.querydsl.apt) { classifier("jakarta") })

    runtimeOnly(libs.mysql.connector.j)
    developmentOnly(libs.spring.boot.devtools)

    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
