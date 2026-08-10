plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1"

// `./gradlew chiseledBuild` builds every supported version in one go. The list comes from
// settings.gradle, so adding a version there is enough.
tasks.register("chiseledBuild") {
    group = "project"
    dependsOn(stonecutter.versions.map { ":${it.project}:build" })
}

// `./gradlew chiseledModrinth` publishes every supported version.
tasks.register("chiseledModrinth") {
    group = "project"
    dependsOn(stonecutter.versions.map { ":${it.project}:modrinth" })
}
