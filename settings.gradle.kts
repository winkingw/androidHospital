pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // 阿里云镜像作为国内开发备用源，放在标准仓库之后，避免 CI 上因镜像异常导致构建失败
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 阿里云镜像作为国内开发备用源，放在标准仓库之后，避免 CI 上因镜像异常导致构建失败
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
    }
}

rootProject.name = "SereneHealth"
include(":app")
