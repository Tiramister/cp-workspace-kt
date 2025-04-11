plugins {
    id("java-library")
    kotlin("jvm") version "1.8.20"
    id("com.diffplug.spotless") version "7.0.3"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

spotless {
    kotlin {
        ktlint("1.5.0")
    }
    kotlinGradle {
        ktlint("1.5.0")
    }
}
