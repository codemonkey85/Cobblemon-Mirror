/*
 *
 *  * Copyright (C) 2023 Cobblemon Contributors
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utilities.ACCESS_WIDENER

plugins {
    id("java")
    id("java-library")

    id("org.cadixdev.licenser")
    id("dev.architectury.loom")
    id("architectury-plugin")
    kotlin("jvm")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    //JEI
    maven("https://maven.blamejared.com/")
    maven("https://maven.parchmentmc.org")
}

license {
    header(rootProject.file("HEADER"))
}

architectury {
    minecraft = project.property("mc_version").toString()
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(project(":common").file(ACCESS_WIDENER))
}

dependencies {
    minecraft("net.minecraft:minecraft:${rootProject.property("mc_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21:${rootProject.property("parchment_version")}")
    })
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.add("-Xlint:-processing,-classfile,-serial")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    withType<Jar> {
        from(rootProject.file("LICENSE"))
    }
}
