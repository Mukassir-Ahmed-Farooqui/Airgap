package com.mukassir.airgap

/**
 * Closed-testing release stages. Each Play closed-testing build is the same codebase
 * compiled with a different `releaseStage` (1..10), passed as `-PreleaseStage=N`.
 * Stage 10 (the default) is the full app. Anything a stage does not include is hidden
 * from the UI and its setting reads as the default, so stored values from a later
 * stage never leak into an earlier one. The database schema is identical in every
 * stage, so testers keep their chats across updates.
 *
 *  1  Core: onboarding, model import, streaming chat (CPU), default look
 *  2  Multiple chats: conversation drawer, switch, rename, delete
 *  3  Readable replies: markdown + code blocks, think-tag toggle
 *  4  Performance: Vulkan GPU, threads, mmap, mlock, quantized KV cache
 *  5  Control: sampling parameters, context, max tokens, system prompts
 *  6  Look: theme families and accent colours
 *  7  Fonts + text size, message search
 *  8  Security: biometric lock, auto-lock, screenshot/tapjacking/accessibility toggles
 *  9  Your data: export / import / clear chats, read aloud (TTS)
 * 10  Polish: translator preset, LaTeX hints, Help & model guide, About
 */
object Features {
    val STAGE: Int = BuildConfig.RELEASE_STAGE

    val MULTI_CHAT = STAGE >= 2
    val MARKDOWN = STAGE >= 3
    val PERFORMANCE = STAGE >= 4
    val SAMPLING = STAGE >= 5
    val THEMES = STAGE >= 6
    val FONTS_SEARCH = STAGE >= 7
    val SECURITY = STAGE >= 8
    val DATA_TTS = STAGE >= 9
    val POLISH = STAGE >= 10
}
