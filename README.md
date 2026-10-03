# Airgap

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A fully offline, zero-network private AI chat assistant for Android powered by `llama.cpp` and Jetpack Compose. All inference runs locally on your device's CPU/GPU. No cloud, no tracking, no network calls.

---

## Features

- **100% Offline & Private**: Zero network permissions requested. Your data never leaves your device.
- **On-Device LLM Inference**: Powered by local `llama.cpp` C++ engine with dynamic ARM CPU optimization (DotProd, FP16, I8MM, SVE) and Vulkan support.
- **GGUF Model Support**: Import any GGUF quantized model (Gemma, Qwen, Llama, Mistral, etc.).
- **Modern Jetpack Compose UI**: Fast, responsive, customizable themes (Catppuccin, Dracula, Ptyxis, AMOLED) and typography.
- **Security & Privacy**: Biometric authentication lock and encrypted local storage for chat history.
- **Markdown & Math Rendering**: Full rich-text chat response formatting.

---

## Quick Start & Usage

1. **Install the App**: Build from source or download the APK.
2. **Download a GGUF Model**:
   - Download any GGUF model file (e.g. `gemma-3-1b-it-Q4_K_M.gguf` or `qwen2.5-0.5b-instruct-q4_k_m.gguf`) from [HuggingFace](https://huggingface.co).
3. **Import Model in Airgap**:
   - Open Airgap -> **Settings** -> **Import GGUF Model**.
   - Select your `.gguf` file.
4. **Start Chatting**: All responses are generated locally on your phone.

---

## Building from Source

### Prerequisites
- **JDK**: Java 17
- **Android SDK**: API 34+
- **Android NDK**: Version `27.2.12479018`
- **CMake**: 3.22+
- **Ninja**: Build tool

### Clone & Build

```bash
# Clone repository with submodules (includes llama.cpp)
git clone --recurse-submodules https://github.com/Mukassir-Ahmed-Farooqui/Airgap.git
cd Airgap

# Build debug APK
./gradlew assembleDebug
```

---

## Changes from Upstream

- **Rebranded**: Rebranded application to **Airgap** with updated metadata, package ID (`com.mukassir.airgap`), and clean UI attribution.
- **Package Refactoring**: Refactored Kotlin and C++ JNI bridge packages to `com.mukassir.airgap` and `com.mukassir.airgap.smollm`.
- **Dynamic Signature Verification**: Made signature verification configurable via Gradle property (`AIRGAP_CERT_SHA256`) and automatically bypassed in debug builds (`BuildConfig.DEBUG`).
- **Cross-Platform Native Build Fixes**: Fixed NDK/CMake toolchain discovery for Windows, Linux, and macOS host environments (`ninja`, `glslc`, `c++_shared`, and `libomp.so` packaging).
- **Cleanups**: Removed old funding configs, obsolete screenshot assets, and verified privacy & backup rules.

---

## Credits & License

- **License**: [Apache License 2.0](LICENSE)
- **Based on**: [OfflineLLM](https://github.com/jegly/OfflineLLM) by jegly (Apache-2.0)
- **Native Wrapper**: Adapted from [SmolChat-Android](https://github.com/shubham0204/SmolChat-Android) by shubham0204 (Apache-2.0)
- **Inference Engine**: [llama.cpp](https://github.com/ggerganov/llama.cpp) (MIT License)
