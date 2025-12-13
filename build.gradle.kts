// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    extra.apply {
        set("compile_sdk_version", 36)
        set("build_tools_version", 36)
        set("target_sdk_version", 36)
    }

    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }


}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.google.protobuf)  apply false
}