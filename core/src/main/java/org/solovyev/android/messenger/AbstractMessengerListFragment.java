package org.solovyev.android.messenger;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.AThreads;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.security.AuthServiceFacade;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.OnRefreshListener2Adapter;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:57 PM
 */
public abstract class AbstractMessengerListFragment<T, LI extends ListItem> extends RoboSherlockListFragment implements AbsListView.OnScrollListener {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */
    @Nonnull
    private static final String FILTER = "filter";

    @Nonnull
    private static final String POSITION = "position";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */


    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private AuthServiceFacade authServiceFacade;

    @Inject
    @Nonnull
    private SyncService syncService;

    @Inject
    @Nonnull
    private MessengerMultiPaneManager multiPaneManager;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    @Nullable
    private UserEventListener userEventListener;

    private MessengerListItemAdapter<LI> adapter;

    @Nullable
    private MessengerAsyncTask<Void, Void, List<T>> listLoader;

    @Nullable
    private EditText filterInput;

    @Nonnull
    private final String tag;

    @Nullable
    private PullToRefreshListView2 pullLv;

    public AbstractMessengerListFragment(@Nonnull String tag) {
        this.tag = tag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate: " + this);
    }

    @Nonnull
    protected UserService getUserService() {
        return userService;
    }

    @Nonnull
    protected AuthServiceFacade getAuthServiceFacade() {
        return authServiceFacade;
    }

    @Nonnull
    protected SyncService getSyncService() {
        return syncService;
    }

    @Nonnull
    protected ChatService getChatService() {
        return chatService;
    }

