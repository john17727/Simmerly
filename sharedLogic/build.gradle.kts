import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.SuspendInterop
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.skie)
}

kotlin {
    android {
        namespace = "dev.juanrincon.simmerly.logic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(libs.androidx.lifecycle.viewmodel)
            export(libs.orbit.core)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            // Ktor
            implementation(libs.ktor.client.android)

            // Koin
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            // ViewModel (non-Compose) — every ViewModel extends this
            api(libs.androidx.lifecycle.viewmodel)

            // Ktor and Kotlinx Serialization through Ktor
            api(libs.bundles.ktor)

            // Koin
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            api(libs.koin.core.viewmodel)

            // DataStore
            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)

            // Kotlinx DateTime
            api(libs.kotlinx.datetime)

            // Room
            api(libs.androidx.room.runtime)
            api(libs.androidx.sqlite.bundled)

            // Paging 3 (common, non-Compose)
            api(libs.androidx.paging.common)

            // Arrow
            api(libs.arrow.core)
            api(libs.arrow.fx.coroutines)

            // Orbit
            api(libs.orbit.core)
            api(libs.orbit.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.orbit.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        jvmMain.dependencies {
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

skie {
    features {
        group {
            SuspendInterop.Enabled(true)
            FlowInterop.Enabled(true)
            SealedInterop.Enabled(true)
            DefaultArgumentInterop.Enabled(true)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}
