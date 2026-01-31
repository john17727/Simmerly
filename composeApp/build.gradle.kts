import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    androidLibrary {
        namespace = "dev.juanrincon.simmerly.composeApp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true // Needed for android resource, check later if still needed.
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            // Ktor
            implementation(libs.ktor.client.android)

            // Koin
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.material.icons.extended)
            implementation(libs.material3.adaptive.navigation.suite)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)

            // Essenty
            implementation(libs.essenty.lifecycle.coroutines)

            // Decompose
            api(libs.decompose)
            implementation(libs.decompose.extensions.compose)
            implementation(libs.decompose.extensions.compose.experimental)

            // MVIKotlin
            implementation(libs.mvikotlin)
            api(libs.mvikotlin.main)
            implementation(libs.mvikotlin.extensions.coroutines)

            // Ktor and Kotlinx Serialization through Ktor
            implementation(libs.bundles.ktor)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.compose.navigation3)

            // DataStore
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            // Kotlinx DateTime
            implementation(libs.kotlinx.datetime)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

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
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // Ktor
            implementation(libs.ktor.client.java)
        }
        iosMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.darwin)
        }

        all {
            /*
            Room generates file without this annotation tag. This lines tells it to
            not ignore the annotation.
             */
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
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

room {
    schemaDirectory("$projectDir/schemas")
}
