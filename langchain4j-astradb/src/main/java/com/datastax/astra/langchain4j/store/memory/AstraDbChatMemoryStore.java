package com.datastax.astra.langchain4j.store.memory;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.commands.FindOptions;
import com.datastax.astra.client.core.query.Sorts;
import com.datastax.astra.client.databases.Database;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.langchain4j.store.memory.AstraDbChatMessage.PROP_MESSAGE;

@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
public class AstraDbChatMemoryStore implements ChatMemoryStore {

    /**
     * Some attributes in the collection to store the chat messages.
     */
    public static final String DEFAULT_COLLECTION_NAME     = "chat_memory";

    /**
     * Client to work with an Astra Collection
     */
    private final Collection<AstraDbChatMessage> chatMemoryCollection;

    /**
     * Client to work with an Astra Collection
     */
    private final Database astraDatabase;

    /**
     * Reuse a collection to store chat messages.
     *
     * @param collection
     *      current collection to store chat messages
     */
    public AstraDbChatMemoryStore(Collection<AstraDbChatMessage> collection) {
        this.chatMemoryCollection = collection;
        this.astraDatabase        = collection.getDatabase();
    }

    /**
     * Create a default collection (no vector) to store chat messages.
     *
     * @param database
     *      client for existing active database
     */
    public AstraDbChatMemoryStore(Database database) {
        this(database.createCollection(DEFAULT_COLLECTION_NAME, CollectionOptions
                .builder().indexingDeny(PROP_MESSAGE) // i do not need index on the message
                .build(), AstraDbChatMessage.class));
    }

    /**
     * Create the collection if not exist.
     */
    public void create() {
        if (!chatMemoryCollection.exists()) {
            astraDatabase.createCollection(
                    chatMemoryCollection.getCollectionName(),CollectionOptions
                        .builder()
                        .indexingDeny(PROP_MESSAGE)
                        .build());
        }
    }

    /**
     * Delete the collection
     */
    public void clear() {
        chatMemoryCollection.deleteAll();
    }

    /**
     * Delete the collection
     */
    public void delete() {
        chatMemoryCollection.drop();
    }

    /** {@inheritDoc} */
    @Override
    public List<ChatMessage> getMessages(@NonNull Object chatId) {
        return getConversation(chatId).stream().map(AstraDbChatMessage::toChatMessage).collect(Collectors.toList());
    }

    /**
     * Get access to a conversation.
     *
     * @param conversationId
     *      conversation id
     * @return
     *      list of messages
     */
    public List<AstraDbChatMessage> getConversation(@NonNull Object conversationId) {
        return chatMemoryCollection.find(eq(AstraDbChatMessage.PROP_CHAT_ID, conversationId), new FindOptions()
                .sort(Sorts.descending(AstraDbChatMessage.PROP_MESSAGE_TIME)))
                .all();
    }

    /**
     * Custom method to replace a conversation.
     *
     * @param conversationId
     *      conversation id
     * @param list
     *      list of messages.
     */
    public void replaceConversation(Object conversationId, List<AstraDbChatMessage> list) {
        // this method replace all messages to a conversation: delete conversation
        deleteMessages(conversationId);
        // insert conversation again
        chatMemoryCollection.insertMany(list);
    }

    /** {@inheritDoc} */
    @Override
    public void updateMessages(Object o, List<ChatMessage> list) {
        if (list != null) {
            replaceConversation(o, list.stream().map(msg -> {
                AstraDbChatMessage astraDBChatMessage = new AstraDbChatMessage(msg);
                astraDBChatMessage.setChatId((String) o);
                return astraDBChatMessage;
            }).collect(Collectors.toList()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteMessages(Object o) {
        chatMemoryCollection.deleteMany(eq(AstraDbChatMessage.PROP_CHAT_ID, o));
    }
}
