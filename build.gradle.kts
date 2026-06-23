import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.27")
}
val base = LibraryHelper(project)

group = "top.mrxiaom.sweet.monitor"
version = "1.0.2"
val targetJavaVersion = 8
val pluginBaseModules = base.modules.run{ listOf(library, paper, actions, l10n) }
val shadowGroup = "top.mrxiaom.sweet.monitor.libs"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly(base.depend.annotations)
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")

    base.library(LibraryHelper.adventure("4.25.0"))
    base.collectPluginHolders()

    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
}

LibraryHelper.initJava(project, base, targetJavaVersion, true)
LibraryHelper.initPublishing(project)

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.monitor")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
