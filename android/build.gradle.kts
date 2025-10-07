@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application") version "8.6.0" apply false
    kotlin("android") version "1.9.24" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

