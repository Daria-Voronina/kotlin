import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// Contains common configuration that should be applied to all projects

// Common Group and version
val kotlinVersion: String by rootProject.extra
group = "org.jetbrains.kotlin"
version = kotlinVersion

// Forcing minimal gson dependency version
val gsonVersion = rootProject.extra["versions.gson"] as String
dependencies {
    constraints {
        configurations.all {
            if (isCanBeResolved && !isCanBeConsumed) {
                allDependencies.configureEach {
                    if (group == "com.google.code.gson" && name == "gson" && this@all.isCanBeDeclared) {
                        this@constraints.add(this@all.name, "com.google.code.gson:gson") {
                            version {
                                require(gsonVersion)
                            }
                            because("Force using same gson version because of https://github.com/google/gson/pull/1991")
                        }
                    }
                }
            }
        }
    }
}

project.configureJvmDefaultToolchain()
project.addEmbeddedConfigurations()
project.addImplicitDependenciesConfiguration()
project.configureJavaCompile()
project.configureJavaBasePlugin()
project.configureKotlinCompilationOptions()
project.configureArtifacts()
project.configureTests()

// There are problems with common build dir:
//  - some tests (in particular js and binary-compatibility-validator depend on the fixed (default) location
//  - idea seems unable to exclude common buildDir from indexing
// therefore it is disabled by default
// buildDir = File(commonBuildDir, project.name)

afterEvaluate {
    run configureCompilerClasspath@{
        val bootstrapCompilerClasspath by rootProject.buildscript.configurations
        configurations.findByName("kotlinCompilerClasspath")?.let {
            dependencies.add(it.name, files(bootstrapCompilerClasspath))
        }
        val bootstrapBuildToolsApiClasspath by rootProject.buildscript.configurations
        configurations.findByName("kotlinBuildToolsApiClasspath")?.let {
            dependencies.add(it.name, files(bootstrapBuildToolsApiClasspath))
        }

        configurations.findByName("kotlinCompilerPluginClasspath")
            ?.exclude("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable")
    }
}

fun Project.addImplicitDependenciesConfiguration() {
    configurations.maybeCreate("implicitDependencies").apply {
        isCanBeConsumed = false
        isCanBeResolved = false
    }
}

fun Project.addEmbeddedConfigurations() {
    configurations.maybeCreate("embedded").apply {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        }
    }

    configurations.maybeCreate("embeddedElements").apply {
        extendsFrom(configurations["embedded"])
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named("embedded-java-runtime"))
        }
    }
}

fun Project.configureJavaCompile() {
    plugins.withType<JavaPlugin> {
        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.add("-Xlint:deprecation")
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Werror")
        }
    }
}

fun Project.configureJavaBasePlugin() {
    plugins.withId("java-base") {
        fun File.toProjectRootRelativePathOrSelf() = (relativeToOrNull(rootDir)?.takeUnless { it.startsWith("..") } ?: this).path

        fun FileCollection.printClassPath(role: String) =
            println("${project.path} $role classpath:\n  ${joinToString("\n  ") { it.toProjectRootRelativePathOrSelf() }}")

        val javaExtension = javaPluginExtension()
        tasks {
            register("printCompileClasspath") { doFirst { javaExtension.sourceSets["main"].compileClasspath.printClassPath("compile") } }
            register("printRuntimeClasspath") { doFirst { javaExtension.sourceSets["main"].runtimeClasspath.printClassPath("runtime") } }
            register("printTestCompileClasspath") { doFirst { javaExtension.sourceSets["test"].compileClasspath.printClassPath("test compile") } }
            register("printTestRuntimeClasspath") { doFirst { javaExtension.sourceSets["test"].runtimeClasspath.printClassPath("test runtime") } }
        }
    }
}

val projectsUsedInIntelliJKotlinPlugin: Array<String> by rootProject.extra
val kotlinApiVersionForProjectsUsedInIntelliJKotlinPlugin: String by rootProject.extra

