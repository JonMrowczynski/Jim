plugins {
    java
    idea
    application
    jacoco
    id("org.openjfx.javafxplugin") version "0.1.0"
}

application.mainClass = "canisius.jim.ruppet.RuppetTester"

val jetbrainsAnnotation = "org.jetbrains:annotations:26.1.0"

repositories.mavenCentral()

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    compileOnly(jetbrainsAnnotation)
    testCompileOnly(jetbrainsAnnotation)

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jacoco.toolVersion = "0.8.11"

tasks.test {
    useJUnitPlatform()
    finalizedBy(listOf("jacocoTestReport", "jacocoTestCoverageVerification"))
}