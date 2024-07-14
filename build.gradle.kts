plugins {
    java
    alias(libs.plugins.blossom)
    alias(libs.plugins.runvelocity)
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
    maven("https://maven.elytrium.net/repo/")
}

dependencies {
    implementation(libs.bstats)
    compileOnly(libs.velocity.api)
    compileOnly(files("libs/velocity.jar"))
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.vpacketevents)
    implementation("ninja.leaping.configurate:configurate-core:3.3")
    implementation("ninja.leaping.configurate:configurate-hocon:3.3")
}

blossom {
    replaceTokenIn("src/main/java/io/github/_4drian3d/unsignedvelocity/utils/Constants.java")
    replaceToken("{version}", version)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    build {
        dependsOn(shadowJar)
    }
    clean {
        delete("run")
    }
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        relocate("org.bstats", "io.github._4drian3d.unsignedvelocity.libs.bstats")
        minimize()
    }
    runVelocity {
        velocityVersion(libs.versions.velocity.get())
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
