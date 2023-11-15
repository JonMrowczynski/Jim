plugins {
    kotlin("jvm") version "1.9.10"
    idea
    application
    jacoco
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

val jetbrainsAnnotationsVersion = "24.0.1"
val junitVersion = "5.10.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

dependencies {
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testCompileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
}

jacoco.toolVersion = "0.8.11"

tasks {

    test {
        useJUnitPlatform()
        finalizedBy(listOf("jacocoTestReport", "jacocoTestCoverageVerification"))
    }

}