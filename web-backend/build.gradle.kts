import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.shadowjar)
    id("jacoco")
}

val appGroupId = "com.github.aivanovski.testwithme"

group = appGroupId
version = libs.versions.appVersion.get()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    test {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    jacocoTestReport {
        reports {
            val coverageDir = File("$buildDir/reports/coverage")
            csv.required.set(true)
            csv.outputLocation.set(File(coverageDir, "coverage.csv"))
            html.required.set(true)
            html.outputLocation.set(coverageDir)
        }

        dependsOn(allprojects.map { it.tasks.named<Test>("test") })
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("test-with-me-backend")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.github.aivanovski.testwithme.web.WebAppMainKt"))
        }
    }
}

dependencies {
    testImplementation(libs.junit.engine)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)

    implementation(libs.logback)
    implementation(libs.koin)
    implementation(libs.kotlinx.json)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authjwt)

    // Arrow
    implementation(libs.arrow.core)
    implementation(libs.arrow.coroutines)

    // TestWithMe API
    implementation(project(":test-with-me"))
    implementation(project(":web-api"))

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.51.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.51.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.51.1")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:3.4.2")
    // // implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    // implementation("org.hibernate.orm:hibernate-c3p0:6.5.2.Final")

    // implementation("org.hibernate.orm:hibernate-entitymanager:6.5.2.Final")

    // implementation("org.springframework.data:spring-data-jpa:3.3.1")


}