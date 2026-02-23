package com.example.database.di

import android.content.Context
import androidx.room.Room
import com.example.database.ChatDatabase
import com.example.database.dao.ChatMessageDao
import com.example.database.dao.ChatRoomDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "kakaotalk_chat.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatMessageDao(database: ChatDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    fun provideChatRoomDao(database: ChatDatabase): ChatRoomDao {
        return database.chatRoomDao()
    }
}
