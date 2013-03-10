package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import org.jivesoftware.smack.*;
import org.solovyev.android.messenger.AbstractRealmConnection;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class XmppRealmConnection extends AbstractRealmConnection<XmppRealm> implements XmppConnectionAware {

    private static final String TAG = XmppRealmConnection.class.getSimpleName();

    public static final int CONNECTION_RETRIES = 3;

    @Nullable
    private volatile Connection connection;

    @Nonnull
    private final ChatManagerListener chatListener;

    @Nonnull
    private final RosterListener rosterListener;

    public XmppRealmConnection(@Nonnull XmppRealm realm, @Nonnull Context context) {
        super(realm, context);
        chatListener = new XmppChatListener(realm);
        rosterListener = new XmppRosterListener(realm);
    }

    @Override
    protected void doWork() {
        // Try to create connection (if not exists)
        if (this.connection == null) {
            tryToConnect(0);
        }
    }

    private synchronized void tryToConnect(int connectionAttempt) {
        if (this.connection == null) {
            final Connection connection = new XMPPConnection(getRealm().getConfiguration().toXmppConfiguration());

            // connect to the server
            try {
                prepareConnection2(connection, getRealm());

                this.connection = connection;
            } catch (XMPPException e) {
                if (connectionAttempt < CONNECTION_RETRIES) {
                    tryToConnect(connectionAttempt + 1);
                } else {
                    stop();
                }
            }
        }
    }

    private void prepareConnection2(@Nonnull Connection connection, @Nonnull XmppRealm realm) throws XMPPException {
        prepareConnection(connection, realm);

        // todo serso: investigate why we cannot add listeners in after connection constructor
        // Attach listeners to connection
        connection.getChatManager().addChatListener(chatListener);
        connection.getRoster().addRosterListener(rosterListener);
    }

    static void prepareConnection(@Nonnull Connection connection, @Nonnull XmppRealm realm) throws XMPPException {
        if (!connection.isConnected()) {
            connection.connect();
            if (!connection.isAuthenticated()) {
                final XmppRealmConfiguration configuration = realm.getConfiguration();
                connection.login(configuration.getLogin(), configuration.getPassword(), configuration.getResource());
            }
        }
    }

    @Override
    protected void stopWork() {
        if (connection != null) {
            connection.getRoster().removeRosterListener(rosterListener);
            connection.getChatManager().removeChatListener(chatListener);

            /**
             * we can't just close connection because some classes may use connection
             * via {@link org.solovyev.android.messenger.realms.xmpp.XmppRealmConnection#doOnConnection(XmppConnectedCallable)}
             */
            //connection.disconnect();
            //connection = null;
        }
    }

    @Nonnull
    private Connection tryGetConnection() throws XMPPException {
        if (connection != null) {
            prepareConnection2(connection, getRealm());
            return connection;
        } else {
            tryToConnect(CONNECTION_RETRIES - 1);
            if (connection != null) {
                return connection;
            } else {
                throw new RealmIsNotConnectedException();
            }
        }
    }

    @Override
    public <R> R doOnConnection(@Nonnull XmppConnectedCallable<R> callable) throws XMPPException {
        final Connection connection = tryGetConnection();
        synchronized (connection) {
            return callable.call(connection);
        }
    }
}
