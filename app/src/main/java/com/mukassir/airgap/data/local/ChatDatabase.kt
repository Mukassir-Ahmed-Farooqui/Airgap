package com.mukassir.airgap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mukassir.airgap.data.local.dao.ConversationDao
import com.mukassir.airgap.data.local.dao.MessageDao
import com.mukassir.airgap.data.local.dao.ModelDao
import com.mukassir.airgap.data.local.entities.Conversation
import com.mukassir.airgap.data.local.entities.Message
import com.mukassir.airgap.data.local.entities.ModelInfo

@Database(
    entities = [Conversation::class, Message::class, ModelInfo::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun modelDao(): ModelDao
}
