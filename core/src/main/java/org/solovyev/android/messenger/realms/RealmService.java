package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:57 AM
 */
public interface RealmService {

    /**
     * @return collection of all configured realms in application
     */
    @NotNull
    Collection<RealmDef> getRealmDefs();

    @NotNull
    Collection<Realm> getRealms();

    /**
     * Method returns the realm which previously has been registered in this service
     * @param realmDefId id of realm def
     * @return realm
     * @throws UnsupportedRealmException if realm hasn't been registered in this service
     */
    @NotNull
    RealmDef getRealmDefById(@NotNull String realmDefId) throws UnsupportedRealmException;

    @NotNull
    Realm getRealmById(@NotNull String realmId) throws UnsupportedRealmException;
}