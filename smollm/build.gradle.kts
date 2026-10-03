plugins {
    alias(libs.plugins.android.library)
}

val sdkDirRaw: String = rootProject.file("local.properties")
    .takeIf { it.exists() }
    ?.readLines()
    ?.firstOrNull { it.startsWith("sdk.dir=") }
    ?.removePrefix("sdk.dir=")
    ?.trim()
    ?: System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: ""

val sdkDir: String = sdkDirRaw
    .replace("\\:", ":")
    .replace("\\\\", "/")
    .replace("\\", "/")

// Toolchain versions are named once here — the NDK version and the CMake version
// are each referenced from several places below (ndkVersion, the glslc path, the
// ninja path, the AGP cmake block), and those drifting apart silently produces a
// path to a directory that no longer exists.
val ndkVersionPinned = "27.2.12479018"

val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
val isMac = org.gradle.internal.os.OperatingSystem.current().isMacOsX
val hostTag = when {
    isWindows -> "windows-x86_64"
    isMac -> "darwin-x86_64"
    else -> "linux-x86_64"
}
val exeExt = if (isWindows) ".exe" else ""

// Newest CMake present in the SDK, so installing a newer one from the SDK Manager
// is picked up without editing this file. llama.cpp declares
// cmake_minimum_required(3.14...3.28), so any SDK CMake satisfies it. Falls back
// to the version AGP bundles when the SDK dir can't be read.
val cmakeVersionPinned: String =
    file("$sdkDir/cmake")
        .takeIf { it.isDirectory }
        ?.listFiles()
        ?.filter { it.isDirectory && (File(it, "bin/ninja").exists() || File(it, "bin/ninja.exe").exists()) }
        ?.maxByOrNull { dir ->
            val p = dir.name.split('.').map { it.toIntOrNull() ?: 0 }
            p.getOrElse(0) { 0 } * 1_000_000L + p.getOrElse(1) { 0 } * 1_000L + p.getOrElse(2) { 0 }
        }
        ?.name
        ?: "3.22.1"

// Host toolchain for llama.cpp's vulkan-shaders-gen, which must run on the build
// machine (not Android). Mirrors upstream's host-toolchain.cmake.in but also pins
// CMAKE_MAKE_PROGRAM to the SDK's ninja when present, since the host may have no
// ninja on PATH and the inner ExternalProject configure fails without one.
val vulkanHostToolchain: File =
    layout.buildDirectory.file("vulkan-host-toolchain.cmake").get().asFile.apply {
        parentFile.mkdirs()
        val sdkNinja = file("$sdkDir/cmake/$cmakeVersionPinned/bin/ninja$exeExt")
            .takeIf { it.exists() } ?: file("$sdkDir/cmake/$cmakeVersionPinned/bin/ninja")
        val makeProgramLine =
            if (sdkDir.isNotEmpty() && sdkNinja.exists())
                "\nset(CMAKE_MAKE_PROGRAM \"${sdkNinja.absolutePath.replace('\\', '/')}\" CACHE FILEPATH \"\")"
            else ""
        val compilerLines = if (!isWindows && file("/usr/bin/gcc").exists()) """
            set(CMAKE_C_COMPILER /usr/bin/gcc)
            set(CMAKE_CXX_COMPILER /usr/bin/g++)
        """.trimIndent() else ""
        writeText(
            """
            set(CMAKE_BUILD_TYPE Release)
            set(CMAKE_C_FLAGS -O2)
            set(CMAKE_CXX_FLAGS -O2)
            set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
            set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY NEVER)
            set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE NEVER)
            """.trimIndent() + (if (compilerLines.isNotEmpty()) "\n$compilerLines" else "") + makeProgramLine
        )
    }

