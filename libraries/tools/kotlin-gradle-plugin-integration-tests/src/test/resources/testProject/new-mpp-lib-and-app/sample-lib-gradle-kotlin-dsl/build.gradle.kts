plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

group = "com.example"
version = "1.0"

//repositories {
//    mavenLocal()
//    mavenCentral()
//}

kotlin {
    val jvm = jvm("jvm6")

    linuxX64("linux64")

    val shouldBeJs = true

    if (shouldBeJs) {
        js("nodeJs") {
            nodejs()
        }
        wasmJs()
    }

    targets.all {
        mavenPublication(Action<MavenPublication> {
            pom.withXml(Action<XmlProvider> {
                asNode().appendNode("name", "Sample MPP library")
            })
        })
    }

    sourceSets {
        jvm.compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.23.4")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            setUrl("${projectDir.absolutePath.replace('\\', '/')}/repo")
        }
    }
}

// Check that a compilation may be created after project evaluation, KT-28896:
afterEvaluate {
    kotlin {
        jvm("jvm6").compilations.create("benchmark") {
            tasks["assemble"].dependsOn(compileKotlinTask)
        }
    }
}
