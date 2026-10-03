package com.mukassir.airgap.di

import android.content.Context
import com.mukassir.airgap.ai.InferenceEngine
import com.mukassir.airgap.ai.ModelManager
import com.mukassir.airgap.data.repository.ChatRepository
import com.mukassir.airgap.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    @Singleton
    fun provideInferenceEngine(): InferenceEngine = InferenceEngine()

    @Provides
    @Singleton
    fun provideModelManager(
        @ApplicationContext context: Context,
        chatRepository: ChatRepository,
        settingsRepository: SettingsRepository,
        inferenceEngine: InferenceEngine,
    ): ModelManager = ModelManager(context, chatRepository, settingsRepository, inferenceEngine)
}
