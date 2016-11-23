package com.janedler.V2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by janedler on 2016/11/23.
 */
public class JToast {

    /**
     * Callback class for {@link JToast} instances.
     *
     * @see JToast#setCallback(JToast.Callback)
     */
    public static abstract class Callback {
        /**
         * Indicates that the JToast was dismissed via a swipe.
         */
        public static final int DISMISS_EVENT_SWIPE = 0;
        /**
         * Indicates that the JToast was dismissed via an action click.
         */
        public static final int DISMISS_EVENT_ACTION = 1;
        /**
         * Indicates that the JToast was dismissed via a timeout.
         */
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        /**
         * Indicates that the JToast was dismissed via a call to {@link #dismiss()}.
         */
        public static final int DISMISS_EVENT_MANUAL = 3;
        /**
         * Indicates that the JToast was dismissed from a new JToast being shown.
         */
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        /**
         * @hide
         */
        @IntDef({DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {
        }

        /**
         * Called when the given {@link JToast} has been dismissed, either through a time-out,
         * having been manually dismissed, or an action being clicked.
         *
         * @param JToast The JToast which has been dismissed.
         * @param event     The event which caused the dismissal. One of either:
         *                  {@link #DISMISS_EVENT_SWIPE}, {@link #DISMISS_EVENT_ACTION},
         *                  {@link #DISMISS_EVENT_TIMEOUT}, {@link #DISMISS_EVENT_MANUAL} or
         *                  {@link #DISMISS_EVENT_CONSECUTIVE}.
         */
        public void onDismissed(JToast JToast, @JToast.Callback.DismissEvent int event) {
            // empty
        }

        /**
         * Called when the given {@link JToast} is visible.
         *
         * @param JToast The JToast which is now visible.
         * @see JToast#show()
         */
        public void onShown(JToast JToast) {
            // empty
        }
    }

    /**
     * @hide
     */
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    /**
     * Show the JToast indefinitely. This means that the JToast will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another JToast is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = -2;

    /**
     * Show the JToast for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = -1;

    /**
     * Show the JToast for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 0;

    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;

    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((JToast) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((JToast) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup mParent;
    private final Context mContext;
    private final LinearLayout mView;
    private final TextView mMessageView; //显示文本控件
    private int mDuration;
    private JToast.Callback mCallback;

    private JToast(ViewGroup parent) {
        mParent = parent;
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        mView = (LinearLayout)inflater.inflate(com.janedler.V2.R.layout.ui_v2_toast_layout, mParent, false);
        mMessageView = (TextView) mView.findViewById(com.janedler.V2.R.id.message);
    }

    /**
     * Make a JToast to display a message
     * <p>
     * <p>JToast will try and find a parent view to hold JToast's view from the value given
     * to {@code view}. JToast will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows JToast to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static JToast make(@NonNull View view, @NonNull CharSequence text,
                                 @JToast.Duration int duration) {
        JToast JToast = new JToast(findSuitableParent(view));
        JToast.setText(text);
        JToast.setDuration(duration);
        return JToast;
    }

    /**
     * Make a JToast to display a message.
     * <p>
     * <p>JToast will try and find a parent view to hold JToast's view from the value given
     * to {@code view}. JToast will walk up the view tree trying to find a suitable parent,
     * which is defined as a {@link CoordinatorLayout} or the window decor's content view,
     * whichever comes first.
     * <p>
     * <p>Having a {@link CoordinatorLayout} in your view hierarchy allows JToast to enable
     * certain features, such as swipe-to-dismiss and automatically moving of widgets like
     * {@link FloatingActionButton}.
     *
     * @param view     The view to find a parent from.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                 #LENGTH_LONG}
     */
    @NonNull
    public static JToast make(@NonNull View view, @StringRes int resId, @JToast.Duration int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            } else if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    /**
     * Update the text in this {@link JToast}.
     *
     * @param message The new text for the Toast.
     */
    @NonNull
    public JToast setText(@NonNull CharSequence message) {
        final TextView tv = mMessageView;
        tv.setText(message);
        return this;
    }

    /**
     * Update the text in this {@link JToast}.
     *
     * @param resId The new text for the Toast.
     */
    @NonNull
    public JToast setText(@StringRes int resId) {
        return setText(mContext.getText(resId));
    }

    /**
     * Set how long to show the view for.
     *
     * @param duration either be one of the predefined lengths:
     *                 {@link #LENGTH_SHORT}, {@link #LENGTH_LONG}, or a custom duration
     *                 in milliseconds.
     */
    @NonNull
    public JToast setDuration(@JToast.Duration int duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Show the {@link JToast}.
     */
    public void show() {
        JToastManager.getInstance().show(mDuration, mManagerCallback);
    }

    private final JToastManager.Callback mManagerCallback = new JToastManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, JToast.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, JToast.this));
        }
    };

    final void showView() {
        mParent.addView(mView);
        JToastManager.getInstance().onShown(mManagerCallback);
    }

    final void hideView(int event) {
        // First remove the view from the parent
        mParent.removeView(mView);
        // Now call the dismiss listener (if available)
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        // Finally, tell the JSnackbarManager that it has been dismissed
        JToastManager.getInstance().onDismissed(mManagerCallback);
    }

}
