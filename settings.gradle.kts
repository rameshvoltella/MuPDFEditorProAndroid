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
        google()
        maven {
            url = uri("https://www.jitpack.io")
        }
        maven {
            url = uri("https://maven.ghostscript.com/")
        }
        jcenter()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
        maven {
            url = uri("https://maven.ghostscript.com/")
        }
        jcenter()
    }
}

rootProject.name = "MUPDF Highliter"
include(":app")
include(":mupdf")
