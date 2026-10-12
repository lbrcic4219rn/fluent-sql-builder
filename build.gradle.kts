plugins {
    java
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val formatter: Configuration = configurations.create("formatter")

dependencies {
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    runtimeOnly(libs.slf4j.jdk14)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    formatter(libs.google.java.format)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "app.Main"
}

tasks.named<JavaExec>("run") {
    val envFile = file(".env")
    if (envFile.isFile) {
        envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .forEach {
                val name = it.substringBefore("=").trim()
                if (System.getenv(name) == null) {
                    environment(name, it.substringAfter("=").trim())
                }
            }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

fun javaSources(): List<String> =
    listOf("src/main/java", "src/test/java")
        .filter { file(it).isDirectory }
        .flatMap { fileTree(it) { include("**/*.java") }.files }
        .map { it.absolutePath }

val formatterJvmArgs =
    listOf(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )

val formatCheck =
    tasks.register<JavaExec>("formatCheck") {
        group = "verification"
        description = "Fails if any source file is not formatted."
        classpath = formatter
        mainClass = "com.google.googlejavaformat.java.Main"
        jvmArgs = formatterJvmArgs
        args = listOf("--aosp", "--dry-run", "--set-exit-if-changed") + javaSources()
    }

tasks.check {
    dependsOn(formatCheck)
}

tasks.register<JavaExec>("format") {
    group = "formatting"
    description = "Reformats all Java sources in place."
    classpath = formatter
    mainClass = "com.google.googlejavaformat.java.Main"
    jvmArgs = formatterJvmArgs
    args = listOf("--aosp", "--replace") + javaSources()
}
