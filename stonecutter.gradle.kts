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

// `./gradlew collectJars` gathers just the release jars into build/release, so the release
// workflow can attach them without naming each Minecraft version.
tasks.register<Copy>("collectJars") {
    group = "project"
    val target = layout.buildDirectory.dir("release")
    doFirst { delete(target) }   // otherwise older versions linger between builds
    into(target)
    stonecutter.versions.forEach { version ->
        from(project(":${version.project}").tasks.named("remapJar"))
    }
}
