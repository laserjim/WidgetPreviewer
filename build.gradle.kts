plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.0")
    implementation("org.jetbrains:annotations:13.0")
    implementation("org.jetbrains.compose.runtime:runtime-desktop:1.6.11")
    implementation("org.jetbrains.compose.ui:ui-desktop:1.6.11")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:1.6.11")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20181211")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}