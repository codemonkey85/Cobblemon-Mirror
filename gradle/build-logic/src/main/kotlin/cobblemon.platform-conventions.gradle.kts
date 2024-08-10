import utilities.VersionType
import utilities.writeVersion

plugins {
    id("cobblemon.base-conventions")
    id("com.github.johnrengelman.shadow")
}

writeVersion(type = VersionType.FULL)

val bundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

loom {
    val clientConfig = runConfigs.getByName("client")
    clientConfig.runDir = "runClient"
    clientConfig.programArg("--username=CobblemonDev")
    //This is AshKetchum's UUID so you get an Ash Ketchum skin
    clientConfig.programArg("--uuid=93e4e551-589a-41cb-ab2d-435266c8e035")
    val serverConfig = runConfigs.getByName("server")
    serverConfig.runDir = "runServer"
}

tasks {

    jar {
        archiveBaseName.set("Cobblemon-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("Cobblemon-${project.name}")
        configurations = listOf(bundle)
        mergeServiceFiles()

        relocate ("org.graalvm", "com.cobblemon.mod.relocations.graalvm")
        relocate ("com.oracle", "com.cobblemon.mod.relocations.oracle")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("Cobblemon-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }

    val copyJar by registering(CopyFile::class) {
        val productionJar = tasks.remapJar.flatMap { it.archiveFile }
        fileToCopy = productionJar
        destination = productionJar.flatMap {
            rootProject.layout.buildDirectory.file("libs/${it.asFile.name}")
        }
    }

    assemble {
        dependsOn(copyJar)
    }

}