fun Project.configureKotlinCompilationOptions() {
    plugins.withType<KotlinBasePluginWrapper> {
        val commonCompilerArgs = listOfNotNull(
            "-opt-in=kotlin.RequiresOptIn",
            "-progressive".takeIf { getBooleanProperty("test.progressive.mode") ?: false },
            "-Xdont-warn-on-error-suppression",
        )

        val kotlinLanguageVersion: String by rootProject.extra
        val useJvmFir by extra(project.kotlinBuildProperties.useFir)
        val useFirLT by extra(project.kotlinBuildProperties.useFirWithLightTree)
        val useFirIC by extra(project.kotlinBuildProperties.useFirTightIC)
        val renderDiagnosticNames by extra(project.kotlinBuildProperties.renderDiagnosticNames)

        tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                freeCompilerArgs.addAll(commonCompilerArgs)
                languageVersion.set(KotlinVersion.fromVersion(kotlinLanguageVersion))
                apiVersion.set(KotlinVersion.fromVersion(kotlinLanguageVersion))
                freeCompilerArgs.add("-Xskip-prerelease-check")

                if (project.path in projectsUsedInIntelliJKotlinPlugin) {
                    apiVersion.set(KotlinVersion.fromVersion(kotlinApiVersionForProjectsUsedInIntelliJKotlinPlugin))
                }
            }

            val layout = project.layout
            val rootDir = rootDir
            val useAbsolutePathsInKlib = kotlinBuildProperties.getBoolean("kotlin.build.use.absolute.paths.in.klib")

            // Workaround to avoid remote build cache misses due to absolute paths in relativePathBaseArg
            // This is a workaround for KT-50876, but with no clear explanation why doFirst is used.
            // However, KGP with Native targets is used in the native-xctest project, and this code fails with
            //  The value for property 'freeCompilerArgs' is final and cannot be changed any further.
            if (project.path != ":native:kotlin-test-native-xctest" &&
                !project.path.startsWith(":native:objcexport-header-generator") &&
                !project.path.startsWith(":native:analysis-api-klib-reader")
            ) {
                doFirst {
                    if (!useAbsolutePathsInKlib) {
                        @Suppress("DEPRECATION")
                        (this as KotlinCompile<*>).kotlinOptions.freeCompilerArgs +=
                            "-Xklib-relative-path-base=${layout.buildDirectory.get().asFile},${layout.projectDirectory.asFile},$rootDir"
                    }
                }
            }
        }

        val jvmCompilerArgs = listOf(
            "-Xno-optimized-callable-references",
            "-Xno-kotlin-nothing-value-exception",
        )

        val projectsWithEnabledContextReceivers: List<String> by rootProject.extra
        val projectsWithOptInToUnsafeCastFunctionsFromAddToStdLib: List<String> by rootProject.extra

        tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.addAll(jvmCompilerArgs)
                if (renderDiagnosticNames) {
                    freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
                }
                allWarningsAsErrors.set(!kotlinBuildProperties.disableWerror)
                if (project.path in projectsWithEnabledContextReceivers) {
                    freeCompilerArgs.add("-Xcontext-receivers")
                }
                if (project.path in projectsWithOptInToUnsafeCastFunctionsFromAddToStdLib) {
                    freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction")
                }

                if (project.path == ":kotlin-util-klib") {
                    // This is a temporary workaround for a configuration problem in kotlin-native. Namely, module `:kotlin-native-shared`
                    // depends on kotlin-util-klib from bootstrap for some reason (see `kotlin-native/shared/build.gradle.kts`), but when
                    // we're packing dependencies for the use in the IDE, we pass paths to the newly built libraries to Proguard
                    // (see `prepare/ide-plugin-dependencies/kotlin-backend-native-for-ide/build.gradle.kts`).
                    //
                    // So the code which was compiled against one version of a library, is analyzed by Proguard against another version.
                    //
                    // This is a bad situation for JVM default flag behavior specifically. If kotlin-util-klib from bootstrap is compiled
                    // in the old mode (with DefaultImpls for interfaces), then subclasses in kotlin-native-shared will also be generated
                    // in the old mode (with DefaultImpls). But then Proguard will analyze these subclasses and their DefaultImpls classes,
                    // and will observe calls to non-existing methods from DefaultImpls of the interfaces in kotlin-util-klib, and report
                    // an error.
                    //
                    // This change will most likely not be needed after the bootstrap, as soon as kotlin-util-klib is compiled with
                    // `-Xjvm-default=all`.
                    freeCompilerArgs.add("-Xjvm-default=all-compatibility")
                } else if (!skipJvmDefaultAllForModule(project.path)) {
                    freeCompilerArgs.add("-Xjvm-default=all")
                }
            }
        }
    }
}

