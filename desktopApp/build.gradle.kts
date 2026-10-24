import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.sharedUI)
    implementation(libs.ui)
    implementation(libs.foundation)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.splashify)
}

compose.desktop {
    application {
        mainClass = "dev.juanrincon.simmerly.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.juanrincon.simmerly"
            packageVersion = "1.0.0"
        }
    }
}
