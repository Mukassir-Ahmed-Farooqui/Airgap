# Keep native methods
-keepclasseswithmembernames class com.mukassir.airgap.smollm.** {
    native <methods>;
}
-keep class com.mukassir.airgap.smollm.SmolLM { *; }
-keep class com.mukassir.airgap.smollm.GGUFReader { *; }
