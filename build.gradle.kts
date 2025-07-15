@file:Suppress("UnstableApiUsage", "PropertyName")

import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("fabric-loom") version "1.10.0-bta"
    id("java")
}

val lwjglVersion = "3.3.4"

val lwjglNatives = when {
    Os.isFamily(Os.FAMILY_UNIX) && !Os.isFamily(Os.FAMILY_MAC) -> "natives-linux"
    Os.isFamily(Os.FAMILY_WINDOWS) -> "natives-windows"
    Os.isFamily(Os.FAMILY_MAC) -> "natives-macos"
    else -> error("Unsupported OS")
}

val mod_group: String by project
val mod_name: String by project
val mod_version: String by project

val bta_channel: String by project
val bta_version: String by project

val loader_version: String by project

val halplibe_version: String by project
val mod_menu_version: String by project

group = mod_group
base.archivesName.set(mod_name)
version = mod_version

loom {
    noIntermediateMappings()
    customMinecraftMetadata.set("https://downloads.betterthanadventure.net/bta-client/$bta_channel/v$bta_version/manifest.json")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven {
        name = "Babric"
        url = uri("https://maven.glass-launcher.net/babric")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "SignalumMavenInfrastructure"
        url = uri("https://maven.thesignalumproject.net/infrastructure")
    }
    maven {
        name = "SignalumMavenReleases"
        url = uri("https://maven.thesignalumproject.net/releases")
    }
    ivy {
        url = uri("https://github.com/Better-than-Adventure")
        patternLayout {
            artifact("[organisation]/releases/download/v[revision]/[module].jar")
        }
        metadataSources { artifact() }
    }
    ivy {
        url = uri("https://downloads.betterthanadventure.net/bta-client/$bta_channel/")
        patternLayout {
            artifact("/v[revision]/client.jar")
        }
        metadataSources { artifact() }
    }
    ivy {
        url = uri("https://downloads.betterthanadventure.net/bta-server/$bta_channel/")
        patternLayout {
            artifact("/v[revision]/server.jar")
        }
        metadataSources { artifact() }
    }
    ivy {
        url = uri("https://piston-data.mojang.com")
        patternLayout {
            artifact("v1/[organisation]/[revision]/[module].jar")
        }
        metadataSources { artifact() }
    }
}

dependencies {
    minecraft("::${bta_version}")
    mappings(loom.layered {})

    modRuntimeOnly("objects:client:43db9b498cb67058d2e12d394e6507722e71bb45") // https://piston-data.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    // Helper library
    // If you do not need Halplibe you can comment this line out or delete this line
    modImplementation("turniplabs:halplibe:$halplibe_version")

    modImplementation("turniplabs:modmenu-bta:$mod_menu_version")

    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.16.0")

    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("com.google.code.gson:gson:2.10.1")

    val log4jVersion = "2.20.0"
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-1.2-api:$log4jVersion")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    include("org.apache.commons:commons-lang3:3.12.0")

    modImplementation("com.github.Better-than-Adventure:legacy-lwjgl3:1.0.5")
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-assimp::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-assimp:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.compileJava {
    options.release.set(8)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

configurations.configureEach {
    // Removes LWJGL2 dependencies
    exclude(group = "org.lwjgl.lwjgl")
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}
