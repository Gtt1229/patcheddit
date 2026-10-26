plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly("androidx.preference:preference:1.2.0")
    implementation(libs.morphe.extensions.library)
    compileOnly(libs.bundles.boost)
    compileOnly(project(":extensions:boost:stub"))
    compileOnly(libs.annotation)
    compileOnly("com.squareup.okhttp3:okhttp:5.0.0-alpha.8")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.8")
}

configurations.configureEach {
    if (name.endsWith("CompileClasspath") || name.endsWith("RuntimeClasspath")) {
        resolutionStrategy.force("com.google.code.gson:gson:2.8.9")
    }
}

android {
    sourceSets.getByName("main").java.directories.add(
        project(":extensions:shared:library").file("src/main/java").absolutePath
    )
    defaultConfig {
        minSdk = 26
    }
}
