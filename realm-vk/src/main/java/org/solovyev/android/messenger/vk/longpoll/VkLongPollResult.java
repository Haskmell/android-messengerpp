package org.solovyev.android.messenger.vk.longpoll;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.User;

import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:47 AM
 */
public class VkLongPollResult implements LongPollResult {

    @Nonnull
    private Long lastUpdate;

    @Nonnull
    private List<LongPollUpdate> updates;

    public VkLongPollResult(@Nonnull Long lastUpdate, @Nonnull List<LongPollUpdate> updates) {
        this.lastUpdate = lastUpdate;
        this.updates = updates;
    }

    @Override
    public Object updateLongPollServerData(@Nullable Object longPollServerData) {
        if ( longPollServerData instanceof LongPollServerData ) {
            final LongPollServerData lpsd = (LongPollServerData) longPollServerData;
            // NOTE: new timestamp
            return new LongPollServerData(lpsd.getKey(), lpsd.getServerUri(), lastUpdate);
        }

        return longPollServerData;
    }

    @Override
    public void doUpdates(@Nonnull User user, @Nonnull Realm realm) {
        for (LongPollUpdate update : updates) {
            update.doUpdate(user, realm);
        }
    }
}
