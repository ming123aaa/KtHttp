plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
    }
}

dependencies{
    api (libs.okhttp)
    api (libs.gson)
    api(libs.kotlinx.coroutines.core) // 核心库
    api("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
}