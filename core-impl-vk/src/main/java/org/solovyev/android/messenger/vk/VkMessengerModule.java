package org.solovyev.android.messenger.vk;

import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.AbstractModule;
import org.solovyev.android.db.MessengerSQLiteOpenHelper;
import org.solovyev.android.db.SQLiteOpenHelperConfiguration;
import org.solovyev.android.http.MessengerRemoteFileService;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.MessengerConfiguration;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.chats.ChatDao;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.DefaultChatService;
import org.solovyev.android.messenger.chats.SqliteChatDao;
import org.solovyev.android.messenger.messages.ChatMessageDao;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.DefaultChatMessageService;
import org.solovyev.android.messenger.messages.SqliteChatMessageDao;
import org.solovyev.android.messenger.realms.DefaultRealmService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.registration.RegistrationService;
import org.solovyev.android.messenger.security.AuthService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.security.AuthServiceFacadeImpl;
import org.solovyev.android.messenger.security.AuthServiceImpl;
import org.solovyev.android.messenger.sync.DefaultSyncService;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.DefaultUserService;
import org.solovyev.android.messenger.users.SqliteUserDao;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.vk.registration.DummyRegistrationService;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 10:27 PM
 */
public class VkMessengerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SQLiteOpenHelperConfiguration.class).to(VkMessengerDbConfiguration.class);
        bind(SQLiteOpenHelper.class).to(MessengerSQLiteOpenHelper.class);
        bind(Realm.class).to(VkRealm.class);
        bind(RealmService.class).to(DefaultRealmService.class);
        bind(MessengerConfiguration.class).to(MessengerConfigurationImpl.class);
        bind(AuthService.class).to(AuthServiceImpl.class);
        bind(AuthServiceFacade.class).to(AuthServiceFacadeImpl.class);
        bind(RemoteFileService.class).to(MessengerRemoteFileService.class);

        bind(UserDao.class).to(SqliteUserDao.class);
        bind(UserService.class).to(DefaultUserService.class);

        bind(ChatDao.class).to(SqliteChatDao.class);
        bind(ChatService.class).to(DefaultChatService.class);

        bind(ChatMessageDao.class).to(SqliteChatMessageDao.class);
        bind(ChatMessageService.class).to(DefaultChatMessageService.class);

        bind(SyncService.class).to(DefaultSyncService.class);
        bind(RegistrationService.class).to(DummyRegistrationService.class);
    }
}
