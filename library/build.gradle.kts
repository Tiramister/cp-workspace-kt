import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    id("java-library")
    kotlin("jvm") version "1.8.20"

    id("org.jetbrains.dokka") version "2.0.0"
    id("com.diffplug.spotless") version "7.0.3"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    kotlin {
        ktlint("1.5.0")
    }
    kotlinGradle {
        ktlint("1.5.0")
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("docs/package.md")
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(URL("https://github.com/Tiramister/cp-workspace-kt/tree/main/library/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
