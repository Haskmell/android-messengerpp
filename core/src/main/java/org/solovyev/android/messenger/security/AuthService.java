package org.solovyev.android.messenger.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:14 PM
 */
public interface AuthService {

    /**
     * Method logins user into realm by specified user login and password
     *
     *
     * @param realm realm to which user will be logged in
     * @param login user login
     * @param password user password
     * @param resolvedCaptcha captcha
     * @return authentication data linked to logged in user
     *
     * @throws InvalidCredentialsException is user login/password are incorrect
     */
    @NotNull
    AuthData loginUser(@NotNull String realm,
                       @NotNull String login,
                       @NotNull String password,
                       @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException;

    /**
     *
     * @param realm   realm of logged user
     * @return current logged in user, must be invoked after successful user login
     *
     * @throws IllegalStateException if no successful login has been done
     */
    @NotNull
    User getUser(@NotNull String realm) throws UserIsNotLoggedInException;

    /**
     * @param realm user's realm
     * @return true if user has been logged in
     */
    boolean isUserLoggedIn(@NotNull String realm);

    /**
     * Method logs out user
     *
     * @param realm   user's realm
     *
     */
    void logoutUser(@NotNull String realm);


    /**
     * Method returns authentication data related to user in specified realm
     * @param realm realm in which user might be login
     *
     * @return related authentication data if user has been logged in
     *
     * @throws UserIsNotLoggedInException if user hasn't been logged in
     */
    @NotNull
    AuthData getAuthData(@NotNull String realm) throws UserIsNotLoggedInException;

    /*
    **********************************************************************
    *
    *                           SAVING/RESTORING STATE
    *
    **********************************************************************
    */

    /**
     * Method saves current state of authentication service (all logged)
     */
    void save();

    void load();
}