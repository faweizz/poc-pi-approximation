import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

application {
    mainClass.set("de.faweizz.sharing.MainKt")
}

group = "de.faweizz.sharing"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.avro:avro:1.10.2")
    implementation("io.strimzi:kafka-oauth-client:0.7.2")
    implementation("org.apache.kafka:kafka-clients:2.0.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}