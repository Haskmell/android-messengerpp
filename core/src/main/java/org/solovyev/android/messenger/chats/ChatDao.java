package org.solovyev.android.messenger.chats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:11 PM
 */
public interface ChatDao {

    @Nonnull
    MergeDaoResult<ApiChat, String> mergeUserChats(@Nonnull String userId, @Nonnull List<? extends ApiChat> chats);

    @Nonnull
    List<String> loadUserChatIds(@Nonnull String userId);

    @Nonnull
    List<String> loadChatIds();

    @Nonnull
    List<AProperty> loadChatPropertiesById(@Nonnull String chatId);

    @Nonnull
    List<Chat> loadUserChats(@Nonnull String userId);

    @Nonnull
    List<User> loadChatParticipants(@Nonnull String chatId);

    @Nullable
    Chat loadChatById(@Nonnull String chatId);

    void updateChat(@Nonnull Chat chat);

    void deleteAllChats();
}
