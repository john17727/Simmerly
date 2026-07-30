import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "dev.juanrincon.simmerly.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(projects.sharedLogic)

            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)
            implementation(libs.material3.adaptive.navigation.suite)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)

            // Koin Compose integrations
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.compose.navigation3)

            // Paging 3 Compose
            implementation(libs.androidx.paging.compose)

            // Material 3 Adaptive
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)

            // Compose Rich Text
            implementation(libs.compose.rich.text)

            // Navigation
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.navigation3.material3.adaptive)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)

            // Orbit Compose
            implementation(libs.orbit.compose)
        }

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

compose.resources {
    packageOfResClass = "simmerly.shared.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.ui.tooling)
}