    @Nonnull
    protected RealmService getRealmService() {
        return realmService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(tag, "onCreateView: " + this);

        final LinearLayout root = new LinearLayout(this.getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        if (isFilterEnabled()) {
            final ViewGroup filterBoxParent = ViewFromLayoutBuilder.<ViewGroup>newInstance(R.layout.msg_list_filter).build(this.getActivity());

            filterInput = (EditText) filterBoxParent.findViewById(R.id.filter_box);
            filterInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterTextChanged(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            root.addView(filterBoxParent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        final View listViewParent = createListView(inflater, container);
        final ListView listView = getListView(listViewParent);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        root.addView(listViewParent, params);

        multiPaneManager.fillContentPane(this.getActivity(), container, root);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isFilterEnabled()) {
            if (savedInstanceState != null) {
                final String filter = savedInstanceState.getString(FILTER);
                if (Strings.isEmpty(filter)) {
                    setFilterBoxVisible(false);
                } else {
                    filterInput.setText(filter);
                    setFilterBoxVisible(true);
                }
            } else {
                setFilterBoxVisible(false);
            }
        }
    }

    @Nonnull
    protected ListView getListView(@Nonnull View root) {
        return (ListView) root.findViewById(android.R.id.list);
    }

    protected abstract boolean isFilterEnabled();

    public void toggleFilterBox() {
        if (isFilterEnabled()) {
            assert filterInput != null;

            final ViewGroup filterBox = (ViewGroup) getView().findViewById(R.id.filter_box_parent);
            if (filterBox != null) {
                int visibility = filterBox.getVisibility();

                if (visibility != View.VISIBLE) {
                    filterBox.setVisibility(View.VISIBLE);
                    filterInput.requestFocus();

                    final InputMethodManager manager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.showSoftInput(filterInput, InputMethodManager.SHOW_IMPLICIT);

                } else if (visibility != View.GONE) {
                    // if filter box is visible before hiding it clear filter query
                    filterInput.getText().clear();
                    filterInput.clearFocus();

                    filterBox.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setFilterBoxVisible(boolean visible) {
        if (isFilterEnabled()) {
            final ViewGroup filterBox = (ViewGroup) getView().findViewById(R.id.filter_box_parent);
            if (filterBox != null) {
                setFilterBoxVisible(visible, filterBox);
            }
        }
    }

    private void setFilterBoxVisible(boolean visible, @Nonnull ViewGroup filterBox) {
        if (visible) {
            filterBox.setVisibility(View.VISIBLE);
        } else {
            filterBox.setVisibility(View.GONE);
        }
    }

    @Nonnull
    protected MessengerListItemAdapter getAdapter() {
        return adapter;
    }

    public static class PullToRefreshListView2 extends PullToRefreshListView {

        public PullToRefreshListView2(Context context) {
            super(context);
        }

        public PullToRefreshListView2(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public PullToRefreshListView2(Context context, Mode mode) {
            super(context, mode);
        }

        @Override
        public void setRefreshingInternal(boolean doScroll) {
            super.setRefreshingInternal(doScroll);
        }
    }

    /*
    COPIED FROM LIST FRAGMENT
    */

    private static final int INTERNAL_EMPTY_ID = 0x00ff0001;
    private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    private static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    private View createListView(@Nonnull LayoutInflater inflater, ViewGroup container) {
        final Context context = getActivity();

        final FrameLayout root = new FrameLayout(context);

        // ------------------------------------------------------------------

        final LinearLayout progressContainer = new LinearLayout(context);
        progressContainer.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        progressContainer.setOrientation(LinearLayout.VERTICAL);
        progressContainer.setVisibility(View.GONE);
        progressContainer.setGravity(Gravity.CENTER);

        final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        progressContainer.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(progressContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        // ------------------------------------------------------------------

        final FrameLayout listViewContainer = new FrameLayout(context);
        listViewContainer.setId(INTERNAL_LIST_CONTAINER_ID);

        final TextView emptyListCaption = new TextView(context);
        emptyListCaption.setId(INTERNAL_EMPTY_ID);
        emptyListCaption.setGravity(Gravity.CENTER);
        listViewContainer.addView(emptyListCaption, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


        final ListViewAwareOnRefreshListener topRefreshListener = getTopPullRefreshListener();
        final ListViewAwareOnRefreshListener bottomRefreshListener = getBottomPullRefreshListener();

        final View listView;

        final Resources resources = context.getResources();
        if (topRefreshListener == null && bottomRefreshListener == null) {
            listView = new ListView(context);
            fillListView((ListView) listView, context);
            listView.setId(android.R.id.list);
        } else if (topRefreshListener != null && bottomRefreshListener != null) {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.BOTH);

            topRefreshListener.setListView(pullLv);
            bottomRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);
            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView(), container);
            prepareLoadingView(resources, pullLv.getFooterLoadingView(), container);

            pullLv.setOnRefreshListener(new OnRefreshListener2Adapter(topRefreshListener, bottomRefreshListener));
            listView = pullLv;
        } else if (topRefreshListener != null) {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);

            topRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);

            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView(), container);
            prepareLoadingView(resources, pullLv.getFooterLoadingView(), container);

            pullLv.setOnRefreshListener(topRefreshListener);

            listView = pullLv;
        } else {
            pullLv = new PullToRefreshListView2(context, PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);

            bottomRefreshListener.setListView(pullLv);

            fillListView(pullLv.getRefreshableView(), context);
            pullLv.setShowIndicator(false);
            prepareLoadingView(resources, pullLv.getHeaderLoadingView(), container);
            prepareLoadingView(resources, pullLv.getFooterLoadingView(), container);

            pullLv.setOnRefreshListener(bottomRefreshListener);

            listView = pullLv;
        }

        listViewContainer.addView(listView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        root.addView(listViewContainer, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        // ------------------------------------------------------------------

        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        return root;
    }

    @Nullable
    protected PullToRefreshListView2 getPullLv() {
        return pullLv;
    }

    protected void fillListView(@Nonnull ListView lv, @Nonnull Context context) {
        final Resources resources = context.getResources();

        lv.setScrollbarFadingEnabled(true);
        lv.setCacheColorHint(resources.getColor(android.R.color.transparent));
        lv.setOnScrollListener(this);
        lv.setDividerHeight(1);
    }

    private void prepareLoadingView(@Nonnull Resources resources, @Nullable LoadingLayout loadingView, @Nullable ViewGroup paneParent) {
        if (loadingView != null) {
            multiPaneManager.fillLoadingLayout(this.getActivity(), paneParent, resources, loadingView);
        }
    }

    @Nullable
    protected abstract ListViewAwareOnRefreshListener getTopPullRefreshListener();

    @Nullable
    protected abstract ListViewAwareOnRefreshListener getBottomPullRefreshListener();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(tag, "onActivityCreated: " + this);

        adapter = createAdapter();

        userEventListener = new UiThreadUserEventListener();
        this.userService.addListener(userEventListener);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setAdapter(adapter);
        lv.setVerticalFadingEdgeEnabled(false);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent,
                                    final View view,
                                    final int index,
                                    final long id) {
                final int position;

                if ( isFilterEnabled() ) {
                    position = index - 1;
                } else {
                    position = index;
                }

                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                // notify adapter

                adapter.getSelectedItemListener().onItemClick(parent, view, position, id);

                final ListItem.OnClickAction onClickAction = listItem.getOnClickAction();
                if (onClickAction != null) {
                    onClickAction.onClick(getActivity(), adapter, getListView());
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                final ListItem.OnClickAction onLongClickAction = listItem.getOnLongClickAction();
                if (onLongClickAction != null) {
                    onLongClickAction.onClick(getActivity(), adapter, getListView());
                    return true;
                }

                return false;
            }
        });

        final int position;
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(POSITION, -1);
        } else {
            position = -1;
        }

        final PostListLoadingRunnable onPostExecute = new PostListLoadingRunnable(position, lv);

        listLoader = createAsyncLoader(adapter, onPostExecute);

        if (listLoader != null) {
            listLoader.execute();
        } else {
            // we need to schedule onPostExecute in order to be after all pending transaction in fragment manager
            new Handler().post(onPostExecute);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (adapter != null) {
            outState.putInt(POSITION, adapter.getSelectedItemPosition());
        }

        if (isFilterEnabled() && filterInput != null) {
            outState.putString(FILTER, filterInput.getText().toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (listLoader != null) {
            listLoader.cancel(false);
        }

        if (userEventListener != null) {
            this.userService.removeListener(userEventListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void filterTextChanged(@Nonnull CharSequence searchText) {
        if (this.adapter != null) {
            if (this.adapter.isInitialized()) {
                this.adapter.filter(searchText);
            }
        }
    }

    @Nonnull
    protected abstract MessengerListItemAdapter<LI> createAdapter();

    @Nullable
    protected abstract MessengerAsyncTask<Void, Void, List<T>> createAsyncLoader(@Nonnull MessengerListItemAdapter<LI> adapter, @Nonnull Runnable onPostExecute);

    @Override
    public final void onScrollStateChanged(AbsListView view, int scrollState) {
        // do nothing
    }

    @Nonnull
    private final AtomicInteger firstVisibleItem = new AtomicInteger(-1);

    @Override
    public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.firstVisibleItem.get() >= 0 && visibleItemCount > 0) {
            boolean scrollUp = false;
            boolean scrollDown = false;
            if (firstVisibleItem < this.firstVisibleItem.get()) {
                scrollUp = true;
            }
            if (firstVisibleItem > this.firstVisibleItem.get()) {
                scrollDown = true;
            }

            final int lastVisibleItem = firstVisibleItem + visibleItemCount;

            switch (view.getId()) {
                case android.R.id.list:
                    if (scrollUp && firstVisibleItem == 0) {
                        // reach top
                        onListViewTopReached();
                    } else {
                        if (scrollDown && lastVisibleItem == totalItemCount) {
                            // reach bottom
                            onListViewBottomReached();
                        }
                    }

                    if (scrollDown) {
                        onItemReachedFromTop(lastVisibleItem);
                    }

                    if (scrollUp) {
                        onItemReachedFromBottom(lastVisibleItem);
                    }

                    break;
            }
        }

        this.firstVisibleItem.set(firstVisibleItem);

    }

    protected void onItemReachedFromTop(int position) {
    }

    protected void onItemReachedFromBottom(int position) {
    }

    protected void onListViewBottomReached() {
    }

    protected void onListViewTopReached() {
    }

    private class UiThreadUserEventListener implements UserEventListener {

        @Override
        public void onUserEvent(@Nonnull final User eventUser, @Nonnull final UserEventType userEventType, final @Nullable Object data) {
            AThreads.tryRunOnUiThread(getActivity(), new Runnable() {
                @Override
                public void run() {
                    AbstractMessengerListFragment.this.adapter.onUserEvent(eventUser, userEventType, data);
                }
            });
        }
    }

    private class PostListLoadingRunnable implements Runnable {

        private final int position;

        @Nonnull
        private final ListView listView;

        public PostListLoadingRunnable(int position, @Nonnull ListView lv) {
            this.position = position;
            listView = lv;
        }

        @Override
        public void run() {
            try {
                // change adapter state
                adapter.setInitialized(true);

                // change UI state
                setListShown(true);

                // apply filter if any
                if (filterInput != null) {
                    filterTextChanged(filterInput.getText());
                } else {
                    filterTextChanged("");
                }

                if (position >= 0 && position < adapter.getCount()) {
                    adapter.getSelectedItemListener().onItemClick(getListView(), null, position, 0);

                    if (multiPaneManager.isDualPane(getActivity())) {
                        final ListItem.OnClickAction onClickAction = adapter.getItem(position).getOnClickAction();
                        if (onClickAction != null) {
                            onClickAction.onClick(getActivity(), adapter, listView);
                        }
                    }
                }

            } catch (IllegalStateException e) {
                // todo serso: find the reason of the exception
                Log.e(tag, e.getMessage(), e);
            }
        }
    }
}
