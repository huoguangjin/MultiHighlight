plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("com.jetbrains.rdgen") version "2022.2.3" // https://github.com/JetBrains/rd/releases
}

fun getRiderLibDir(): File {
    val ideClassesDir = rootProject.ext.get("ideClassesDir") as Provider<File>
    return File(ideClassesDir.get(), "lib/rd")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    val riderLibDir = getRiderLibDir()
    implementation(fileTree(riderLibDir) {
        include("rd-gen.jar", "rider-model.jar")
    })
}

rdgen {
    val dotnetPluginId = "ReSharperPlugin.MultiHighlight"

    val modelDir = File(rootDir, "rider-protocol/src/main/kotlin/model")
    val csOutput = File(rootDir, "src/dotnet/${dotnetPluginId}/Rider")
    val ktOutput = File(rootDir, "src/main/kotlin/com/github/huoguangjin/multihighlight")

    verbose = true

    val riderLibDir = getRiderLibDir()
    classpath("${riderLibDir}/rider-model.jar")

    sources("${modelDir}/rider")
    hashFolder = "${buildDir}"
    packages = "model.rider"

    generator {
        language = "kotlin"
        transform = "asis"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "com.jetbrains.rider.model"
        directory = "$ktOutput"
    }

    generator {
        language = "csharp"
        transform = "reversed"
        root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
        namespace = "JetBrains.Rider.Model"
        directory = "$csOutput"
    }
}
