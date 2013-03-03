package org.solovyev.android.messenger.chats;

import android.database.Cursor;
import javax.annotation.Nonnull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmEntityMapper;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Converter;

import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 9:42 PM
 */
public class ChatMapper implements Converter<Cursor, Chat> {

    @Nonnull
    private final ChatDao chatDao;

    public ChatMapper(@Nonnull ChatDao chatDao) {
        this.chatDao = chatDao;
    }

    @Nonnull
    @Override
    public Chat convert(@Nonnull Cursor c) {
        final String chatId = c.getString(0);
        final RealmEntity realmEntity = RealmEntityMapper.newInstanceFor(1).convert(c);

        final Integer messagesCount = c.getInt(3);

        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();
        final String lastMessagesSyncDateString = c.getString(4);
        final DateTime lastMessagesSyncDate = lastMessagesSyncDateString == null ? null : dateTimeFormatter.parseDateTime(lastMessagesSyncDateString);

        final List<AProperty> properties = chatDao.loadChatPropertiesById(chatId);

        //final List<ChatMessage> chatMessages = chatDao.loadChatMessages(chatId);
        //final List<User> chatParticipants = chatDao.loadChatParticipants(chatId);

        return ChatImpl.newInstance(realmEntity, messagesCount, properties, lastMessagesSyncDate);
    }
}