import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.changelog") version "2.0.0"
    id("org.jetbrains.qodana") version "0.1.13"
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

// https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(project.name)
    version.set(platformVersion)

    // https://github.com/JetBrains/gradle-intellij-plugin#building-properties
    updateSinceUntilBuild.set(false)

    // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    // https://www.jetbrains.com/intellij-repository/releases/
    // localPath.set("/Applications/Android Studio.app")
}

// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(pluginVersion)
    groups.set(emptyList())
}

// https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = javaVersion
    }

    wrapper {
        val gradleVersion: String by project
        setGradleVersion(gradleVersion)
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").let(::markdownToHTML)
        )

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                val changeLogItem = getOrNull(pluginVersion) ?: getUnreleased()
                renderItem(
                    changeLogItem.withHeader(false).withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        })
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
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
