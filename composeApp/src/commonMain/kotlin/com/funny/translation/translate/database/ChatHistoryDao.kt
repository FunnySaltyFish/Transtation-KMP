package com.funny.translation.translate.database

import com.funny.compose.ai.bean.ChatMessage
import com.funny.translation.database.Dao
import com.funny.translation.database.Delete
import com.funny.translation.database.Insert
import com.funny.translation.database.Query

@Dao
interface ChatHistoryDao {
    @Query("select * from table_chat_history where conversationId = :conversationId")
    fun getMessagesByConversationId(conversationId: String): List<ChatMessage>
    
    // clear all messages by conversationId
    @Query("delete from table_chat_history where conversationId = :conversationId")
    fun clearMessagesByConversationId(conversationId: String)

    @Insert
    fun insert(chatMessage: ChatMessage)

    @Delete
    fun delete(id: String)
}