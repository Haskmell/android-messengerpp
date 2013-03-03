package org.solovyev.android.messenger;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:22 PM
 */
public abstract class AbstractAsyncLoader<R, LI extends ListItem> extends MessengerAsyncTask<Void, Void, List<R>> {

    @Nonnull
    private ListItemArrayAdapter<LI> adapter;

    @Nullable
    private Runnable onPostExecute;

    public AbstractAsyncLoader(@Nonnull Context context,
                               @Nonnull ListItemArrayAdapter<LI> adapter,
                               @Nullable Runnable onPostExecute) {
        super(context);
        this.adapter = adapter;
        this.onPostExecute = onPostExecute;
    }


    @Override
    protected List<R> doWork(@Nonnull List<Void> voids) {
        final Context context = getContext();
        if (context != null) {
            return getElements(context);
        }

        return Collections.emptyList();
    }

    @Nonnull
    protected abstract List<R> getElements(@Nonnull Context context);

    @Override
    protected void onSuccessPostExecute(@Nullable final List<R> elements) {

        if (elements != null) {
            adapter.doWork(new Runnable() {
                @Override
                public void run() {
                    for (R element : elements) {
                        adapter.add(createListItem(element));
                    }
                }
            });

            final Comparator<? super LI> comparator = getComparator();
            if (comparator != null) {
                adapter.sort(comparator);
            }
        }

        if (onPostExecute != null) {
            onPostExecute.run();
        }
    }

    @Nullable
    protected abstract Comparator<? super LI> getComparator();

    @Nonnull
    protected abstract LI createListItem(@Nonnull R element);
}
