plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "io.github.lumine1909"
version = "1.3.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.viaversion.com")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    implementation("io.github.lumine1909:reflexion:2.0.0")
    compileOnly("com.viaversion:viaversion-api:5.5.1")
}

tasks.shadowJar {
    archiveClassifier.set("")
}