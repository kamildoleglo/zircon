import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.dokka")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        kotlinx()
    }
}

tasks {
    val docsDir = projectDir.resolve("docs")

    val renameModulesToIndex by registering {
        doLast {
            logger.lifecycle("---=== Renaming -modules.html to index.html ===---")
            docsDir.listFiles()
                    ?.filter { it.isDirectory }
                    ?.forEach { dir ->
                        dir.listFiles()
                                ?.find { file -> file.name == "-modules.html" }
                                ?.renameTo(File(dir, "index.html"))
                    }
        }
    }

    val generateDocsIndexTask by registering {
        doLast {
            logger.lifecycle("---=== Generating index.html for docs ===---")

            val docsSubDirs = docsDir.listFiles()
                    ?.filter { it.isDirectory }
                    ?.joinToString("\n") { dir -> "<li><a href=\"${dir.name}\">${dir.name}</a></li>" }

            val html = """|<!doctype html><head><title>Choose a Version</title></head><body>
                |<h1>Pick a Version</h1><ul>
                |$docsSubDirs
                |</ul></body></html>""".trimMargin()

            docsDir.resolve("index.html").writeText(html)
        }
    }

    dokkaHtmlMultiModule {
        outputDirectory.set(docsDir.resolve(project.version.toString()))
        finalizedBy(renameModulesToIndex, generateDocsIndexTask)
    }

    register<DokkaMultiModuleTask>("dokkaHtmlAsJavaMultiModule") {
        addSubprojectChildTasks("dokkaHtmlAsJava")
        removeChildTask(":zircon.core:dokkaHtmlAsJava") // TODO: remove after https://github.com/Kotlin/dokka/issues/1663 is fixed
        outputDirectory.set(docsDir.resolve("${project.version}-JAVA"))
        finalizedBy(renameModulesToIndex, generateDocsIndexTask)
    }
}