android {
    namespace = "com.mukassir.airgap.smollm"
    compileSdk = 37
    ndkVersion = ndkVersionPinned

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        ndk {
            abiFilters += "arm64-v8a"
        }
        externalNativeBuild {
            cmake {
                val vulkanHeaders = rootProject.file("Vulkan-Headers/include")
                val spirvHeaders = rootProject.file("SPIRV-Headers/install")
                val useVulkan = vulkanHeaders.exists() && spirvHeaders.exists()

                cppFlags += listOf("-O3")
                arguments += "-DANDROID_STL=c++_shared"
                // The SPIRV include dir is passed as a cache var further down
                // (-DSPIRV_HEADERS_INCLUDE_DIR) and consumed in our CMakeLists, rather
                // than pushed through cppFlags — cppFlags becomes CMAKE_CXX_FLAGS, which
                // is whitespace-split and applies to every C++ target in the build.
                arguments += listOf("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                arguments += "-DBUILD_SHARED_LIBS=ON"
                // common drags in HTTP/download code and is no longer needed —
                // the JNI wrapper uses the llama.h C API directly.
                arguments += "-DLLAMA_BUILD_COMMON=OFF"
                // Vulkan GPU backend (opt-in at runtime via Settings; devices without
                // Vulkan 1.2 fail registration gracefully and stay CPU-only).
                arguments += if (useVulkan) "-DGGML_VULKAN=ON" else "-DGGML_VULKAN=OFF"
                // Backends stay runtime-loadable so the Vulkan plugin can register (or
                // fail) independently of the CPU one.
                arguments += "-DGGML_BACKEND_DL=ON"
                // One ggml-cpu plugin per ARM feature level, scored against the device
                // at load time — this is what puts dotprod/fp16/i8mm/SVE kernels on the
                // SoCs that have them, while still running on armv8.0. Mutually exclusive
                // with a fixed GGML_CPU_ARM_ARCH pin, which would trade the whole ladder
                // for one hard-coded floor.
                arguments += "-DGGML_CPU_ALL_VARIANTS=ON"
                if (sdkDir.isNotEmpty()) {
                    // Only pin ninja when the SDK actually ships one — hosts like
                    // F-Droid's buildserver ship a different one and AGP falls back fine.
                    val ninja = file("$sdkDir/cmake/$cmakeVersionPinned/bin/ninja$exeExt")
                        .takeIf { it.exists() } ?: file("$sdkDir/cmake/$cmakeVersionPinned/bin/ninja")
                    if (ninja.exists()) {
                        arguments += "-DCMAKE_MAKE_PROGRAM=${ninja.absolutePath.replace('\\', '/')}"
                    }
                    if (useVulkan) {
                        // glslc ships with the NDK; FindVulkan won't discover it in the
                        // cross-compile sysroot on its own.
                        val glslc = file("$sdkDir/ndk/$ndkVersionPinned/shader-tools/$hostTag/glslc$exeExt")
                            .takeIf { it.exists() } ?: file("$sdkDir/ndk/$ndkVersionPinned/shader-tools/linux-x86_64/glslc")
                        arguments += "-DVulkan_GLSLC_EXECUTABLE=${glslc.absolutePath.replace('\\', '/')}"
                        arguments += "-DGGML_VULKAN_SHADERS_GEN_TOOLCHAIN=${vulkanHostToolchain.absolutePath.replace('\\', '/')}"
                        arguments += "-DVulkan_INCLUDE_DIR=${vulkanHeaders.absolutePath.replace('\\', '/')}"

                        val spirvInclude = rootProject.file("SPIRV-Headers/include")
                        if (spirvInclude.exists()) {
                            arguments += "-DSPIRV_HEADERS_INCLUDE_DIR=${spirvInclude.absolutePath.replace('\\', '/')}"
                        }
                        arguments += "-DSPIRV-Headers_DIR=${spirvHeaders.absolutePath.replace('\\', '/')}/share/cmake/SPIRV-Headers"
                    }
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = cmakeVersionPinned
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.coroutines.core)
}
