plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1"

// `./gradlew chiseledBuild` builds every version that currently compiles.
// 1.21.1 joins this list once the port lands.
tasks.register("chiseledBuild") {
    group = "project"
    dependsOn(":1.20.1:build")
}
