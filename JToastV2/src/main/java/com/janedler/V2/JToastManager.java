package com.janedler.V2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by janedler on 2016/11/23.
 */
public class JToastManager {

    private static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    private static JToastManager sSnackbarManager;

    static JToastManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new JToastManager();
        }
        return sSnackbarManager;
    }

    private final Object mLock;
    private final Handler mHandler;

    private JToastManager.SnackbarRecord mCurrentSnackbar;
    private JToastManager.SnackbarRecord mNextSnackbar;

    private JToastManager() {
        mLock = new Object();
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Log.e("TAG","hHH");
                switch (message.what) {
                    case MSG_TIMEOUT:
                        handleTimeout((JToastManager.SnackbarRecord) message.obj);
                        return true;
                }
                return false;
            }
        });
    }

    public void show(int duration, JToastManager.Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentSnackbar.duration = duration;

                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
                scheduleTimeoutLocked(mCurrentSnackbar);
                return;
            } else if (isNextSnackbar(callback)) {
                // We'll just update the duration
                mNextSnackbar.duration = duration;
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = new JToastManager.SnackbarRecord(duration, callback);
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(mCurrentSnackbar,
                    JToast.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return;
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null;
                // Otherwise, just show it now
                showNextSnackbarLocked();
            }
        }
    }

    private boolean isCurrentSnackbar(JToastManager.Callback callback) {
        return mCurrentSnackbar != null && mCurrentSnackbar.isSnackbar(callback);
    }

    private boolean isNextSnackbar(JToastManager.Callback callback) {
        return mNextSnackbar != null && mNextSnackbar.isSnackbar(callback);
    }

    public void dismiss(JToastManager.Callback callback, int event) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar, event);
            } else if (isNextSnackbar(callback)) {
                cancelSnackbarLocked(mNextSnackbar, event);
            }
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    public void onDismissed(JToastManager.Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentSnackbar = null;
                if (mNextSnackbar != null) {
                    showNextSnackbarLocked();
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    public void onShown(JToastManager.Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public void cancelTimeout(JToastManager.Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
            }
        }
    }

    public void restoreTimeout(JToastManager.Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    private static class SnackbarRecord {
        private final WeakReference<JToastManager.Callback> callback;
        private int duration;

        SnackbarRecord(int duration, JToastManager.Callback callback) {
            this.callback = new WeakReference<>(callback);
            this.duration = duration;
        }

        boolean isSnackbar(JToastManager.Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }

    private void showNextSnackbarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar;
            mNextSnackbar = null;

            final JToastManager.Callback callback = mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentSnackbar = null;
            }
        }
    }



    private void scheduleTimeoutLocked(JToastManager.SnackbarRecord r) {
        if (r.duration == JToast.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return;
        }

        int durationMs = LONG_DURATION_MS;
        if (r.duration > 0) {
            durationMs = r.duration;
        } else if (r.duration == JToast.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS;
        }
        mHandler.removeCallbacksAndMessages(r);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), 2000);
    }

    private void handleTimeout(JToastManager.SnackbarRecord record) {
        synchronized (mLock) {
            if (mCurrentSnackbar == record || mNextSnackbar == record) {
                cancelSnackbarLocked(record, JToast.Callback.DISMISS_EVENT_TIMEOUT);
            }
        }
    }

    private boolean cancelSnackbarLocked(JToastManager.SnackbarRecord record, int event) {
        final JToastManager.Callback callback = record.callback.get();
        if (callback != null) {
            callback.dismiss(event);
            return true;
        }
        return false;
    }


    /**
     * Manager回调
     */
    interface Callback {
        //显示
        void show();
        //关闭
        void dismiss(int event);
    }


}
