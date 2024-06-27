import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJPlugin)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
}

val pluginVersion: String by project

val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

val platformVersion: String by project

val javaVersion: String by project

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(javaVersion.toInt())
}

// https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = project.name
    version = platformVersion

    // https://github.com/JetBrains/gradle-intellij-plugin#building-properties
    updateSinceUntilBuild = false

    // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    // https://www.jetbrains.com/intellij-repository/releases/
    // localPath = "/Applications/Android Studio.app"
}

// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version = pluginVersion
    groups.empty()
}

tasks {
    wrapper {
        val gradleVersion: String by project
        setGradleVersion(gradleVersion)
        distributionType = Wrapper.DistributionType.ALL
    }

    patchPluginXml {
        version = pluginVersion
        sinceBuild = pluginSinceBuild

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = changelog.run {
            val changeLogItem = getOrNull(pluginVersion) ?: getUnreleased()
            renderItem(
                changeLogItem.withHeader(false).withEmptySections(false),
                Changelog.OutputType.HTML,
            )
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = listOf(pluginVersion.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
    }
}