fun Project.configureArtifacts() {
    tasks.withType<Javadoc>().configureEach {
        enabled = false
    }

    tasks.withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        val `rw-r--r--` = 0b110100100
        val `rwxr-xr-x` = 0b111101101
        fileMode = `rw-r--r--`
        dirMode = `rwxr-xr-x`
        filesMatching("**/bin/*") { mode = `rwxr-xr-x` }
        filesMatching("**/bin/*.bat") { mode = `rw-r--r--` }
    }

    normalization {
        runtimeClasspath {
            ignore("META-INF/MANIFEST.MF")
            ignore("META-INF/compiler.version")
            ignore("META-INF/plugin.xml")
            ignore("kotlin/KotlinVersionCurrentValue.class")
        }
    }

    fun Task.listConfigurationContents(configName: String) {
        doFirst {
            project.configurations.findByName(configName)?.let {
                println("$configName configuration files:\n${it.allArtifacts.files.files.joinToString("\n  ", "  ")}")
            }
        }
    }

    tasks.register("listArchives") { listConfigurationContents("archives") }
    tasks.register("listDistJar") { listConfigurationContents("distJar") }
}

fun Project.configureTests() {
    val ignoreTestFailures: Boolean by rootProject.extra
    tasks.configureEach {
        if (this is VerificationTask) {
            ignoreFailures = ignoreTestFailures
        }
    }

    val concurrencyLimitService = project.gradle.sharedServices.registerIfAbsent(
        "concurrencyLimitService",
        ConcurrencyLimitService::class
    ) {
        maxParallelUsages = 1
    }

    tasks.withType<Test>().configureEach {
        outputs.doNotCacheIf("https://youtrack.jetbrains.com/issue/KTI-112") { true }
        if (project.kotlinBuildProperties.limitTestTasksConcurrency) {
            usesService(concurrencyLimitService)
        }
    }

    // Aggregate task for build related checks
    tasks.register("checkBuild")
    val mppProjects: List<String> by rootProject.extra
    if (path !in mppProjects) {
        configureTestRetriesForTestTasks()
    }
}

// TODO: migrate remaining modules to the new JVM default scheme.
fun skipJvmDefaultAllForModule(path: String): Boolean =
// Gradle plugin modules are disabled because different Gradle versions bundle different Kotlin compilers,
    // and not all of them support the new JVM default scheme.
    "-gradle" in path || "-runtime" in path || path == ":kotlin-project-model" ||
            // Visitor/transformer interfaces in ir.tree are very sensitive to the way interface methods are implemented.
            // Enabling default method generation results in a performance loss of several % on full pipeline test on Kotlin.
            // TODO: investigate the performance difference and enable new mode for ir.tree.
            path == ":compiler:ir.tree" ||
            // Workaround a Proguard issue:
            //     java.lang.IllegalAccessError: tried to access method kotlin.reflect.jvm.internal.impl.types.checker.ClassicTypeSystemContext$substitutionSupertypePolicy$2.<init>(
            //       Lkotlin/reflect/jvm/internal/impl/types/checker/ClassicTypeSystemContext;Lkotlin/reflect/jvm/internal/impl/types/TypeSubstitutor;
            //     )V from class kotlin.reflect.jvm.internal.impl.resolve.OverridingUtilTypeSystemContext
            // KT-54749
            path == ":core:descriptors"
