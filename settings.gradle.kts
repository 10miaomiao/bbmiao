pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}

rootProject.name = "bbmiao"
include(":miao-binding")
include(":miao-binding-android")

// bilimiao 模块复用
include(":grpc-generator")
project(":grpc-generator").projectDir = file("bilimiao/grpc-generator")
include( ":DanmakuFlameMaster")
project(":DanmakuFlameMaster").projectDir = file("bilimiao/DanmakuFlameMaster")
include(":bilimiao-comm")
project(":bilimiao-comm").projectDir = file("bilimiao/bilimiao-comm")

include(":app")
 