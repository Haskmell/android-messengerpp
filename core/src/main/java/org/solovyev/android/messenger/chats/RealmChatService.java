package org.solovyev.android.messenger.chats;

import android.content.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:29 PM
 */
public interface RealmChatService {

    @NotNull
    List<ChatMessage> getChatMessages(@NotNull String realmUserId, @NotNull Context context);

    @NotNull
    List<ChatMessage> getNewerChatMessagesForChat(@NotNull String realmChatId, @NotNull String realmUserId, @NotNull Context context);

    @NotNull
    List<ChatMessage> getOlderChatMessagesForChat(@NotNull String realmChatId, @NotNull String realmUserId, @NotNull Integer offset, @NotNull Context context);

    @NotNull
    List<ApiChat> getUserChats(@NotNull String realmUserId, @NotNull Context context);

    // return: message id
    @NotNull
    String sendChatMessage(@NotNull Chat chat, @NotNull ChatMessage chatMessage, @NotNull Context context);
}