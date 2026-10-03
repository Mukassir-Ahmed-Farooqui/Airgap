package com.mukassir.airgap.di

import com.mukassir.airgap.data.local.dao.ConversationDao
import com.mukassir.airgap.data.local.dao.MessageDao
import com.mukassir.airgap.data.local.dao.ModelDao
import com.mukassir.airgap.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        conversationDao: ConversationDao,
        messageDao: MessageDao,
        modelDao: ModelDao,
    ): ChatRepository = ChatRepository(conversationDao, messageDao, modelDao)
}
