/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


object Constraints {
    const val SPRING_BOOT_VERSION = "3.2.2"
    const val KOTLIN_VERSION = "1.9.22"
    val githubPackagesUsername = System.getenv()["GIHUB_PACKAGE_USERNAME"]
    val githubPackagesToken = System.getenv()["GIHUB_PACKAGE_TOKEN"]
    val javaVersion = JavaVersion.VERSION_17
}
plugins {
    val kotlinVersion = "1.9.22"
    val springBootVersion = "3.2.2"
    id("java-library")
    id("maven-publish")
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
}

group = "com.github.breninsul"
version = "1.0.0"

java {
    sourceCompatibility = Constraints.javaVersion
}
java {
    withJavadocJar()
    withSourcesJar()
}
repositories {
    mavenCentral()
}
tasks.compileJava {
    dependsOn.add(tasks.processResources)
}
tasks.compileKotlin {
    dependsOn.add(tasks.processResources)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:${Constraints.SPRING_BOOT_VERSION}")
    api("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:${Constraints.SPRING_BOOT_VERSION}")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.apache.zookeeper:zookeeper:3.9.1")
    kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.apache.curator:curator-test:5.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = Constraints.javaVersion.majorVersion
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
            val softwareComponent = components.first()
            from(softwareComponent)
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/BreninSul/synchronization-starter")
            credentials {
                username = Constraints.githubPackagesUsername
                password = Constraints.githubPackagesToken
            }
        }
    }
}
