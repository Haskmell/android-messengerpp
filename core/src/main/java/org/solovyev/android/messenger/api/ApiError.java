package org.solovyev.android.messenger.api;

import android.os.Parcelable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.captcha.Captcha;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 11:48 PM
 */
public interface ApiError extends Parcelable {

    @Nonnull
    public static final Parcelable.Creator<ApiError> CREATOR = new ApiErrorParcelableCreator();

    @Nonnull
    String getErrorId();

    @Nullable
    String getErrorDescription();

    @Nullable
    Captcha getCaptcha();
}
